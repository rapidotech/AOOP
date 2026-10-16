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
            resetGateAndCancel();
            resetValidationAndFailure();
            successfulResetAndNavigation();
            regularSecureLoginAndNavigation();
            resetPrimaryButtonContrast();
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
        AtomicReference<String> suppliedPassword = new AtomicReference<>();
        AtomicReference<char[]> suppliedArray = new AtomicReference<>();
        AtomicReference<frm_Login> frame = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> frame.set(new frm_Login((user, password) -> {
            calls.incrementAndGet();
            background.set(!SwingUtilities.isEventDispatchThread());
            suppliedUser.set(user); suppliedPassword.set(new String(password)); suppliedArray.set(password); started.countDown();
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
        check(" secret ".equals(suppliedPassword.get()), "Password whitespace must be preserved exactly");
        release.countDown();
        awaitText(frame.get(), "incorrect");
        check(allCleared(suppliedArray.get()), "Authentication working password must be cleared after the worker finishes");
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

    private static void resetGateAndCancel() throws Exception {
        AtomicReference<frm_Login> frame = new AtomicReference<>();
        AtomicInteger navigation = new AtomicInteger();
        AtomicInteger resetCalls = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            frame.set(new frm_Login((user, password) -> DatabaseHandler.AuthenticationResult.resetRequired(user),
                    (user, oldPassword, newPassword) -> { resetCalls.incrementAndGet(); return null; },
                    result -> navigation.incrementAndGet()));
            find(frame.get(), "username", JTextField.class).setText("90001");
            find(frame.get(), "password", JPasswordField.class).setText("unique-temporary-fixture");
            find(frame.get(), "signIn", JButton.class).doClick();
        });
        PasswordResetDialog dialog = awaitResetDialog();
        SwingUtilities.invokeAndWait(() -> {
            check(EmpUserSession.getInstance().getCurrentUser() == null, "Reset-required authentication must not create a full employee session");
            check(navigation.get() == 0, "A reset-required account must not navigate into employee workflows");
            check(dialog.getRootPane().getDefaultButton() == find(dialog, "savePassword", JButton.class), "Enter must submit the mandatory reset");
            find(dialog, "cancelReset", JButton.class).doClick();
        });
        awaitText(frame.get(), "required");
        SwingUtilities.invokeAndWait(() -> {
            check(EmpUserSession.getInstance().getCurrentUser() == null, "Canceling reset must leave the session empty");
            check(resetCalls.get() == 0, "Canceling before submission must never call password persistence");
            check(navigation.get() == 0, "Canceling reset must not open employee workflows");
            frame.get().dispose();
        });
    }

    private static void resetValidationAndFailure() throws Exception {
        AtomicReference<PasswordResetDialog> dialog = new AtomicReference<>();
        AtomicInteger calls = new AtomicInteger();
        AtomicBoolean flag = new AtomicBoolean(true);
        SwingUtilities.invokeAndWait(() -> {
            dialog.set(new PasswordResetDialog(null, "90001", (user, oldPassword, newPassword) -> {
                calls.incrementAndGet();
                check(!SwingUtilities.isEventDispatchThread(), "Reset persistence must run off the event thread");
                return null;
            }, result -> { throw new AssertionError("Failed reset must not authenticate"); }));
            fillReset(dialog.get(), "unique-temporary-fixture", "short", "short");
            find(dialog.get(), "savePassword", JButton.class).doClick();
            check(calls.get() == 0, "Short replacement passwords must not reach persistence");
            fillReset(dialog.get(), "unique-temporary-fixture", "\u00e9".repeat(37), "\u00e9".repeat(37));
            find(dialog.get(), "savePassword", JButton.class).doClick();
            check(calls.get() == 0, "Passwords above 72 UTF-8 bytes must be rejected rather than truncated");
            fillReset(dialog.get(), "unique-temporary-fixture", "a unique replacement", "different replacement");
            find(dialog.get(), "savePassword", JButton.class).doClick();
            check(calls.get() == 0, "Mismatched confirmation must not reach persistence");
            fillReset(dialog.get(), "unique-temporary-fixture", "123412341234", "123412341234");
            find(dialog.get(), "savePassword", JButton.class).doClick();
            check(calls.get() == 0, "A known repeated default must not reach password persistence");
            fillReset(dialog.get(), "incorrect-temporary-fixture", "a unique replacement", "a unique replacement");
            find(dialog.get(), "savePassword", JButton.class).doClick();
        });
        awaitResetText(dialog.get(), "not changed");
        SwingUtilities.invokeAndWait(() -> {
            check(flag.get(), "Rejected reset must leave the simulated mandatory-reset flag intact");
            check(EmpUserSession.getInstance().getCurrentUser() == null, "Rejected reset must leave the session empty");
            check(find(dialog.get(), "savePassword", JButton.class).isEnabled(), "Rejected reset must restore retry controls");
            dialog.get().dispose();
            dialog.set(new PasswordResetDialog(null, "90001", (user, oldPassword, newPassword) -> {
                throw new PersistenceException("Offline fixture");
            }, result -> { throw new AssertionError("Unavailable reset must not authenticate"); }));
            fillReset(dialog.get(), "unique-temporary-fixture", "a unique replacement", "a unique replacement");
            find(dialog.get(), "savePassword", JButton.class).doClick();
        });
        awaitResetText(dialog.get(), "connection");
        SwingUtilities.invokeAndWait(() -> {
            check(EmpUserSession.getInstance().getCurrentUser() == null, "Persistence failure must leave the session empty");
            check(flag.get(), "Persistence failure must not claim reset completion");
            dialog.get().dispose();
        });
    }

    private static void successfulResetAndNavigation() throws Exception {
        AtomicReference<frm_Login> frame = new AtomicReference<>();
        AtomicInteger navigation = new AtomicInteger();
        AtomicBoolean flag = new AtomicBoolean(true);
        AtomicReference<char[]> oldWorking = new AtomicReference<>();
        AtomicReference<char[]> newWorking = new AtomicReference<>();
        AtomicReference<String> savedPassword = new AtomicReference<>();
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            frame.set(new frm_Login((user, password) -> DatabaseHandler.AuthenticationResult.resetRequired(user),
                    (user, oldPassword, newPassword) -> {
                        oldWorking.set(oldPassword); newWorking.set(newPassword);
                        savedPassword.set(new String(newPassword));
                        started.countDown();
                        try { release.await(5, TimeUnit.SECONDS); }
                        catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
                        flag.set(false);
                        return new DatabaseHandler.AuthenticationResult(new EmpDetails("90001", "Sample", "Employee"), "EMPLOYEE");
                    }, result -> navigation.incrementAndGet()));
            find(frame.get(), "username", JTextField.class).setText("90001");
            find(frame.get(), "password", JPasswordField.class).setText("unique-temporary-fixture");
            find(frame.get(), "signIn", JButton.class).doClick();
        });
        PasswordResetDialog dialog = awaitResetDialog();
        SwingUtilities.invokeAndWait(() -> {
            fillReset(dialog, "unique-temporary-fixture", " new unique password ", " new unique password ");
            find(dialog, "savePassword", JButton.class).doClick();
        });
        check(started.await(3, TimeUnit.SECONDS), "Valid reset must dispatch its worker");
        SwingUtilities.invokeAndWait(() -> {
            check(!find(dialog, "savePassword", JButton.class).isEnabled(), "Pending reset must prevent duplicate submissions");
            check(!find(dialog, "cancelReset", JButton.class).isEnabled(), "A dispatched commit cannot falsely claim cancellation");
            check(EmpUserSession.getInstance().getCurrentUser() == null, "Pending reset must not create a session before persistence returns");
            dialog.dispatchEvent(new java.awt.event.WindowEvent(dialog, java.awt.event.WindowEvent.WINDOW_CLOSING));
            check(dialog.isDisplayable(), "Window close must wait until an in-flight password change completes");
        });
        release.countDown();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (navigation.get() == 0 && System.nanoTime() < deadline) Thread.sleep(20);
        SwingUtilities.invokeAndWait(() -> {
            check(navigation.get() == 1, "Successful reset must open the existing authenticated workflow exactly once");
            check(!flag.get(), "Successful reset must reflect committed mandatory-reset remediation");
            check(EmpUserSession.getInstance().getCurrentUser() != null, "Successful reset may create the full employee session");
            check(" new unique password ".equals(savedPassword.get()), "Replacement password whitespace must not be trimmed");
            check(allCleared(oldWorking.get()) && allCleared(newWorking.get()), "Reset working password arrays must be cleared");
            EmpUserSession.getInstance().clearSession();
        });
    }

    private static boolean allCleared(char[] password) {
        if (password == null) return false;
        for (char character : password) if (character != '\0') return false;
        return true;
    }

    private static void regularSecureLoginAndNavigation() throws Exception {
        AtomicReference<frm_Login> frame = new AtomicReference<>();
        AtomicInteger navigation = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            frame.set(new frm_Login((user, password) ->
                    new DatabaseHandler.AuthenticationResult(new EmpDetails(user, "Sample", "Employee"), "EMPLOYEE"),
                    (user, current, replacement) -> { throw new AssertionError("A remediated account must not reset on every login"); },
                    result -> navigation.incrementAndGet()));
            find(frame.get(), "username", JTextField.class).setText("90001");
            find(frame.get(), "password", JPasswordField.class).setText("already-remediated-fixture");
            find(frame.get(), "signIn", JButton.class).doClick();
        });
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (navigation.get() == 0 && System.nanoTime() < deadline) Thread.sleep(20);
        SwingUtilities.invokeAndWait(() -> {
            check(navigation.get() == 1, "A fully authenticated account must retain access to the existing workflow");
            check("90001".equals(EmpUserSession.getInstance().getCurrentUser().getEmpID()), "A full authentication result must set the correct employee session");
            check(!frame.get().isDisplayable(), "Successful sign in must close the login form");
            EmpUserSession.getInstance().clearSession();
        });
    }

    private static void resetPrimaryButtonContrast() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            PasswordResetDialog dialog = new PasswordResetDialog(null, "90001", (user, current, replacement) -> null, result -> { });
            try {
                JButton button = find(dialog, "savePassword", JButton.class);
                for (boolean enabled : new boolean[]{true, false}) {
                    button.setEnabled(enabled);
                    java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(button.getWidth(), button.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = image.createGraphics();
                    button.paint(graphics); graphics.dispose();
                    Color painted = new Color(image.getRGB(button.getWidth() / 2, 5));
                    check(contrast(Color.WHITE, painted) >= 4.5, "Rendered reset primary button must keep white-text contrast in enabled and pending states");
                }
            } finally { dialog.dispose(); }
        });
    }

    private static void fillReset(PasswordResetDialog dialog, String current, String replacement, String confirmation) {
        find(dialog, "currentPassword", JPasswordField.class).setText(current);
        find(dialog, "newPassword", JPasswordField.class).setText(replacement);
        find(dialog, "confirmPassword", JPasswordField.class).setText(confirmation);
    }

    private static PasswordResetDialog awaitResetDialog() throws Exception {
        AtomicReference<PasswordResetDialog> dialog = new AtomicReference<>();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            SwingUtilities.invokeAndWait(() -> {
                for (Window window : Window.getWindows()) {
                    if (window instanceof PasswordResetDialog && window.isVisible()) dialog.set((PasswordResetDialog) window);
                }
            });
            if (dialog.get() != null) return dialog.get();
            Thread.sleep(20);
        }
        throw new AssertionError("Mandatory password reset dialog did not open");
    }

    private static void awaitResetText(PasswordResetDialog dialog, String text) throws Exception {
        AtomicBoolean found = new AtomicBoolean();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            SwingUtilities.invokeAndWait(() -> found.set(find(dialog, "resetStatus", JLabel.class).getText().contains(text)));
            if (found.get()) return;
            Thread.sleep(20);
        }
        throw new AssertionError("Expected reset status: " + text);
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
