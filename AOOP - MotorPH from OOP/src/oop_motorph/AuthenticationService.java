package oop_motorph;

import java.sql.*;
import java.time.*;
import java.util.List;

/** Authentication throttles and audit events commit together, including rejected attempts. */
final class AuthenticationService {
    private static final String UNKNOWN_ACCOUNT_HASH = dummyHash();
    private AuthenticationService() { }

    static DatabaseHandler.AuthenticationResult authenticate(String username, char[] password) {
        return attempt(username, password, null);
    }

    static DatabaseHandler.AuthenticationResult resetPassword(String username, char[] currentPassword, char[] newPassword) {
        PasswordService.validateNewPassword(newPassword);
        return attempt(username, currentPassword, newPassword);
    }

    private static DatabaseHandler.AuthenticationResult attempt(String username, char[] password, char[] replacement) {
        if (username == null || username.isBlank() || password == null || password.length == 0) { return null; }
        String replacementHash = replacement == null ? null : PasswordService.hashPassword(replacement);
        Attempt attempt = PersistenceSupport.inTransactionResult("authenticate the account", connection -> {
            Source source = lockedSource(connection);
            Account account = lockedAccount(connection, username);
            Instant now = source.now;
            long blocked = Math.max(waitSeconds(now, source.retryAfter, source.lockedUntil),
                    account == null ? 0 : waitSeconds(now, account.retryAfter, account.lockedUntil));
            if (blocked > 0) {
                AuditService.append(connection, "anonymous", "AUTH_THROTTLED", username, "DENIED", "retry_after_seconds=" + blocked);
                return new Attempt(null, account, blocked);
            }
            boolean credentialValid = verifyCredential(password, account == null ? null : account.hash);
            if (account == null || !credentialValid) {
                int accountFailures = account == null ? 0 : effectiveFailures(account.failures, account.lockedUntil, now) + 1;
                int machineFailures = effectiveFailures(source.failures, source.lockedUntil, now) + 1;
                long seconds = Math.max(retrySeconds(accountFailures), retrySeconds(machineFailures));
                boolean locked = accountFailures >= 5 || machineFailures >= 5;
                Instant until = now.plusSeconds(locked ? 900 : seconds);
                updateSource(connection, machineFailures, until, locked ? until : null);
                if (account != null) { updateAccountThrottle(connection, account.id, accountFailures, until, locked ? until : null); }
                AuditService.append(connection, "anonymous", locked ? "AUTH_LOCKED" : "AUTH_FAILURE", username, "DENIED",
                        "failed_attempts=" + accountFailures + ";retry_after_seconds=" + (locked ? 900 : seconds));
                return new Attempt(null, account, locked ? 900 : seconds);
            }
            if (replacement != null) {
                if (!account.mustChange) { throw new IllegalArgumentException("A reset is no longer required. Sign in again with your current password."); }
                if (PasswordService.verifyPassword(replacement, account.hash)) { throw new IllegalArgumentException("Choose a new password that differs from your temporary password."); }
                DatabaseHandler.AuthenticationResult result = fullResult(connection, account.username, account.role);
                if (result == null) { throw new PersistenceException("The employee profile is missing. Contact your administrator."); }
                try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection,
                        "UPDATE users SET password=?,must_change_password=0,failed_attempts=0,locked_until=NULL,next_login_at=NULL WHERE UserID=? AND password=? AND must_change_password=1")) {
                    statement.setString(1, replacementHash); statement.setInt(2, account.id); statement.setString(3, account.hash);
                    PersistenceSupport.requireOneRow(statement.executeUpdate());
                }
                updateSource(connection, 0, null, null);
                AuditService.append(connection, account.username, "PASSWORD_RESET", account.username, "SUCCESS", "forced_reset_completed");
                return new Attempt(result, new Account(account.id, account.username, replacementHash, account.role, false, 0, null, null), 0);
            }
            updateAccountThrottle(connection, account.id, 0, null, null);
            updateSource(connection, 0, null, null);
            DatabaseHandler.AuthenticationResult result = account.mustChange
                    ? DatabaseHandler.AuthenticationResult.resetRequired(account.username) : fullResult(connection, account.username, account.role);
            AuditService.append(connection, account.username, account.mustChange ? "AUTH_RESET_REQUIRED" : "AUTH_SUCCESS", account.username,
                    result == null ? "DENIED" : "SUCCESS", account.mustChange ? "restricted_reset_only" : "credential_verified");
            return new Attempt(result, account, 0);
        });
        // Throw only AFTER the rejected attempt's counters and audit append have committed.
        if (attempt.retryAfter > 0) { throw new AuthenticationThrottledException(attempt.retryAfter); }
        if (attempt.result != null && !attempt.result.isResetRequired()) {
            AuthorizationService.issue(attempt.result, attempt.account.id, attempt.account.username, attempt.account.role, attempt.account.hash);
        }
        return attempt.result;
    }

    static void createUser(String username, char[] temporaryPassword, String role, String firstName, String lastName) {
        AuthorizationService.requireHr();
        if (username == null || username.isBlank()) { throw new IllegalArgumentException("A username is required."); }
        if (!"HR".equalsIgnoreCase(role) && !"EMPLOYEE".equalsIgnoreCase(role)) { throw new IllegalArgumentException("Select a valid account role."); }
        if (firstName == null || lastName == null) { throw new IllegalArgumentException("First and last names are required."); }
        String hash = PasswordService.hashPassword(temporaryPassword);
        PersistenceSupport.inTransaction("create the account", connection -> {
            AuthorizationService.requireHr(connection);
            if (DatabaseHandler.getEmployeeData(connection, username).isEmpty()) { throw new IllegalArgumentException("Create the employee profile before enrolling an account."); }
            try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection,
                    "INSERT INTO users (username,password,role,first_name,last_name,must_change_password) VALUES (?,?,?,?,?,1)")) {
                statement.setString(1, username); statement.setString(2, hash); statement.setString(3, role);
                statement.setString(4, firstName); statement.setString(5, lastName);
                PersistenceSupport.requireOneRow(statement.executeUpdate());
            }
            AuditService.business(connection, "USER_CREATED", username);
        });
    }

    static long retrySeconds(int failures) { return failures <= 0 ? 0 : failures >= 5 ? 900 : 1L << (failures - 1); }
    static boolean verifyCredential(char[] password, String storedHash) {
        boolean supported = PasswordService.isSupportedHash(storedHash);
        boolean verified = PasswordService.verifyPassword(password, supported ? storedHash : UNKNOWN_ACCOUNT_HASH);
        return supported && verified;
    }
    private static String dummyHash() {
        char[] temporary = PasswordService.generateTemporaryPassword();
        try { return PasswordService.hashPassword(temporary); }
        finally { java.util.Arrays.fill(temporary, '\0'); }
    }
    private static int effectiveFailures(int failures, Instant lockedUntil, Instant now) { return lockedUntil != null && !lockedUntil.isAfter(now) ? 0 : failures; }
    private static long waitSeconds(Instant now, Instant... deadlines) {
        long seconds = 0;
        for (Instant deadline : deadlines) { if (deadline != null && deadline.isAfter(now)) { seconds = Math.max(seconds, Math.max(1, (Duration.between(now, deadline).toMillis() + 999) / 1000)); } }
        return seconds;
    }

    private static Source lockedSource(Connection connection) throws SQLException {
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection, "INSERT IGNORE INTO AuthThrottle(SourceKey,SourceMachine) VALUES (?,?)")) {
            statement.setString(1, AuditService.sourceKey()); statement.setString(2, AuditService.sourceMachine()); statement.executeUpdate();
        }
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection,
                "SELECT FailedAttempts,RetryAfter,LockedUntil,UTC_TIMESTAMP(6) AS ServerNow FROM AuthThrottle WHERE SourceKey=? FOR UPDATE")) {
            statement.setString(1, AuditService.sourceKey());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) { throw new SQLException("Missing authentication source state."); }
                Instant now = AuditService.instant(result, "ServerNow");
                if (now == null) { throw new SQLException("Missing server time."); }
                return new Source(result.getInt("FailedAttempts"), AuditService.instant(result, "RetryAfter"), AuditService.instant(result, "LockedUntil"), now);
            }
        }
    }

    private static Account lockedAccount(Connection connection, String username) throws SQLException {
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection,
                "SELECT UserID,username,password,role,must_change_password,failed_attempts,locked_until,next_login_at FROM users WHERE username=? FOR UPDATE")) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) { return null; }
                boolean change = result.getBoolean("must_change_password");
                if (result.wasNull()) { throw new SQLException("Missing reset state."); }
                Account account = new Account(result.getInt("UserID"), result.getString("username"), result.getString("password"), result.getString("role"), change,
                        result.getInt("failed_attempts"), AuditService.instant(result, "next_login_at"), AuditService.instant(result, "locked_until"));
                if (result.next()) { throw new SQLException("Ambiguous account."); }
                return account;
            }
        }
    }

    private static void updateAccountThrottle(Connection connection, int userId, int failures, Instant retry, Instant locked) throws SQLException {
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection, "UPDATE users SET failed_attempts=?,next_login_at=?,locked_until=? WHERE UserID=?")) {
            statement.setInt(1, failures); setInstant(statement, 2, retry); setInstant(statement, 3, locked); statement.setInt(4, userId);
            PersistenceSupport.requireOneRow(statement.executeUpdate());
        }
    }
    private static void updateSource(Connection connection, int failures, Instant retry, Instant locked) throws SQLException {
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection, "UPDATE AuthThrottle SET FailedAttempts=?,RetryAfter=?,LockedUntil=? WHERE SourceKey=?")) {
            statement.setInt(1, failures); setInstant(statement, 2, retry); setInstant(statement, 3, locked); statement.setString(4, AuditService.sourceKey());
            PersistenceSupport.requireOneRow(statement.executeUpdate());
        }
    }
    private static void setInstant(PreparedStatement statement, int index, Instant value) throws SQLException {
        if (value == null) { statement.setNull(index, Types.TIMESTAMP); }
        else { statement.setTimestamp(index, Timestamp.from(value), java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))); }
    }
    private static DatabaseHandler.AuthenticationResult fullResult(Connection connection, String username, String role) throws SQLException {
        List<EmpDetails> employees = DatabaseHandler.getEmployeeData(connection, username);
        return employees.size() == 1 ? new DatabaseHandler.AuthenticationResult(employees.get(0), role) : null;
    }
    private static final class Attempt {
        final DatabaseHandler.AuthenticationResult result; final Account account; final long retryAfter;
        Attempt(DatabaseHandler.AuthenticationResult result, Account account, long retry) { this.result=result; this.account=account; retryAfter=retry; }
    }
    private static final class Account {
        final int id, failures; final String username, hash, role; final boolean mustChange; final Instant retryAfter, lockedUntil;
        Account(int id,String username,String hash,String role,boolean change,int failures,Instant retry,Instant locked) {
            this.id=id;this.username=username;this.hash=hash;this.role=role;mustChange=change;this.failures=failures;retryAfter=retry;lockedUntil=locked;
        }
    }
    private static final class Source {
        final int failures; final Instant retryAfter, lockedUntil, now;
        Source(int failures,Instant retry,Instant locked,Instant now) { this.failures=failures;retryAfter=retry;lockedUntil=locked;this.now=now; }
    }
}
