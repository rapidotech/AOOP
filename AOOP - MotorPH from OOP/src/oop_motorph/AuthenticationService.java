package oop_motorph;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Password authentication and enrollment; full sessions are withheld until a required reset commits. */
final class AuthenticationService {
    private AuthenticationService() { }

    static DatabaseHandler.AuthenticationResult authenticate(String username, char[] password) {
        if (username == null || username.isBlank() || password == null || password.length == 0) { return null; }
        return PersistenceSupport.inTransactionResult("authenticate the account", connection -> {
            Account account = lockedAccount(connection, username);
            if (account == null || !PasswordService.verifyPassword(password, account.hash)) { return null; }
            if (account.mustChange) { return new DatabaseHandler.AuthenticationResult(null, null, true); }
            return fullResult(connection, username, account.role);
        });
    }

    static DatabaseHandler.AuthenticationResult resetPassword(String username, char[] currentPassword, char[] newPassword) {
        PasswordService.validateNewPassword(newPassword);
        if (username == null || username.isBlank() || currentPassword == null || currentPassword.length == 0) { return null; }
        // Slow hashing occurs before taking the row lock; proof and state are rechecked inside it.
        String replacementHash = PasswordService.hashPassword(newPassword);
        return PersistenceSupport.inTransactionResult("reset the account password", connection -> {
            Account account = lockedAccount(connection, username);
            if (account == null || !PasswordService.verifyPassword(currentPassword, account.hash)) { return null; }
            if (!account.mustChange) {
                throw new IllegalArgumentException("A reset is no longer required. Sign in again with your current password.");
            }
            if (PasswordService.verifyPassword(newPassword, account.hash)) {
                throw new IllegalArgumentException("Choose a new password that differs from your temporary password.");
            }
            DatabaseHandler.AuthenticationResult result = fullResult(connection, username, account.role);
            if (result == null) { throw new PersistenceException("The employee profile is missing. Contact your administrator before resetting this account."); }
            try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection,
                    "UPDATE users SET password = ?, must_change_password = 0 WHERE UserID = ? AND password = ? AND must_change_password = 1")) {
                statement.setString(1, replacementHash);
                statement.setInt(2, account.id);
                statement.setString(3, account.hash);
                PersistenceSupport.requireOneRow(statement.executeUpdate());
            }
            return result;
        });
    }

    static void createUser(String username, char[] temporaryPassword, String role, String firstName, String lastName) {
        if (username == null || username.isBlank()) { throw new IllegalArgumentException("A username is required."); }
        if (!"HR".equals(role) && !"EMPLOYEE".equals(role)) { throw new IllegalArgumentException("Select a valid account role."); }
        String hash = PasswordService.hashPassword(temporaryPassword);
        PersistenceSupport.inTransaction("create the account", connection -> {
            if (DatabaseHandler.getEmployeeData(connection, username).isEmpty()) {
                throw new IllegalArgumentException("Create the employee profile before enrolling an account.");
            }
            try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection,
                    "INSERT INTO users (username, password, role, first_name, last_name, must_change_password) VALUES (?, ?, ?, ?, ?, 1)")) {
                statement.setString(1, username);
                statement.setString(2, hash);
                statement.setString(3, role);
                statement.setString(4, firstName);
                statement.setString(5, lastName);
                PersistenceSupport.requireOneRow(statement.executeUpdate());
            }
        });
    }

    private static Account lockedAccount(Connection connection, String username) throws SQLException {
        try (PreparedStatement statement = PersistenceSupport.prepareStatement(connection,
                "SELECT UserID, password, role, must_change_password FROM users WHERE username = ? FOR UPDATE")) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) { return null; }
                Account account = new Account(result.getInt("UserID"), result.getString("password"),
                        result.getString("role"), result.getBoolean("must_change_password"));
                if (result.wasNull()) { throw new SQLException("Missing password reset state."); }
                if (result.next()) { throw new SQLException("Ambiguous account username."); }
                return account;
            }
        }
    }

    private static DatabaseHandler.AuthenticationResult fullResult(Connection connection, String username, String role) throws SQLException {
        List<EmpDetails> employees = DatabaseHandler.getEmployeeData(connection, username);
        return employees.size() == 1 ? new DatabaseHandler.AuthenticationResult(employees.get(0), role) : null;
    }

    private static final class Account {
        final int id;
        final String hash, role;
        final boolean mustChange;
        Account(int id, String hash, String role, boolean mustChange) {
            this.id = id; this.hash = hash; this.role = role; this.mustChange = mustChange;
        }
    }
}
