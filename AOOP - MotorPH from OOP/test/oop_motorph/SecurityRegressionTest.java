package oop_motorph;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/** C2 tests use generated disposable passwords and fake JDBC; no live user records are accessed. */
public final class SecurityRegressionTest {
    private static int assertions;
    private static final FakeDriver DRIVER = new FakeDriver();
    private static final char[] TEMPORARY = "Unique-temporary-proof!".toCharArray();
    private static final char[] PERSONAL = " Personal-password-2026! ".toCharArray();

    public static void main(String[] args) throws Exception {
        Path emptyConfiguration = Files.createTempFile("motorph-security-config-", ".properties");
        System.setProperty("aoop.db.config", emptyConfiguration.toString());
        System.setProperty("aoop.db.url", FakeDriver.URL);
        System.setProperty("aoop.db.user", "offline-account");
        System.setProperty("aoop.db.password", "offline-proof");
        DriverManager.registerDriver(DRIVER);
        try {
            passwordStorageAndBoundaries();
            authenticationAndForcedReset();
            resetRevalidationAndAtomicFailure();
            newAccountAndTransportPolicy();
            System.out.println("Security regression checks passed: " + assertions + " assertions; BCrypt cost 12 and fake JDBC only.");
        } finally {
            DriverManager.deregisterDriver(DRIVER);
            for (String key : List.of("aoop.db.config", "aoop.db.url", "aoop.db.user", "aoop.db.password")) { System.clearProperty(key); }
            Files.deleteIfExists(emptyConfiguration);
            Arrays.fill(TEMPORARY, '\0'); Arrays.fill(PERSONAL, '\0');
        }
    }

    private static void passwordStorageAndBoundaries() {
        String first = PasswordService.hashPassword(PERSONAL);
        String second = PasswordService.hashPassword(PERSONAL);
        check(first.startsWith("$2b$12$") && first.length() == 60, "New credentials must use BCrypt12");
        check(!first.equals(second), "Each hash must have a unique random salt");
        check(PasswordService.verifyPassword(PERSONAL, first), "The exact correct password must verify");
        check(!PasswordService.verifyPassword("Personal-password-2026!".toCharArray(), first), "Password whitespace must remain significant");
        check(!PasswordService.verifyPassword("Wrong-personal-password!".toCharArray(), first), "Incorrect passwords must fail");
        check(!PasswordService.verifyPassword(PERSONAL, "f".repeat(64)), "Legacy SHA-256 must never be accepted or wrapped");
        expectPolicy(() -> PasswordService.validateNewPassword("short".toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("123412341234".toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("password1234".toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("x".repeat(73).toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("abcdefghi😀😀😀😀😀😀😀😀😀😀😀😀😀😀😀😀".toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("😀😀😀😀😀😀".toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword(("Unicode-proof-" + '\uD800').toCharArray()));
        expectPolicy(() -> PasswordService.validateNewPassword("control\npassword!".toCharArray()));
        char[] maximum = ("A".repeat(71) + "Z").toCharArray();
        String maximumHash = PasswordService.hashPassword(maximum);
        check(PasswordService.verifyPassword(maximum, maximumHash), "Exactly72 UTF-8 bytes must be supported");
        maximum[71] = 'Y';
        check(!PasswordService.verifyPassword(maximum, maximumHash), "The72nd byte must not be truncated");
        char[] firstTemporary = PasswordService.generateTemporaryPassword();
        char[] secondTemporary = PasswordService.generateTemporaryPassword();
        PasswordService.validateNewPassword(firstTemporary);
        check(!Arrays.equals(firstTemporary, secondTemporary), "Temporary account credentials must be unique");
        Arrays.fill(firstTemporary, '\0'); Arrays.fill(secondTemporary, '\0'); Arrays.fill(maximum, '\0');
    }

    private static void authenticationAndForcedReset() {
        DRIVER.reset(PasswordService.hashPassword(TEMPORARY), true);
        DatabaseHandler.AuthenticationResult pending = DatabaseHandler.authenticate("10001", TEMPORARY);
        check(pending != null && pending.isResetRequired(), "Temporary proof must enter the reset-only state");
        check(pending.getEmployee() == null && pending.getRole() == null, "Pending reset must expose no full session employee or role");
        check(DRIVER.last().queries.size() == 1, "Pending reset must not load a full employee profile");
        check(DRIVER.last().queries.get(0).contains("FOR UPDATE"), "Authentication must lock and revalidate the stored account state");
        check(DatabaseHandler.authenticate("10001", "incorrect-bootstrap".toCharArray()) == null, "Wrong temporary proof must fail");
        check(DatabaseHandler.authenticate("missing-user", TEMPORARY) == null, "Unknown accounts must fail");
        check(DatabaseHandler.resetPassword("10001", "incorrect-bootstrap".toCharArray(), PERSONAL) == null,
                "Reset must revalidate current proof rather than trusting the earlier UI result");
        check(DRIVER.mustChange, "Failed proof must preserve required-reset state");
        expectPolicy(() -> DatabaseHandler.resetPassword("10001", TEMPORARY, TEMPORARY));
        check(DRIVER.last().rolledBack, "Reusing temporary proof must roll back");
        DatabaseHandler.AuthenticationResult full = DatabaseHandler.resetPassword("10001", TEMPORARY, PERSONAL);
        check(full != null && !full.isResetRequired() && full.getEmployee() != null, "Only a committed valid reset can return a full authentication result");
        check(!DRIVER.mustChange && PasswordService.verifyPassword(PERSONAL, DRIVER.hash), "Reset must persist BCrypt and clear its flag together");
        check(DRIVER.last().committed && DRIVER.last().closed, "Reset must commit and release its connection");
        check(DRIVER.last().update.contains("UserID = ? AND password = ? AND must_change_password = 1"),
                "Reset writes must target the locked DB identity, current credential and reset state");
        check(DatabaseHandler.authenticate("10001", TEMPORARY) == null, "A used temporary password must fail after reset");
        check(DatabaseHandler.authenticate("10001", PERSONAL) != null, "The personal password must work after reset");
        check(DatabaseHandler.resetPassword("10001", TEMPORARY, "Another-password-2026!".toCharArray()) == null,
                "Replaying old temporary proof must not change the new password");
        expectPolicy(() -> DatabaseHandler.resetPassword("10001", PERSONAL, "Another-password-2026!".toCharArray()));
    }

    private static void resetRevalidationAndAtomicFailure() {
        DRIVER.reset(PasswordService.hashPassword(TEMPORARY), true);
        String oldHash = DRIVER.hash;
        DRIVER.failUpdate = true;
        expectPersistence(() -> DatabaseHandler.resetPassword("10001", TEMPORARY, PERSONAL));
        check(DRIVER.hash.equals(oldHash) && DRIVER.mustChange, "A failed update must preserve both hash and reset flag");
        check(DRIVER.last().rolledBack && DRIVER.last().closed && !DRIVER.last().committed, "SQL reset failure must roll back and close");
        DRIVER.failUpdate = false;
        DRIVER.zeroRows = true;
        expectPersistence(() -> DatabaseHandler.resetPassword("10001", TEMPORARY, PERSONAL));
        check(DRIVER.hash.equals(oldHash) && DRIVER.mustChange, "A stale guard must not create a full session or alter credentials");
        DRIVER.zeroRows = false;
        DRIVER.profileMissing = true;
        expectPersistence(() -> DatabaseHandler.resetPassword("10001", TEMPORARY, PERSONAL));
        check(DRIVER.hash.equals(oldHash) && DRIVER.mustChange, "A missing employee profile must not consume enrollment proof");
        DRIVER.profileMissing = false;
        DRIVER.reset("0".repeat(64), true);
        check(DatabaseHandler.authenticate("10001", "1234".toCharArray()) == null, "Unmigrated legacy/default accounts must not obtain even a reset-only session");
        check(DatabaseHandler.resetPassword("10001", "1234".toCharArray(), PERSONAL) == null,
                "Public legacy defaults must not claim an account through reset");
    }

    private static void newAccountAndTransportPolicy() {
        DRIVER.reset(PasswordService.hashPassword(PERSONAL), false);
        DatabaseHandler.createUser("10001", TEMPORARY, "HR", "Offline", "Account");
        check(DRIVER.mustChange && PasswordService.verifyPassword(TEMPORARY, DRIVER.hash), "New accounts must use BCrypt and require a personal reset");
        check(DRIVER.last().update.endsWith("VALUES (?, ?, ?, ?, ?, 1)"), "Client input must never disable the new-account reset requirement");
        expectPolicy(() -> DatabaseHandler.createUser("10001", "1234".toCharArray(), "HR", "Offline", "Account"));
        check("REQUIRED".equals(DRIVER.lastProperties.getProperty("sslMode")), "Database connections must require encrypted transport by default");
        for (String option : List.of("sslMode=DISABLED", "sslMode=PREFERRED", "useSSL=false", "ssl%4dode=DISABLED", "socket%54imeout=0")) {
            System.setProperty("aoop.db.url", FakeDriver.URL + "?" + option);
            expectPersistence(() -> DatabaseConfig.getInstance().getConnection());
        }
        System.setProperty("aoop.db.url", FakeDriver.URL);
    }

    private static void check(boolean value, String message) { assertions++; if (!value) { throw new AssertionError(message); } }
    private static void expectPolicy(Runnable operation) {
        try { operation.run(); } catch (IllegalArgumentException expected) { assertions++; return; }
        throw new AssertionError("Expected a password policy rejection");
    }
    private static void expectPersistence(Runnable operation) {
        try { operation.run(); } catch (PersistenceException expected) { assertions++; return; }
        throw new AssertionError("Expected a safe persistence failure");
    }
    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) { return false; }
        if (type == int.class) { return 0; }
        if (type == double.class) { return 0.0; }
        if (type == long.class) { return 0L; }
        return null;
    }

    private static final class State {
        boolean committed, rolledBack, closed;
        String stagedHash, update;
        Boolean stagedMustChange;
        final List<String> queries = new ArrayList<>();
    }

    private static final class FakeDriver implements Driver {
        static final String URL = "jdbc:mysql://security-test.invalid/aoop";
        String hash;
        boolean mustChange, failUpdate, zeroRows, profileMissing;
        Properties lastProperties;
        final List<State> states = new ArrayList<>();
        void reset(String hash, boolean mustChange) { this.hash = hash; this.mustChange = mustChange; }
        State last() { return states.get(states.size() - 1); }

        public Connection connect(String url, Properties properties) {
            if (!acceptsURL(url)) { return null; }
            lastProperties = properties;
            State state = new State(); states.add(state);
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (proxy, method, arguments) -> {
                switch (method.getName()) {
                    case "prepareStatement": return statement(state, (String) arguments[0]);
                    case "commit":
                        if (state.stagedHash != null) { hash = state.stagedHash; mustChange = state.stagedMustChange; }
                        state.committed = true; return null;
                    case "rollback": state.rolledBack = true; return null;
                    case "close": state.closed = true; return null;
                    case "isClosed": return state.closed;
                    default: return defaultValue(method.getReturnType());
                }
            });
        }

        private PreparedStatement statement(State state, String sql) {
            Map<Integer, Object> parameters = new HashMap<>();
            return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class}, (proxy, method, arguments) -> {
                switch (method.getName()) {
                    case "executeQuery":
                        state.queries.add(sql);
                        return result(sql, parameters);
                    case "executeUpdate":
                        if (failUpdate) { throw new SQLException("synthetic offline write failure"); }
                        if (zeroRows) { return 0; }
                        state.update = sql;
                        if (sql.startsWith("INSERT INTO users")) {
                            state.stagedHash = (String) parameters.get(2); state.stagedMustChange = true;
                        } else if (sql.startsWith("UPDATE users")) {
                            if (!Objects.equals(parameters.get(2), 7) || !Objects.equals(parameters.get(3), hash) || !mustChange) { return 0; }
                            state.stagedHash = (String) parameters.get(1); state.stagedMustChange = false;
                        }
                        return 1;
                    default:
                        if (method.getName().startsWith("set") && arguments != null && arguments.length >= 2) {
                            parameters.put((Integer) arguments[0], arguments[1]);
                        }
                        return defaultValue(method.getReturnType());
                }
            });
        }

        private ResultSet result(String sql, Map<Integer, Object> parameters) {
            boolean accountQuery = sql.contains("FROM users WHERE username");
            boolean exists = accountQuery ? "10001".equals(parameters.get(1)) : !profileMissing;
            int[] position = {0};
            return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ResultSet.class}, (proxy, method, arguments) -> {
                switch (method.getName()) {
                    case "next": return exists && position[0]++ == 0;
                    case "getInt": return 7;
                    case "getBoolean": return mustChange;
                    case "getString":
                        String key = String.valueOf(arguments[0]);
                        if ("password".equals(key)) { return hash; }
                        if ("role".equals(key)) { return "HR"; }
                        if ("EmployeeID".equals(key)) { return "10001"; }
                        return "offline-value";
                    default: return defaultValue(method.getReturnType());
                }
            });
        }
        public boolean acceptsURL(String url) { return URL.equals(url); }
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties properties) { return new DriverPropertyInfo[0]; }
        public int getMajorVersion() { return 1; }
        public int getMinorVersion() { return 0; }
        public boolean jdbcCompliant() { return false; }
        public Logger getParentLogger() { return Logger.getGlobal(); }
    }
}
