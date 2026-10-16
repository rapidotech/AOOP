import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import oop_motorph.*;

/** Explicitly invoked isolated integration harness. Refuses every non-clone URL. */
public final class SecurityAssurance {
    private static int checks;
    private static final List<String> outcomes = new ArrayList<>();
    private static String url() {
        String url = System.getenv("M2_JDBC_URL");
        if (url == null || !url.startsWith("jdbc:mysql://127.0.0.1:3307/aoop_m2?"))
            throw new IllegalStateException("Refusing a non-isolated database URL.");
        return url;
    }
    private static Connection admin() throws SQLException {
        return DriverManager.getConnection(url(), "root", System.getenv("M2_ADMIN_PASSWORD"));
    }
    private static void check(boolean condition, String name) {
        if (!condition) throw new IllegalStateException("Assurance failed: " + name);
        checks++; outcomes.add(name + ": success");
    }
    private static void denied(Connection connection, String sql, String name) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException expected) {
            if (!Set.of(1142, 1143, 1044, 1227, 1045).contains(expected.getErrorCode())) throw expected;
            checks++; outcomes.add(name + ": denied (SQL " + expected.getErrorCode() + ")"); return;
        }
        throw new IllegalStateException("Forbidden operation succeeded: " + name);
    }
    private static String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
    private static void enroll() throws Exception {
        try (Connection connection = admin()) {
            connection.setAutoCommit(false);
            List<String> secrets = new ArrayList<>();
            long started = System.nanoTime();
            try (PreparedStatement select = connection.prepareStatement("SELECT UserID, username, password FROM users FOR UPDATE");
                 ResultSet rows = select.executeQuery();
                 PreparedStatement update = connection.prepareStatement("UPDATE users SET password=?, must_change_password=1 WHERE UserID=? AND password=?")) {
                while (rows.next()) {
                    String oldHash = rows.getString("password");
                    if (PasswordService.isSupportedHash(oldHash)) continue;
                    char[] temporary = PasswordService.generateTemporaryPassword();
                    try {
                        String bcrypt = PasswordService.hashPassword(temporary);
                        update.setString(1, bcrypt); update.setInt(2, rows.getInt("UserID")); update.setString(3, oldHash);
                        if (update.executeUpdate() != 1) throw new SQLException("Enrollment row changed concurrently.");
                        secrets.add("{\"username\":" + quote(rows.getString("username")) + ",\"password\":" + quote(new String(temporary)) + "}");
                    } finally { Arrays.fill(temporary, '\0'); }
                }
                // Parent must DPAPI-protect escrow BEFORE authorizing commit.
                System.out.println("{\"bootstrap\":[" + String.join(",", secrets) + "],\"count\":" + secrets.size() + ",\"duration_ms\":" + ((System.nanoTime()-started)/1_000_000) + "}");
                System.out.flush();
                String response = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)).readLine();
                if (!"COMMIT_ESCROW_SAVED".equals(response)) { connection.rollback(); throw new IllegalStateException("Protected escrow not acknowledged; enrollment rolled back."); }
                connection.commit();
                System.out.println("ENROLLMENT_COMMITTED");
            } catch (Exception failure) { connection.rollback(); throw failure; }
        }
    }
    private static void test() throws Exception {
        System.setProperty("aoop.db.url", url());
        System.setProperty("aoop.db.user", "motorph_m2_app");
        System.setProperty("aoop.db.password", System.getenv("M2_APP_PASSWORD"));
        char[] temporary = PasswordService.generateTemporaryPassword();
        char[] replacement = PasswordService.generateTemporaryPassword();
        String testId = "99001";
        String syntheticAddress = "M2 Synthetic Address " + UUID.randomUUID();
        boolean employeeCreated = false, accountCreated = false;
        try (Connection administrator = admin(); Connection application = DatabaseConfig.getInstance().getConnection()) {
            try (Statement s = administrator.createStatement(); ResultSet r = s.executeQuery("SELECT @@port, DATABASE(), @@require_secure_transport")) {
                r.next(); check(r.getInt(1)==3307 && "aoop_m2".equals(r.getString(2)) && r.getBoolean(3), "C1 isolated identity and TLS-required server");
            }
            try (Statement s = application.createStatement(); ResultSet r = s.executeQuery("SHOW SESSION STATUS LIKE 'Ssl_cipher'")) {
                r.next(); check(r.getString(2)!=null && !r.getString(2).isBlank(), "C1 verified TLS connection negotiated cipher");
            }
            check(!DatabaseHandler.getEmployeeData().isEmpty() && !DatabaseHandler.getSalaryData().isEmpty()
                    && !DatabaseHandler.getAttendanceData().isEmpty() && !DatabaseHandler.getPayrollData().isEmpty(), "C1 existing employee salary attendance payroll reads");
            denied(application, "SELECT 1 FROM mysql.user LIMIT 1", "C1 system-account data");
            denied(application, "SELECT 1 FROM aoop_m2_restore_check.users LIMIT 1", "C1 unrelated-schema data");
            denied(application, "CREATE TABLE aoop_m2.m2_forbidden_probe(id INT)", "C1 schema DDL");
            denied(application, "GRANT SELECT ON aoop_m2.* TO 'motorph_m2_app'@'127.0.0.1'", "C1 grant delegation");
            denied(application, "UPDATE users SET role='HR' WHERE username='99001'", "C1 role-column modification");
            try {
                DriverManager.getConnection(url().replace("sslMode=VERIFY_IDENTITY", "sslMode=DISABLED"), "motorph_m2_app", System.getenv("M2_APP_PASSWORD"));
                throw new IllegalStateException("TLS disabled connection was accepted.");
            } catch (SQLException expected) { checks++; outcomes.add("C1 plaintext transport: denied (SQL " + expected.getErrorCode() + ")"); }
            String configuredUrl = System.getProperty("aoop.db.url");
            System.setProperty("aoop.db.url", "");
            try { DatabaseConfig.getInstance().getConnection(); throw new IllegalStateException("Missing configuration accepted."); }
            catch (PersistenceException expected) { checks++; outcomes.add("C1 missing configuration: denied safely"); }
            finally { System.setProperty("aoop.db.url", configuredUrl); }

            check(!DatabaseHandler.employeeExists(testId, null), "C1 disposable test employee ID unused");
            try (Statement s=administrator.createStatement(); ResultSet r=s.executeQuery("SELECT COUNT(*) FROM users WHERE username='99001'")) {
                r.next(); check(r.getInt(1)==0, "C1 disposable test account ID unused");
            }
            EmpDetails template = DatabaseHandler.getEmployeeData().get(0);
            EmpDetails synthetic = new EmpDetails(testId, "Milestone", "Assurance", "1990-01-01", syntheticAddress, "09123456789", template.getEmployeeStatus(), template.getPosition(), "N/A");
            DatabaseHandler.addEmployee(synthetic);
            employeeCreated = true;
            check(DatabaseHandler.employeeExists(testId, null), "C1 employee insert via restricted account");
            synthetic.setPhoneNumber("09999999999");
            DatabaseHandler.updateEmployee(synthetic);
            check(DatabaseHandler.getEmployeeData().stream().anyMatch(e->testId.equals(e.getEmpID()) && "09999999999".equals(e.getPhoneNumber())), "C1 employee update via restricted account");
            EmpSalaryDetails salary = new EmpSalaryDetails(testId, "Milestone", "Assurance", "M2-SSS", "M2-PH", "M2-TIN", "M2-PAG", 20000, 200, 100, 300, 0, 0);
            DatabaseHandler.updateEmployeeSalary(salary);
            EmpSalaryDetails saved = DatabaseHandler.getSalaryData().stream().filter(e->testId.equals(e.getEmpID())).findFirst().orElseThrow();
            check(saved.getBasicSalary()==20000 && Math.abs(saved.getGrossSemi()-10300)<0.01, "C1 salary update and generated values via restricted account");

            DatabaseHandler.createUser(testId, temporary, "EMPLOYEE", "Milestone", "Assurance");
            accountCreated = true;
            DatabaseHandler.AuthenticationResult pending = DatabaseHandler.authenticate(testId, temporary);
            check(pending!=null && pending.isResetRequired() && pending.getEmployee()==null && pending.getRole()==null, "C2 temporary credential restricted to reset");
            check(DatabaseHandler.authenticate(testId, "1234".toCharArray())==null, "C2 documented default denied");
            check(DatabaseHandler.authenticate(testId, "Incorrect-Password-2026".toCharArray())==null, "C2 incorrect credential denied");
            check(DatabaseHandler.resetPassword(testId, "Incorrect-Password-2026".toCharArray(), replacement)==null, "C2 reset revalidates current credential");
            check(DatabaseHandler.authenticate(testId, temporary).isResetRequired(), "C2 failed reset preserves restriction");
            for (char[] invalid : new char[][]{"short".toCharArray(), "123412341234".toCharArray(), "x".repeat(73).toCharArray()}) {
                try { DatabaseHandler.resetPassword(testId, temporary, invalid); throw new IllegalStateException("Invalid new password accepted."); }
                catch (IllegalArgumentException expected) { checks++; outcomes.add("C2 invalid new password: denied"); }
            }
            DatabaseHandler.AuthenticationResult complete = DatabaseHandler.resetPassword(testId, temporary, replacement);
            check(complete!=null && !complete.isResetRequired() && complete.getEmployee()!=null, "C2 committed reset permits full authentication");
            check(DatabaseHandler.authenticate(testId, temporary)==null, "C2 old temporary credential revoked");
            check(DatabaseHandler.authenticate(testId, replacement)!=null, "C2 new BCrypt credential succeeds");
            String hash1=PasswordService.hashPassword(replacement), hash2=PasswordService.hashPassword(replacement);
            check(!hash1.equals(hash2) && hash1.startsWith("$2b$12$") && PasswordService.verifyPassword(replacement,hash1), "C2 salted cost12 BCrypt independently generated");
            try (Statement s=administrator.createStatement(); ResultSet r=s.executeQuery("SELECT password,must_change_password FROM users WHERE username='99001'")) {
                r.next(); check(PasswordService.isSupportedHash(r.getString(1)) && !r.getBoolean(2), "C2 persisted BCrypt format and reset flag cleared");
            }
            try (Statement s=administrator.createStatement(); ResultSet r=s.executeQuery("SELECT password,must_change_password FROM users WHERE username<>'99001'")) {
                int users=0; boolean clean=true;
                while(r.next()){users++;clean &= PasswordService.isSupportedHash(r.getString(1)) && r.getBoolean(2) && !PasswordService.verifyPassword("1234".toCharArray(),r.getString(1));}
                check(users>0 && clean, "C2 all cloned legacy accounts enrolled without shared default");
            }
            long start=System.nanoTime(); PasswordService.hashPassword(replacement);
            outcomes.add("C2 BCrypt12 hash duration_ms="+((System.nanoTime()-start)/1_000_000));
            DatabaseHandler.deleteEmployee(testId);
            check(!DatabaseHandler.employeeExists(testId,null), "C1 employee delete via restricted account");
        } finally {
            Arrays.fill(temporary,'\0'); Arrays.fill(replacement,'\0');
            try(Connection cleanup=admin(); Statement s=cleanup.createStatement()) {
                if(accountCreated) s.executeUpdate("DELETE FROM users WHERE username='99001'");
                if(employeeCreated) {
                    s.executeUpdate("DELETE FROM employees WHERE EmployeeID=99001");
                    try(PreparedStatement address=cleanup.prepareStatement("DELETE FROM addresses WHERE StreetAddress=?")) {
                        address.setString(1,syntheticAddress); address.executeUpdate();
                    }
                }
            }
        }
        System.out.println("{\"assertions\":"+checks+",\"outcomes\":["+outcomes.stream().map(SecurityAssurance::quote).collect(java.util.stream.Collectors.joining(","))+"]}");
    }
    public static void main(String[] args) throws Exception {
        if(args.length!=1)throw new IllegalArgumentException("Choose enroll or test");
        if("verify-escrow".equals(args[0])) {
            System.setProperty("aoop.db.url",url());
            System.setProperty("aoop.db.user","motorph_m2_app");
            System.setProperty("aoop.db.password",System.getenv("M2_APP_PASSWORD"));
            BufferedReader input=new BufferedReader(new InputStreamReader(System.in,StandardCharsets.UTF_8));
            int verified=0;String line;
            while((line=input.readLine())!=null) {
                String[] fields=line.split("\t",2);
                if(fields.length!=2)throw new IllegalArgumentException("Malformed private escrow input");
                char[] password=fields[1].toCharArray();
                try {
                    DatabaseHandler.AuthenticationResult result=DatabaseHandler.authenticate(fields[0],password);
                    if(result==null || !result.isResetRequired() || result.getEmployee()!=null || result.getRole()!=null)throw new IllegalStateException("Escrow proof mismatch");
                    verified++;
                } finally {Arrays.fill(password,'\0');}
            }
            System.out.println("Protected escrow proofs verified against isolated accounts: "+verified);
        } else if("profile".equals(args[0])) {
            String active=System.getenv("AOOP_DB_URL");
            if(active==null || !active.startsWith("jdbc:mysql://127.0.0.1:3307/aoop_m2?"))throw new IllegalStateException("Wrong profile target");
            try(Connection c=DatabaseConfig.getInstance().getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT @@port,DATABASE(),CURRENT_USER()")) {
                r.next(); if(r.getInt(1)!=3307 || !"aoop_m2".equals(r.getString(2)) || !r.getString(3).startsWith("motorph_m2_app@"))throw new IllegalStateException("Wrong profile identity");
            }
            System.out.println("DPAPI launch profile verified: isolated database, restricted application account, TLS identity verification.");
        } else if("enroll".equals(args[0]))enroll(); else if("test".equals(args[0]))test(); else throw new IllegalArgumentException("Unknown mode");
    }
}
