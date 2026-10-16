package oop_motorph;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import javax.swing.table.*;

/** Offline Swing regressions. Auth is injected; no JDBC driver or live service is used. */
public final class UiRegressionTest {
    private static int assertions;

    public static void main(String[] args) throws Exception {
        try {
            for (UIManager.LookAndFeelInfo look : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(look.getName())) { UIManager.setLookAndFeel(look.getClassName()); break; }
            }
            lazyLoginAndKeyboard();
            asynchronousAuthentication();
            unavailableAndRetry();
            sortedPayrollMapping();
            failedPayrollSavePreservesDisplay();
            readOnlyControlsStayReadable();
            check(contrast(Color.WHITE, AppTheme.GREEN) >= 4.5, "Primary button contrast must meet 4.5:1");
            System.out.println("UI regression checks passed: " + assertions + " assertions; injected auth only.");
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                for (Window window : Window.getWindows()) window.dispose();
            });
        }
    }

    private static void lazyLoginAndKeyboard() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<frm_Login> frame = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> frame.set(new frm_Login((user, hash) -> { calls.incrementAndGet(); return null; })));
        SwingUtilities.invokeAndWait(() -> {
            frm_Login login = frame.get();
            check(calls.get() == 0, "Opening login must not access credentials or the database");
            JButton signIn = find(login, "signIn", JButton.class);
            JTextField username = find(login, "username", JTextField.class);
            JPasswordField password = find(login, "password", JPasswordField.class);
            check(login.getRootPane().getDefaultButton() == signIn, "Enter must activate Sign in");
            check(signIn.isFocusPainted(), "Keyboard focus must remain visible");
            login.validate();
            JLabel company = find(login, "companyName", JLabel.class);
            check(company.getWidth() >= company.getFontMetrics(company.getFont()).stringWidth(company.getText()),
                    "The full legal company name must fit its rendered label bounds");
            check(username.getAccessibleContext().getAccessibleName().contains("Username"), "Username must have an associated label");
            check(password.getAccessibleContext().getAccessibleName().contains("Password"), "Password must have an associated label");
            username.postActionEvent();
            check(calls.get() == 0, "Empty credentials must never reach authentication");
            check(find(login, "loginStatus", JLabel.class).getText().contains("Enter both"), "Missing credentials need a recovery message");
            login.dispose();
        });
    }

    private static void asynchronousAuthentication() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean background = new AtomicBoolean();
        AtomicInteger calls = new AtomicInteger();
        AtomicReference<String> suppliedUser = new AtomicReference<>();
        AtomicReference<String> suppliedHash = new AtomicReference<>();
        AtomicReference<frm_Login> frame = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> frame.set(new frm_Login((user, hash) -> {
            calls.incrementAndGet();
            background.set(!SwingUtilities.isEventDispatchThread());
            suppliedUser.set(user); suppliedHash.set(hash); started.countDown();
            try { release.await(5, TimeUnit.SECONDS); }
            catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
            return null;
        })));
        SwingUtilities.invokeAndWait(() -> {
            find(frame.get(), "username", JTextField.class).setText(" 90001 ");
            JPasswordField password = find(frame.get(), "password", JPasswordField.class);
            password.setText(" secret ");
            password.postActionEvent();
        });
        check(started.await(3, TimeUnit.SECONDS), "Password Enter must start authentication");
        SwingUtilities.invokeAndWait(() -> {
            check(background.get(), "Database authentication must run off the Swing event thread");
            check(!find(frame.get(), "signIn", JButton.class).isEnabled(), "Sign in must disable duplicate submission");
            check(!find(frame.get(), "username", JTextField.class).isEnabled(), "Inputs must show the pending state");
            find(frame.get(), "password", JPasswordField.class).postActionEvent();
            check(calls.get() == 1, "Repeated Enter while pending must not start another request");
        });
        check("90001".equals(suppliedUser.get()), "Username whitespace may be normalized");
        check(frm_Login.hashPassword(" secret ").equals(suppliedHash.get()), "Password whitespace must be preserved exactly");
        release.countDown();
        awaitText(frame.get(), "incorrect");
        SwingUtilities.invokeAndWait(() -> {
            check(find(frame.get(), "signIn", JButton.class).isEnabled(), "Invalid credentials must restore retry controls");
            check(find(frame.get(), "password", JPasswordField.class).getPassword().length == 0, "Invalid password must be cleared");
            frame.get().dispose();
        });
    }

    private static void unavailableAndRetry() throws Exception {
        AtomicReference<frm_Login> frame = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            frame.set(new frm_Login((user, hash) -> { throw new PersistenceException("Offline test"); }));
            find(frame.get(), "username", JTextField.class).setText("90001");
            find(frame.get(), "password", JPasswordField.class).setText("password");
            find(frame.get(), "signIn", JButton.class).doClick();
        });
        awaitText(frame.get(), "unavailable");
        SwingUtilities.invokeAndWait(() -> {
            check(find(frame.get(), "signIn", JButton.class).isEnabled(), "Unavailable authentication must support retry");
            check(find(frame.get(), "loginStatus", JLabel.class).getText().contains("connection"), "Unavailable and invalid credentials must have distinct messages");
            check(EmpUserSession.getInstance().getCurrentUser() == null, "Failed authentication must not create a session");
            frame.get().dispose();
        });
    }

    private static void sortedPayrollMapping() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            DefaultTableModel model = new DefaultTableModel(DatabaseHandler.getPayrollColumnNames(), 0);
            Object[] first = new Object[15]; first[0] = "90002"; first[1] = "Zed Sample"; first[13] = "22000.00";
            Object[] second = new Object[15]; second[0] = "90001"; second[1] = "Ada Sample"; second[13] = "11000.00";
            model.addRow(first); model.addRow(second);
            JTable table = new JTable(model);
            table.setAutoCreateRowSorter(true);
            table.getRowSorter().toggleSortOrder(1);
            String summary = frm_EmployeesPayrollProcess.payrollSelectionSummary(table, 0);
            check(summary.contains("90001") && summary.contains("Ada Sample") && summary.contains("11000.00"),
                    "Payroll actions must use visible selection's model employee ID, name and net pay");
            check(!summary.contains("90002"), "Sorted payroll selections must not read another employee");
        });
    }

    private static void failedPayrollSavePreservesDisplay() throws Exception {
        String previousDirectory = System.getProperty("aoop.data.dir");
        Path directory = Files.createTempDirectory("motorph-ui-ledger-test-");
        System.setProperty("aoop.data.dir", directory.toString());
        EmpUserSession session = EmpUserSession.getInstance();
        EmpDetails previousUser = session.getCurrentUser();
        String previousRole = session.getRole();
        try {
            String[] payroll = new String[22];
            java.util.Arrays.fill(payroll, "0");
            payroll[0] = "TX-TEST-1"; payroll[1] = "90001";
            payroll[2] = "Sample"; payroll[3] = "Employee";
            payroll[4] = "2026-01-01"; payroll[5] = "2026-01-15"; payroll[21] = "Pending";
            CSVHandler.saveUpdatedPayrollData(Collections.singletonList(payroll));
            SwingUtilities.invokeAndWait(() -> {
                session.setCurrentUser(new EmpDetails("90001", "Sample", "Employee"), "HR");
                frm_EmployeesPayrollPaid frame = new frm_EmployeesPayrollPaid();
                try {
                    java.lang.reflect.Field field = frm_EmployeesPayrollPaid.class.getDeclaredField("tbl_Payroll");
                    field.setAccessible(true);
                    JTable table = (JTable) field.get(frame);
                    check(table.getRowCount() == 1, "Local payroll fixture must load into the approval table");
                    table.setRowSelectionInterval(0, 0);
                    // Simulate a record removed by another writer between display and Save.
                    CSVHandler.saveUpdatedPayrollData(Collections.emptyList());
                    Method action = frm_EmployeesPayrollPaid.class.getDeclaredMethod("changePayrollStatus", String.class, String.class);
                    action.setAccessible(true);
                    boolean failed = false;
                    try { action.invoke(frame, "Pending", "Verified"); }
                    catch (InvocationTargetException ex) { failed = ex.getCause() instanceof PersistenceException; }
                    check(failed, "A stale payroll row must report failed persistence");
                    check("Pending".equals(table.getValueAt(0, 21)), "Failed persistence must leave the displayed payroll status unchanged");
                } catch (ReflectiveOperationException ex) {
                    throw new AssertionError(ex);
                } finally {
                    frame.dispose();
                }
            });
        } finally {
            if (previousDirectory == null) System.clearProperty("aoop.data.dir");
            else System.setProperty("aoop.data.dir", previousDirectory);
            if (previousUser == null) session.clearSession();
            else session.setCurrentUser(previousUser, previousRole);
        }
    }

    private static void readOnlyControlsStayReadable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JComboBox<String> combo = new JComboBox<>(new String[]{"Regular"});
            combo.setEnabled(false);
            AppTheme.apply(combo);
            Component rendered = combo.getRenderer().getListCellRendererComponent(new JList<String>(), "Regular", -1, false, false);
            // Basic/Nimbus delegates reset these values while painting a disabled combo.
            rendered.setEnabled(false);
            rendered.setForeground(Color.LIGHT_GRAY);
            check(rendered.isEnabled(), "Read-only values must use the legible enabled text renderer");
            check(AppTheme.INK.equals(rendered.getForeground()), "Disabled combo rendering must preserve the shared ink color");
            check(contrast(rendered.getForeground(), AppTheme.SURFACE) >= 4.5, "Read-only value contrast must meet 4.5:1");
            check(!combo.isEnabled(), "Readable rendering must preserve the disabled interaction state");
            JLabel phone = new JLabel("Phone Number:");
            AppTheme.apply(phone);
            check("Phone:".equals(phone.getText()), "The phone field label must fit its existing narrow slot");
        });
    }

    private static void awaitText(frm_Login frame, String text) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        AtomicBoolean found = new AtomicBoolean();
        while (System.nanoTime() < deadline) {
            SwingUtilities.invokeAndWait(() -> found.set(find(frame, "loginStatus", JLabel.class).getText().contains(text)));
            if (found.get()) return;
            Thread.sleep(20);
        }
        throw new AssertionError("Expected login status: " + text);
    }

    private static <T extends Component> T find(Container parent, String name, Class<T> type) {
        for (Component component : parent.getComponents()) {
            if (name.equals(component.getName()) && type.isInstance(component)) return type.cast(component);
            if (component instanceof Container) {
                T child = findOrNull((Container) component, name, type);
                if (child != null) return child;
            }
        }
        throw new AssertionError("Missing UI control: " + name);
    }

    private static <T extends Component> T findOrNull(Container parent, String name, Class<T> type) {
        for (Component component : parent.getComponents()) {
            if (name.equals(component.getName()) && type.isInstance(component)) return type.cast(component);
            if (component instanceof Container) {
                T child = findOrNull((Container) component, name, type);
                if (child != null) return child;
            }
        }
        return null;
    }

    private static double contrast(Color first, Color second) {
        double a = luminance(first), b = luminance(second);
        return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
    }

    private static double luminance(Color color) {
        double[] channels = {color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d};
        for (int i = 0; i < channels.length; i++) channels[i] = channels[i] <= 0.04045
                ? channels[i] / 12.92 : Math.pow((channels[i] + 0.055) / 1.055, 2.4);
        return channels[0] * 0.2126 + channels[1] * 0.7152 + channels[2] * 0.0722;
    }

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
}
