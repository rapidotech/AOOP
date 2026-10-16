package oop_motorph;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;

/** Offline regression harness: the only JDBC driver on its runtime classpath is FakeDriver. */
public final class PersistenceRegressionTest {
    private static final FakeDriver DRIVER = new FakeDriver();
    private static int assertions;

    public static void main(String[] args) throws Exception {
        Path testDirectory = Files.createTempDirectory("aoop-persistence-test-");
        System.setProperty("aoop.data.dir", testDirectory.toString());
        System.setProperty("aoop.db.config", testDirectory.resolve("database.properties").toString());
        DriverManager.registerDriver(DRIVER);
        System.setProperty("aoop.db.url", "jdbc:mysql://persistence-test.invalid/aoop");
        System.setProperty("aoop.db.user", "offline-test");
        System.setProperty("aoop.db.password", "offline-only");
        try {
            configurationAndConnectionOwnership();
            localConfigurationFallback(testDirectory);
            duplicateChecksAndReadFailures();
            transactionBoundaries();
            localRecordsRoundTrip(testDirectory);
            targetedAuthentication();
            System.out.println("Persistence regression checks passed: " + assertions + " assertions; fake JDBC only.");
        } finally {
            DriverManager.deregisterDriver(DRIVER);
            System.clearProperty("aoop.db.url");
            System.clearProperty("aoop.db.user");
            System.clearProperty("aoop.db.password");
            System.clearProperty("aoop.db.config");
            System.clearProperty("aoop.data.dir");
            try (java.util.stream.Stream<Path> paths = Files.walk(testDirectory)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try { Files.deleteIfExists(path); } catch (java.io.IOException failure) { throw new java.io.UncheckedIOException(failure); }
                });
            }
        }
    }

    private static void configurationAndConnectionOwnership() throws Exception {
        check(DRIVER.connections.isEmpty(), "Singleton must not connect during initialization");
        DatabaseConfig config = DatabaseConfig.getInstance();
        check(DRIVER.connections.isEmpty(), "Singleton construction must not connect");
        Connection first = config.getConnection();
        Connection second = config.getConnection();
        check(first != second, "Connections must have independent ownership");
        first.close();
        check(!second.isClosed(), "Closing one caller's connection must not close another");
        second.close();
        check("5000".equals(DRIVER.properties.getProperty("connectTimeout")), "Connection timeout must be bounded");
        check("15000".equals(DRIVER.properties.getProperty("socketTimeout")), "Read timeout must be bounded");
        System.setProperty("aoop.db.url", "");
        expectPersistence(config::getConnection);
        System.setProperty("aoop.db.url", "jdbc:mysql://persistence-test.invalid/aoop?socketTimeout=0");
        expectPersistence(config::getConnection);
        System.setProperty("aoop.db.url", "jdbc:mysql://persistence-test.invalid/aoop");
        DRIVER.failConnect = true;
        PersistenceException failure = expectPersistence(config::getConnection);
        check(!failure.getMessage().contains("sensitive-driver-detail"), "Driver messages must not reach the UI");
        check(failure.getCause() == null, "Driver causes must not expose credentials through logs");
        DRIVER.failConnect = false;
    }

    private static void duplicateChecksAndReadFailures() {
        DRIVER.count = 1;
        check(CSVHandler.employeeExists("10001", null), "A null excluded ID must still find duplicates");
        State state = DRIVER.latest();
        check(!state.sql.get(0).contains("<>"), "A null excluded ID must not be compared by SQL");
        check(state.parameters.size() == 1, "Only the candidate employee ID should be bound");
        check(state.timeout == 15 && state.closed, "Read statement timeout and connection close must apply");
        check(DatabaseHandler.employeeExists("10002", "10001"), "Excluded IDs must use the filtered query");
        check(DRIVER.latest().parameters.get(2).equals("10001"), "Excluded ID must be bound");
        DRIVER.failQuery = true;
        expectPersistence(() -> DatabaseHandler.employeeExists("10001", null));
        check(DRIVER.latest().closed, "A failed duplicate query must release its connection");
        expectPersistence(DatabaseHandler::getEmployeeData);
        expectPersistence(DatabaseHandler::getSalaryData);
        expectPersistence(DatabaseHandler::getAttendanceData);
        expectPersistence(DatabaseHandler::getPayrollData);
        expectPersistence(DatabaseHandler::readPayrollFromDatabase);
        expectPersistence(DatabaseHandler::getNextEmployeeId);
        DRIVER.failQuery = false;
    }

    private static void localConfigurationFallback(Path directory) throws Exception {
        Path file = directory.resolve("database.properties");
        Files.writeString(file, "url=jdbc:mysql://persistence-test.invalid/aoop\nuser=fallback-test\npassword=local-offline-only\n", StandardCharsets.UTF_8);
        // Environment overrides intentionally win; keep the test isolated from real credentials.
        if (System.getenv("AOOP_DB_URL") == null && System.getenv("AOOP_DB_USER") == null
                && System.getenv("AOOP_DB_PASSWORD") == null) {
            System.clearProperty("aoop.db.url");
            System.clearProperty("aoop.db.user");
            System.clearProperty("aoop.db.password");
            try (Connection connection = DatabaseConfig.getInstance().getConnection()) {
                check("fallback-test".equals(DRIVER.properties.getProperty("user")), "User-local file must supply missing configuration");
            }
            System.setProperty("aoop.db.user", "property-overrides-file");
            try (Connection connection = DatabaseConfig.getInstance().getConnection()) {
                check("property-overrides-file".equals(DRIVER.properties.getProperty("user")), "Java properties must override the local file");
            }
            System.setProperty("aoop.db.password", "");
            try (Connection connection = DatabaseConfig.getInstance().getConnection()) {
                check("".equals(DRIVER.properties.getProperty("password")), "An explicit empty password must override the local file");
            }
        }
        System.setProperty("aoop.db.url", "jdbc:mysql://persistence-test.invalid/aoop");
        System.setProperty("aoop.db.user", "offline-test");
        System.setProperty("aoop.db.password", "offline-only");
    }

    private static void transactionBoundaries() {
        PersistenceSupport.inTransaction("complete offline test", connection -> { });
        State success = DRIVER.latest();
        check(success.committed && !success.rolledBack && success.closed, "Successful transactions must commit and close");
        expectPersistence(() -> PersistenceSupport.inTransaction("complete offline test", connection -> {
            throw new SQLException("sensitive-driver-detail");
        }));
        State sqlFailure = DRIVER.latest();
        check(!sqlFailure.committed && sqlFailure.rolledBack && sqlFailure.closed, "SQL failures must roll back and close");
        try {
            PersistenceSupport.inTransaction("complete offline test", connection -> { throw new IllegalArgumentException("invalid"); });
            throw new AssertionError("Expected validation failure");
        } catch (IllegalArgumentException expected) {
            check(DRIVER.latest().rolledBack && DRIVER.latest().closed, "Runtime validation failures must roll back and close");
        }
        DRIVER.updateRows = 0;
        expectPersistence(() -> DatabaseHandler.deleteEmployee("10001"));
        check(DRIVER.latest().rolledBack && !DRIVER.latest().committed, "A missing employee must not report delete success");
        DRIVER.updateRows = 1;
        DatabaseHandler.deleteEmployee("10001");
        check(DRIVER.latest().committed && DRIVER.latest().closed, "Employee delete must commit and release its connection");
        DRIVER.failUpdate = true;
        expectPersistence(() -> DatabaseHandler.deleteEmployee("10001"));
        check(DRIVER.latest().rolledBack && DRIVER.latest().closed, "Write failures must escape and release the connection");
        DRIVER.failUpdate = false;
        EmpSalaryDetails salary = new EmpSalaryDetails("10001", "First", "Last", "sss", "health", "tin", "fund",
                20000, 1500, 500, 500, 10000, 100);
        int before = DRIVER.connections.size();
        CSVHandler.addEmployeeSalary(salary);
        check(DRIVER.connections.size() == before + 1, "Employee and salary creation must share one transaction");
        check(DRIVER.latest().committed && DRIVER.latest().closed, "Employee and salary creation must commit together");
        DRIVER.failUpdateSql = "UPDATE compensation";
        expectPersistence(() -> CSVHandler.addEmployeeSalary(salary));
        check(DRIVER.latest().rolledBack && !DRIVER.latest().committed && DRIVER.latest().closed,
                "A salary failure after employee creation must roll back the entire operation");
        check(DRIVER.latest().sql.stream().anyMatch(sql -> sql.startsWith("INSERT INTO employees")),
                "Salary rollback test must fail after employee insertion");
        DRIVER.failUpdateSql = null;
        DRIVER.departmentRows = 0;
        expectPersistence(() -> CSVHandler.addEmployeeSalary(salary));
        check(DRIVER.latest().rolledBack && DRIVER.latest().sql.stream().noneMatch(sql -> sql.startsWith("INSERT INTO employees")),
                "A position without a known department must fail before inserting an employee");
        DRIVER.departmentRows = 2;
        expectPersistence(() -> CSVHandler.addEmployeeSalary(salary));
        check(DRIVER.latest().rolledBack, "Ambiguous departments must roll back rather than choose an arbitrary department");
        DRIVER.departmentRows = 1;
    }

    private static void localRecordsRoundTrip(Path directory) throws Exception {
        int before = DRIVER.connections.size();
        check(CSVHandler.loadAttendanceRequests().isEmpty(), "Local requests must start empty without copying bundled private data");
        EmpAttLeave request = new EmpAttLeave("10001", "First, \"Quoted\"", "Last\nLine", "Regular", "Engineer", "N/A",
                LocalDate.of(2026, 10, 17), LocalDate.of(2026, 10, 18), LocalTime.of(8, 0), LocalTime.of(17, 0),
                8, 1, "VL Request", "Pending", 10, 1, 9, 10, 0, 10);
        CSVHandler.saveAttendanceRequest(request);
        List<EmpAttLeave> loaded = CSVHandler.loadAttendanceRequests();
        check(loaded.size() == 1, "Saved requests must reload");
        check(loaded.get(0).getFirstName().equals(request.getFirstName()) && loaded.get(0).getLastName().equals(request.getLastName()),
                "CSV commas, quotes and multiline text must round-trip");
        request.setAttDateTo(LocalDate.of(2026, 10, 19));
        CSVHandler.saveEditAttendanceRequest(request, "17-Oct-26", "18-Oct-26");
        check(CSVHandler.loadAttendanceRequests().get(0).getAttDateTo().equals(request.getAttDateTo()), "Edited requests must reload");
        CSVHandler.updateAttendanceStatus("10001", "Approved", "ignored success presentation");
        check("Approved".equals(CSVHandler.loadAttendanceRequests().get(0).getAttendanceStatus()), "Approval status must persist");
        byte[] previous = Files.readAllBytes(directory.resolve(LocalRecordStore.ATTENDANCE));
        expectPersistence(() -> CSVHandler.updateAttendanceStatus("missing", "Approved", "ignored"));
        check(Arrays.equals(previous, Files.readAllBytes(directory.resolve(LocalRecordStore.ATTENDANCE))), "Failed edits must preserve previous attendance records");
        expectPersistence(() -> LocalRecordStore.write(LocalRecordStore.ATTENDANCE, Collections.singletonList(new String[]{"bad"})));
        check(Arrays.equals(previous, Files.readAllBytes(directory.resolve(LocalRecordStore.ATTENDANCE))), "Invalid rows must preserve previous attendance records");
        expectPersistence(() -> CSVHandler.saveAttendanceRequest(request));
        request.setAttDateFrom(LocalDate.of(2026, 11, 1));
        request.setAttDateTo(LocalDate.of(2026, 11, 2));
        request.setAttendanceStatus("Pending");
        CSVHandler.saveAttendanceRequest(request);
        expectPersistence(() -> CSVHandler.updateAttendanceStatus("10001", "Rejected", "ignored"));
        CSVHandler.updateAttendanceStatus("10001", "2026-11-01", "2026-11-02", "Rejected", "ignored");
        check("Approved".equals(CSVHandler.loadAttendanceRequests().get(0).getAttendanceStatus()), "Precise approval must preserve other requests for the same employee");
        check("Rejected".equals(CSVHandler.loadAttendanceRequests().get(1).getAttendanceStatus()), "Precise approval must target only its requested date range");
        previous = Files.readAllBytes(directory.resolve(LocalRecordStore.ATTENDANCE));
        String[] payroll = new String[22];
        Arrays.fill(payroll, "test");
        payroll[1] = "10001"; payroll[20] = "123.45"; payroll[21] = "Pending";
        payroll[4] = "2026-10-01"; payroll[5] = "2026-10-15";
        CSVHandler.saveUpdatedPayrollData(Collections.singletonList(payroll));
        CSVHandler.updatePayrollStatus("10001", "Approved", "ignored");
        String[] savedPayroll = CSVHandler.loadPayrollLedger().get(0);
        check("Approved".equals(savedPayroll[21]), "Payroll status must persist in column 21");
        check("123.45".equals(savedPayroll[20]), "Updating payroll status must preserve net pay");
        String[] secondPayroll = payroll.clone();
        secondPayroll[0] = "second-transaction";
        secondPayroll[4] = "2026-10-16"; secondPayroll[5] = "2026-10-31";
        CSVHandler.saveUpdatedPayrollData(Arrays.asList(savedPayroll, secondPayroll));
        expectPersistence(() -> CSVHandler.updatePayrollStatus("10001", "Verified", "ignored"));
        CSVHandler.updatePayrollStatus(savedPayroll[0], "10001", "01-Oct-26", "15-Oct-26", "Verified", "ignored");
        List<String[]> precisePayroll = CSVHandler.loadPayrollLedger();
        check("Verified".equals(precisePayroll.get(0)[21]), "Payroll approval must match the selected transaction and period");
        check("Pending".equals(precisePayroll.get(1)[21]), "Payroll approval must preserve other periods for the same employee");
        Path notDirectory = directory.resolve("regular-file");
        Files.writeString(notDirectory, "sentinel", StandardCharsets.UTF_8);
        System.setProperty("aoop.data.dir", notDirectory.toString());
        expectPersistence(() -> CSVHandler.saveAttendanceRequest(request));
        System.setProperty("aoop.data.dir", directory.toString());
        check(Arrays.equals(previous, Files.readAllBytes(directory.resolve(LocalRecordStore.ATTENDANCE))), "Filesystem failures must preserve existing records");
        check(DRIVER.connections.size() == before, "Local request and payroll operations must not mutate the database");
    }

    private static void targetedAuthentication() {
        DRIVER.accountHash = PasswordService.hashPassword("Stored-test-password!".toCharArray());
        int before = DRIVER.connections.size();
        check(DatabaseHandler.authenticate("10001", "wrong-password".toCharArray()) == null, "Invalid hashes must reject login");
        check(DRIVER.connections.size() == before + 1, "Rejected login must not load employees");
        State invalid = DRIVER.latest();
        check(invalid.sql.get(0).contains("WHERE username = ?"), "Credential lookup must target one account");
        check(invalid.parameters.get(1).equals("10001"), "Login username must be bound");
        DatabaseHandler.AuthenticationResult result = DatabaseHandler.authenticate("10001", "Stored-test-password!".toCharArray());
        check(result != null && "10001".equals(result.getEmployee().getEmpID()), "Valid login must resolve its employee");
        check("HR".equals(result.getRole()), "Valid login must retain its role");
        State employee = DRIVER.latest();
        check(employee.sql.stream().anyMatch(sql -> sql.contains("WHERE e.EmployeeID = ?")), "Login employee lookup must target one employee");
        check(employee.closed, "Authentication must release employee connections");
    }

    private static PersistenceException expectPersistence(Runnable action) {
        try { action.run(); }
        catch (PersistenceException expected) { assertions++; return expected; }
        throw new AssertionError("Expected a persistence failure");
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) { throw new AssertionError(message); }
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) { return null; }
        if (type == boolean.class) { return false; }
        if (type == int.class) { return 0; }
        if (type == long.class) { return 0L; }
        if (type == double.class) { return 0.0; }
        if (type == float.class) { return 0.0f; }
        if (type == short.class) { return (short) 0; }
        if (type == byte.class) { return (byte) 0; }
        return '\0';
    }

    private static final class State {
        boolean closed, committed, rolledBack;
        int timeout;
        final List<String> sql = new ArrayList<>();
        final Map<Integer, Object> parameters = new HashMap<>();
    }

    private static final class FakeDriver implements Driver {
        final List<State> connections = new ArrayList<>();
        Properties properties;
        boolean failConnect, failQuery, failUpdate;
        int count = 1, updateRows = 1, departmentRows = 1;
        String accountHash;
        String failUpdateSql;

        State latest() { return connections.get(connections.size() - 1); }

        public Connection connect(String url, Properties input) throws SQLException {
            if (!acceptsURL(url)) { return null; }
            if (failConnect) { throw new SQLException("sensitive-driver-detail"); }
            properties = input;
            State state = new State();
            connections.add(state);
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "close": state.closed = true; return null;
                    case "isClosed": return state.closed;
                    case "commit": state.committed = true; return null;
                    case "rollback": state.rolledBack = true; return null;
                    case "prepareStatement":
                        String sql = (String) args[0];
                        state.sql.add(sql);
                        return statement(state, sql);
                    default: return defaultValue(method.getReturnType());
                }
            });
        }

        private PreparedStatement statement(State state, String sql) {
            return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "setQueryTimeout": state.timeout = (Integer) args[0]; return null;
                    case "executeQuery":
                        if (failQuery) { throw new SQLException("sensitive-driver-detail"); }
                        return result(sql);
                    case "executeUpdate":
                        if (failUpdate || (failUpdateSql != null && sql.startsWith(failUpdateSql))) { throw new SQLException("sensitive-driver-detail"); }
                        return updateRows;
                    default:
                        if (method.getName().startsWith("set") && args != null && args.length >= 2) {
                            state.parameters.put((Integer) args[0], args[1]);
                        }
                        return defaultValue(method.getReturnType());
                }
            });
        }

        private ResultSet result(String sql) {
            int[] cursor = {0};
            return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ResultSet.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "next": return cursor[0]++ < (sql.startsWith("SELECT DISTINCT DepartmentID") ? departmentRows : 1);
                    case "getInt": return count;
                    case "getString":
                        if ("password".equals(args[0])) { return accountHash; }
                        if ("role".equals(args[0])) { return "HR"; }
                        if ("EmployeeID".equals(args[0])) { return "10001"; }
                        return "test-value";
                    default: return defaultValue(method.getReturnType());
                }
            });
        }

        public boolean acceptsURL(String url) { return "jdbc:mysql://persistence-test.invalid/aoop".equals(url); }
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties properties) { return new DriverPropertyInfo[0]; }
        public int getMajorVersion() { return 1; }
        public int getMinorVersion() { return 0; }
        public boolean jdbcCompliant() { return false; }
        public Logger getParentLogger() { return Logger.getGlobal(); }
    }
}
