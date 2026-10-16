package oop_motorph;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.*;

/** Restricted account recovery: no employee session exists until persistence succeeds. */
public final class PasswordResetDialog extends JDialog {
    @FunctionalInterface
    interface ResetAction {
        DatabaseHandler.AuthenticationResult reset(String username, char[] currentPassword, char[] newPassword);
    }

    private final String username;
    private final ResetAction resetter;
    private final Consumer<DatabaseHandler.AuthenticationResult> onSuccess;
    private final JPasswordField current = passwordField("currentPassword");
    private final JPasswordField replacement = passwordField("newPassword");
    private final JPasswordField confirmation = passwordField("confirmPassword");
    private final JButton save = new PrimaryButton("Save password and sign in");
    private final JButton cancel = new JButton("Back to sign in");
    private final JLabel status = new JLabel(" ");
    private final JCheckBox showPasswords = new JCheckBox("Show passwords");
    private final char echo;
    private boolean saving;
    private Timer retryTimer;
    private long retryDeadlineNanos;

    PasswordResetDialog(JFrame owner, String username, ResetAction resetter,
            Consumer<DatabaseHandler.AuthenticationResult> onSuccess) {
        super(owner, "Change your password \u2014 MotorPH", true);
        this.username = username;
        this.resetter = resetter;
        this.onSuccess = onSuccess;
        echo = current.getEchoChar();
        setName("passwordReset");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) { cancelReset(); }
        });
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 10, 0);
        JLabel heading = new JLabel("Change your password");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(AppTheme.INK);
        body.add(heading, c);
        c.gridy++;
        JLabel reason = new JLabel("<html>Your account needs a new password before you can continue.<br>Use the temporary password provided by your administrator.</html>");
        reason.setForeground(AppTheme.MUTED);
        reason.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.insets = new Insets(0, 0, 20, 0); body.add(reason, c);
        addField(body, c, "Current / temporary password", 'T', current);
        addField(body, c, "New password", 'N', replacement);
        addField(body, c, "Confirm new password", 'C', confirmation);
        c.gridy++; c.insets = new Insets(0, 0, 10, 0);
        showPasswords.setOpaque(false);
        showPasswords.setMnemonic('H');
        showPasswords.addActionListener(event -> {
            char value = showPasswords.isSelected() ? 0 : echo;
            current.setEchoChar(value); replacement.setEchoChar(value); confirmation.setEchoChar(value);
        });
        body.add(showPasswords, c);
        c.gridy++;
        JLabel policy = new JLabel("<html>At least 12 characters; up to 72 UTF-8 bytes.<br>Choose a new password, not a temporary or default password.</html>");
        policy.setName("passwordPolicy");
        policy.setForeground(AppTheme.MUTED);
        policy.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.insets = new Insets(0, 0, 12, 0); body.add(policy, c);
        c.gridy++;
        status.setName("resetStatus");
        status.setPreferredSize(new Dimension(420, 58));
        status.getAccessibleContext().setAccessibleName("Password change status");
        body.add(status, c);
        c.gridy++;
        JPanel actions = new JPanel(new BorderLayout(12, 0));
        actions.setOpaque(false);
        save.setName("savePassword"); save.setMnemonic('S');
        save.setBackground(AppTheme.GREEN); save.setForeground(Color.WHITE);
        save.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        cancel.setName("cancelReset");
        cancel.setBackground(new Color(229, 231, 235)); cancel.setForeground(AppTheme.INK);
        save.addActionListener(event -> savePassword());
        cancel.addActionListener(event -> cancelReset());
        actions.add(cancel, BorderLayout.WEST); actions.add(save, BorderLayout.CENTER);
        c.insets = new Insets(0, 0, 0, 0); body.add(actions, c);
        setContentPane(body);
        AppTheme.apply(body);
        getRootPane().setDefaultButton(save);
        getRootPane().registerKeyboardAction(event -> cancelReset(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private static JPasswordField passwordField(String name) {
        JPasswordField field = new JPasswordField(28);
        field.setName(name);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(AppTheme.LINE),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        return field;
    }

    private static void addField(JPanel body, GridBagConstraints c, String text, char mnemonic, JPasswordField field) {
        c.gridy++; c.insets = new Insets(0, 0, 6, 0);
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(AppTheme.INK);
        label.setLabelFor(field); label.setDisplayedMnemonic(mnemonic);
        body.add(label, c);
        c.gridy++; c.insets = new Insets(0, 0, 16, 0); body.add(field, c);
    }

    private void savePassword() {
        if (saving || (retryTimer != null && retryTimer.isRunning())) return;
        final char[] oldPassword = current.getPassword();
        final char[] newPassword = replacement.getPassword();
        char[] confirmed = confirmation.getPassword();
        try {
            if (oldPassword.length == 0) throw new IllegalArgumentException("Enter your current or temporary password.");
            PasswordService.validateNewPassword(newPassword);
            if (!Arrays.equals(newPassword, confirmed)) throw new IllegalArgumentException("New passwords do not match. Try again.");
            if (Arrays.equals(oldPassword, newPassword)) throw new IllegalArgumentException("Choose a new password that differs from your temporary password.");
        } catch (IllegalArgumentException ex) {
            Arrays.fill(oldPassword, '\0'); Arrays.fill(newPassword, '\0');
            setStatus(ex.getMessage(), true);
            return;
        } finally {
            Arrays.fill(confirmed, '\0');
        }
        setSaving(true);
        setStatus("Saving your new password\u2026", false);
        new SwingWorker<DatabaseHandler.AuthenticationResult, Void>() {
            @Override protected DatabaseHandler.AuthenticationResult doInBackground() {
                try { return resetter.reset(username, oldPassword, newPassword); }
                finally { Arrays.fill(oldPassword, '\0'); Arrays.fill(newPassword, '\0'); }
            }
            @Override protected void done() {
                setSaving(false);
                if (!isDisplayable()) return;
                try {
                    DatabaseHandler.AuthenticationResult result = get();
                    if (result == null || result.isResetRequired() || result.getEmployee() == null || result.getRole() == null) {
                        EmpUserSession.getInstance().clearSession();
                        current.setText("");
                        setStatus("Password not changed. Check the current password and retry.", true);
                        current.requestFocusInWindow();
                        return;
                    }
                    clearFields();
                    dispose();
                    onSuccess.accept(result);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    EmpUserSession.getInstance().clearSession();
                    setStatus("Password change interrupted. Please try again.", true);
                } catch (ExecutionException ex) {
                    EmpUserSession.getInstance().clearSession();
                    if (ex.getCause() instanceof AuthenticationThrottledException) {
                        startRetryDelay(((AuthenticationThrottledException) ex.getCause()).getRetryAfterSeconds());
                    } else if (ex.getCause() instanceof IllegalArgumentException) setStatus(ex.getCause().getMessage(), true);
                    else setStatus("Password not changed. Check the connection and retry.", true);
                }
            }
        }.execute();
    }

    private void setSaving(boolean busy) {
        saving = busy;
        current.setEnabled(!busy); replacement.setEnabled(!busy); confirmation.setEnabled(!busy);
        showPasswords.setEnabled(!busy); save.setEnabled(!busy); cancel.setEnabled(!busy);
        save.setText(busy ? "Saving password\u2026" : "Save password and sign in");
        setCursor(Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private void cancelReset() {
        // A dispatched commit cannot be canceled honestly; wait for its result first.
        if (saving) return;
        EmpUserSession.getInstance().clearSession();
        clearFields();
        dispose();
    }

    private void clearFields() { current.setText(""); replacement.setText(""); confirmation.setText(""); }

    private void startRetryDelay(long retryAfterSeconds) {
        retryDeadlineNanos = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(Math.max(1, Math.min(retryAfterSeconds, 86400)));
        if (retryTimer != null) retryTimer.stop();
        retryTimer = new Timer(250, event -> updateRetryDelay());
        updateRetryDelay(); retryTimer.start();
    }

    private void updateRetryDelay() {
        long remaining = retryDeadlineNanos - System.nanoTime();
        if (remaining <= 0) {
            retryTimer.stop(); save.setEnabled(true); save.setText("Save password and sign in");
            setStatus("You can try changing your password again.", false);
            return;
        }
        long seconds = (remaining + 999999999L) / 1000000000L;
        save.setEnabled(false); save.setText("Try again (" + seconds + " s)");
        setStatus("Password change could not be completed. Try again in " + seconds + " seconds.", true);
    }

    @Override public void dispose() {
        if (retryTimer != null) retryTimer.stop();
        super.dispose();
    }

    private void setStatus(String message, boolean error) {
        status.setForeground(error ? new Color(185, 28, 28) : AppTheme.MUTED);
        status.setText("<html><div style='width: 305px'>" + message.replace("&", "&amp;").replace("<", "&lt;") + "</div></html>");
        status.getAccessibleContext().setAccessibleDescription(message);
    }

    /** Nimbus otherwise lightens custom button colors below readable white-text contrast. */
    private static final class PrimaryButton extends JButton {
        PrimaryButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setContentAreaFilled(false);
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D paint = (Graphics2D) graphics.create();
            try {
                paint.setColor(isEnabled() ? getBackground() : new Color(75, 85, 99));
                paint.fillRect(0, 0, getWidth(), getHeight());
                paint.setFont(getFont());
                paint.setColor(Color.WHITE);
                FontMetrics metrics = paint.getFontMetrics();
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;
                int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                javax.swing.plaf.basic.BasicGraphicsUtils.drawStringUnderlineCharAt(paint, getText(),
                        getDisplayedMnemonicIndex(), x, y);
                if (isFocusOwner()) paint.drawRect(4, 4, getWidth() - 9, getHeight() - 9);
            } finally { paint.dispose(); }
        }
    }
}
