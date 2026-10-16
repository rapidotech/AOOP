import java.awt.Graphics2D;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import oop_motorph.*;

/** Offline render fixture. Not on the application runtime classpath. Never opens a socket. */
public final class VisualSmoke {
    private static final String URL = "jdbc:mysql://aoop-fixture.invalid/visual";
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Supply screenshot directory");
        File directory = new File(args[0]);
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IllegalStateException("Cannot create output directory");
        System.setProperty("aoop.db.url", URL);
        System.setProperty("aoop.db.user", "synthetic");
        System.setProperty("aoop.db.password", "synthetic-fixture-only");
        System.setProperty("aoop.db.config", new File(directory, "missing-fixture-config.properties").getAbsolutePath());
        System.setProperty("aoop.data.dir", java.nio.file.Files.createTempDirectory("aoop-visual-data-").toString());
        java.util.Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) DriverManager.deregisterDriver(drivers.nextElement());
        DriverManager.registerDriver(new Driver() {
            public boolean acceptsURL(String url) { return URL.equals(url); }
            public Connection connect(String url, Properties info) throws SQLException {
                return acceptsURL(url) ? connection() : null;
            }
            public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) { return new DriverPropertyInfo[0]; }
            public int getMajorVersion() { return 1; }
            public int getMinorVersion() { return 0; }
            public boolean jdbcCompliant() { return false; }
            public Logger getParentLogger() { return Logger.getLogger("aoop.visual.fixture"); }
        });
        Robot robot = new Robot();
        for (String name : new String[]{"login", "profile", "records", "payroll"}) {
            final JFrame[] frame = new JFrame[1];
            SwingUtilities.invokeAndWait(() -> {
                AppTheme.install();
                EmpUserSession.getInstance().setCurrentUser(new EmpDetails("90001", "Sample", "Employee", "1990-01-01", "Synthetic address", "09123456789", "Regular", "HR Manager", "N/A"), name.equals("profile") ? "EMPLOYEE" : "HR");
                frame[0] = switch (name) {
                    case "login" -> new frm_Login();
                    case "profile" -> new frm_EmpProfile();
                    case "records" -> new frm_EmployeesRecords();
                    default -> new frm_EmployeesPayrollProcess();
                };
                frame[0].setVisible(true);
            });
            robot.waitForIdle();
            robot.delay(250);
            for (boolean compact : name.equals("login") ? new boolean[]{false} : new boolean[]{false, true}) {
                SwingUtilities.invokeAndWait(() -> {
                    try {
                        JFrame f = frame[0];
                        if (!name.equals("login")) {
                            f.setMinimumSize(new java.awt.Dimension(0, 0));
                            f.setSize(compact ? 1100 : 1400, compact ? 780 : 950);
                            f.validate();
                        }
                        BufferedImage image = new BufferedImage(f.getContentPane().getWidth(), f.getContentPane().getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D graphics = image.createGraphics();
                        f.getContentPane().printAll(graphics);
                        graphics.dispose();
                        ImageIO.write(image, "png", new File(directory, name + (compact ? "-compact" : "") + ".png"));
                    } catch (Exception ex) { throw new RuntimeException(ex); }
                });
            }
            SwingUtilities.invokeAndWait(() -> frame[0].dispose());
            System.out.println("Rendered synthetic " + name);
        }
        EmpUserSession.getInstance().clearSession();
        System.exit(0);
    }
    private static Connection connection() {
        return (Connection) Proxy.newProxyInstance(VisualSmoke.class.getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> {
            if (m.getName().equals("prepareStatement") || m.getName().equals("createStatement")) {
                if (a != null && a.length > 0 && a[0] instanceof String sql && !sql.stripLeading().toUpperCase(java.util.Locale.ROOT).startsWith("SELECT")) throw new SQLException("Fixture refuses writes");
                return statement(a != null && a.length > 0 && a[0] instanceof String ? (String) a[0] : "");
            }
            if (m.getName().equals("isValid")) return true;
            return defaultValue(m.getReturnType());
        });
    }
    private static PreparedStatement statement(String sql) {
        return (PreparedStatement) Proxy.newProxyInstance(VisualSmoke.class.getClassLoader(), new Class<?>[]{PreparedStatement.class}, (p, m, a) -> {
            if (m.getName().equals("executeQuery")) return result(sql);
            if (m.getName().startsWith("execute")) throw new SQLException("Fixture refuses writes");
            return defaultValue(m.getReturnType());
        });
    }
    private static ResultSet result(String sql) {
        java.util.Map<String, Object> row = new java.util.HashMap<>();
        if (sql.contains("FROM employees e") || sql.contains("FROM Payroll p")) {
            row.put("EmployeeID", "90001"); row.put("FirstName", "Sample"); row.put("LastName", "Employee");
            row.put("Birthday", "1990-01-01"); row.put("StreetAddress", "Synthetic address");
            row.put("City", "Sample city"); row.put("Region", "Sample region"); row.put("PhoneNumber", "09123456789");
            row.put("StatusName", "Regular"); row.put("PositionName", "HR Manager"); row.put("DepartmentName", "Human Resources");
            row.put("total_earnings", 25000.0); row.put("net_pay", 22000.0);
            row.put("PayrollPeriodStartDate", java.sql.Date.valueOf("2026-10-01"));
        }
        final boolean[] consumed = {false};
        return (ResultSet) Proxy.newProxyInstance(VisualSmoke.class.getClassLoader(), new Class<?>[]{ResultSet.class}, (p, m, a) -> {
            if (m.getName().equals("next")) {
                boolean available = !consumed[0] && !row.isEmpty(); consumed[0] = true; return available;
            }
            Object value = a != null && a.length > 0 ? row.get(String.valueOf(a[0])) : null;
            if (m.getName().equals("getString")) return value == null ? null : value.toString();
            if (m.getName().equals("getDouble")) return value instanceof Number ? ((Number) value).doubleValue() : 0.0;
            if (m.getName().equals("getDate")) return value instanceof java.sql.Date ? value : null;
            return defaultValue(m.getReturnType());
        });
    }
    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) return null;
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0;
        if (type == float.class) return 0f;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        return '\0';
    }
}
