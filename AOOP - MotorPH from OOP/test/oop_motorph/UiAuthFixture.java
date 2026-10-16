package oop_motorph;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/** Test-only JDBC double. Sessions still require a genuine BCrypt-verified issued result. */
final class UiAuthFixture implements AutoCloseable {
    private static final String URL = "jdbc:mysql://ui-auth-fixture.invalid/aoop";
    private final Map<String, String> previous = new HashMap<>();
    private final Path configuration;
    private final Driver driver;
    private final char[] proof = "Unique-UI-fixture-password!".toCharArray();
    private final String hash = PasswordService.hashPassword(proof);
    private String role = "EMPLOYEE";
    private boolean resetRequired;
    int employeeQueries;
    java.util.List<SecurityAlert> securityAlerts = java.util.List.of();
    boolean failSecurityAlerts;

    UiAuthFixture() throws Exception {
        configuration = Files.createTempFile("motorph-ui-auth-", ".properties");
        set("aoop.db.config", configuration.toString()); set("aoop.db.url", URL);
        set("aoop.db.user", "offline-ui"); set("aoop.db.password", "offline-ui-only");
        set("aoop.data.dir", Files.createTempDirectory("motorph-ui-empty-ledger-").toString());
        driver = new Driver() {
            @Override public Connection connect(String url, Properties properties) { return acceptsURL(url) ? connection() : null; }
            @Override public boolean acceptsURL(String url) { return URL.equals(url); }
            @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
            @Override public int getMajorVersion() { return 1; }
            @Override public int getMinorVersion() { return 0; }
            @Override public boolean jdbcCompliant() { return false; }
            @Override public Logger getParentLogger() { return Logger.getGlobal(); }
        };
        DriverManager.registerDriver(driver);
    }

    synchronized DatabaseHandler.AuthenticationResult issue(String role, boolean resetRequired) {
        this.role = role; this.resetRequired = resetRequired;
        return DatabaseHandler.authenticate("90001", proof);
    }

    void establish(String role) { EmpUserSession.getInstance().establish(issue(role, false)); }

    private void set(String key, String value) { previous.put(key, System.getProperty(key)); System.setProperty(key, value); }

    private Connection connection() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (proxy, method, args) -> {
            if ("prepareStatement".equals(method.getName()) || "prepareCall".equals(method.getName())) return statement(String.valueOf(args[0]));
            if ("getAutoCommit".equals(method.getName())) return false;
            if ("isValid".equals(method.getName())) return true;
            return defaultValue(method.getReturnType());
        });
    }

    private PreparedStatement statement(String sql) {
        Map<Integer, Object> parameters = new HashMap<>();
        return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{CallableStatement.class}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "executeQuery":
                    if (failSecurityAlerts && sql.toLowerCase(Locale.ROOT).contains("from auditlog")) throw new SQLException("offline alert-query fixture");
                    return rows(sql, parameters);
                case "executeUpdate": return 1;
                case "execute": return false;
                case "getUpdateCount": return -1;
                default:
                    if (method.getName().startsWith("set") && args != null && args.length >= 2 && args[0] instanceof Integer) parameters.put((Integer) args[0], args[1]);
                    return defaultValue(method.getReturnType());
            }
        });
    }

    private ResultSet rows(String sql, Map<Integer, Object> parameters) {
        String query = sql.toLowerCase(Locale.ROOT);
        boolean account = query.contains("from users");
        boolean employee = query.contains("from employees");
        boolean throttle = query.contains("from auththrottle");
        boolean alertQuery = query.contains("from auditlog");
        if (employee) employeeQueries++;
        boolean exists = account || employee || throttle;
        int[] cursor = {0};
        boolean[] wasNull = {false};
        return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ResultSet.class}, (proxy, method, args) -> {
            String key = args == null || args.length == 0 ? "" : String.valueOf(args[0]).toLowerCase(Locale.ROOT);
            if (method.getName().startsWith("get") && !"getTimestamp".equals(method.getName())) wasNull[0] = false;
            switch (method.getName()) {
                case "next": return alertQuery ? cursor[0]++ < securityAlerts.size() : exists && cursor[0]++ == 0;
                case "getBoolean": return "must_change_password".equals(key) && resetRequired;
                case "getInt": return "userid".equals(key) ? 7 : 0;
                case "getLong": return alertQuery ? securityAlerts.get(cursor[0] - 1).getEventId() : 0L;
                case "getTimestamp":
                    if (alertQuery) { wasNull[0] = false; return Timestamp.from(securityAlerts.get(cursor[0] - 1).getOccurredAt()); }
                    if ("servernow".equals(key)) { wasNull[0] = false; return Timestamp.from(java.time.Instant.now()); }
                    wasNull[0] = true; return null;
                case "getString":
                    if (alertQuery) {
                        SecurityAlert alert = securityAlerts.get(cursor[0] - 1);
                        return "3".equals(key) ? alert.getUsername() : "4".equals(key) ? alert.getSourceMachine() : alert.getAction();
                    }
                    if ("password".equals(key)) return hash;
                    if ("role".equals(key)) return role;
                    if ("username".equals(key) || "employeeid".equals(key)) return "90001";
                    if ("firstname".equals(key)) return "Sample";
                    if ("lastname".equals(key)) return "Employee";
                    if ("birthday".equals(key)) return "1990-01-01";
                    if ("statusname".equals(key)) return "Regular";
                    if ("positionname".equals(key)) return "HR Manager";
                    if ("phonenumber".equals(key)) return "09123456789";
                    if ("streetaddress".equals(key)) return "Synthetic address";
                    if ("city".equals(key)) return "Test City";
                    if ("region".equals(key)) return "Test Region";
                    if ("supervisorname".equals(key)) return "No supervisor";
                    return "offline-value";
                case "wasNull": return wasNull[0];
                default: return defaultValue(method.getReturnType());
            }
        });
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class || type == short.class || type == byte.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0d;
        if (type == float.class) return 0f;
        return null;
    }

    @Override public void close() throws Exception {
        EmpUserSession.getInstance().clearSession();
        DriverManager.deregisterDriver(driver);
        for (Map.Entry<String, String> setting : previous.entrySet()) {
            if (setting.getValue() == null) System.clearProperty(setting.getKey()); else System.setProperty(setting.getKey(), setting.getValue());
        }
        Files.deleteIfExists(configuration);
        Arrays.fill(proof, '\0');
    }
}
