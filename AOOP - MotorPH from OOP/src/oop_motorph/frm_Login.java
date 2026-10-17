package oop_motorph;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javax.swing.*;


public class frm_Login extends javax.swing.JFrame {

    private final BiFunction<String, char[], DatabaseHandler.AuthenticationResult> authenticator;
    private final PasswordResetDialog.ResetAction passwordResetter;
    private final Consumer<DatabaseHandler.AuthenticationResult> authenticatedNavigation;
    private final JLabel loginStatus = new JLabel("Enter your username and password.");
    private boolean signingIn;
    private char passwordEcho;
    private Timer retryTimer;
    private long retryDeadlineNanos;

    public frm_Login() {
        this(DatabaseHandler::authenticate);
    }

    // Package-private injection keeps UI tests independent of any database.
    frm_Login(BiFunction<String, char[], DatabaseHandler.AuthenticationResult> authenticator) {
        this(authenticator, DatabaseHandler::resetPassword, null);
    }

    frm_Login(BiFunction<String, char[], DatabaseHandler.AuthenticationResult> authenticator,
            PasswordResetDialog.ResetAction passwordResetter,
            Consumer<DatabaseHandler.AuthenticationResult> authenticatedNavigation) {
        this.authenticator = authenticator;
        this.passwordResetter = passwordResetter;
        this.authenticatedNavigation = authenticatedNavigation;
        EmpUserSession.getInstance().clearSession();
        AppTheme.install();
        initComponents();
        configureLogin();
        setLocationRelativeTo(null);
        setResizable(true);
        setTitle("Welcome to MotorPH");
        AppTheme.finish(this);
    }

    /** Source-owned authentication composition; the generated fixed layout is retired. */
    private void configureLogin() {
        passwordEcho = jPasswordField.getEchoChar();
        jLabel1.setFont(new Font("Segoe UI", Font.BOLD, 48));
        jLabel1.setForeground(Color.WHITE);
        jLabel1.setAlignmentX(Component.LEFT_ALIGNMENT);
        jLabel2.setIcon(null); jLabel6.setIcon(null);
        jLabel4.setText("Username"); jLabel4.setLabelFor(txt_Login); jLabel4.setDisplayedMnemonic('U');
        jLabel5.setText("Password"); jLabel5.setLabelFor(jPasswordField); jLabel5.setDisplayedMnemonic('P');
        txt_Login.setName("username"); jPasswordField.setName("password"); btn_Login.setName("signIn");
        loginStatus.setName("loginStatus"); loginStatus.setForeground(AppTheme.MUTED);
        loginStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginStatus.setPreferredSize(new Dimension(350, 54));
        loginStatus.getAccessibleContext().setAccessibleName("Sign in status");
        WorkspaceUI.primary(btn_Login, "Sign in"); btn_Login.setBackground(AppTheme.GREEN); btn_Login.setMnemonic('S');
        btn_Login.setFocusPainted(true);
        jCheckBox1.setText("Show password"); jCheckBox1.setMnemonic('H'); jCheckBox1.setOpaque(false);

        JPanel brand = new JPanel(new BorderLayout(0, 32));
        brand.setName("signInBrand"); brand.setBackground(AppTheme.FOREST);
        brand.setBorder(BorderFactory.createEmptyBorder(42, 32, 32, 28));
        brand.setPreferredSize(new Dimension(330, 580));
        brand.add(jLabel1, BorderLayout.NORTH);
        JPanel message = new JPanel(); message.setOpaque(false);
        message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
        for (String line : new String[]{"People.", "Attendance.", "Payroll."}) {
            JLabel text = new JLabel(line); text.setForeground(new Color(222, 238, 229));
            text.setFont(new Font("Segoe UI", Font.PLAIN, 31));
            message.add(text); message.add(Box.createVerticalStrut(10));
        }
        JPanel centeredMessage = new JPanel(new GridBagLayout()); centeredMessage.setOpaque(false);
        GridBagConstraints brandConstraint = new GridBagConstraints(); brandConstraint.anchor = GridBagConstraints.WEST; brandConstraint.weightx = 1;
        centeredMessage.add(message, brandConstraint); brand.add(centeredMessage, BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(36, 40, 36, 40));
        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 8, 0);
        JLabel heading = new JLabel("Welcome back"); heading.setFont(new Font("Segoe UI", Font.BOLD, 29)); heading.setForeground(AppTheme.INK);
        fields.add(heading, c);
        JLabel introduction = new JLabel("Sign in to your MotorPH workspace."); introduction.setForeground(AppTheme.MUTED);
        c.gridy++; c.insets = new Insets(0, 0, 30, 0); fields.add(introduction, c);
        c.gridy++; c.insets = new Insets(0, 0, 7, 0); fields.add(jLabel4, c);
        c.gridy++; c.insets = new Insets(0, 0, 20, 0); fields.add(txt_Login, c);
        c.gridy++; c.insets = new Insets(0, 0, 7, 0); fields.add(jLabel5, c);
        c.gridy++; c.insets = new Insets(0, 0, 12, 0); fields.add(jPasswordField, c);
        c.gridy++; c.insets = new Insets(0, 0, 24, 0); fields.add(jCheckBox1, c);
        c.gridy++; c.insets = new Insets(0, 0, 12, 0); fields.add(btn_Login, c);
        c.gridy++; c.insets = new Insets(0, 0, 0, 0); fields.add(loginStatus, c);
        GridBagConstraints centered = new GridBagConstraints(); centered.fill = GridBagConstraints.HORIZONTAL; centered.weightx = 1;
        form.add(fields, centered);
        jPanel1.removeAll(); jPanel1.setLayout(new BorderLayout());
        jPanel1.add(brand, BorderLayout.WEST); jPanel1.add(form, BorderLayout.CENTER);
        setContentPane(jPanel1);
        getRootPane().setDefaultButton(btn_Login);
        // Establish the native peer and initial component geometry before applying the workspace bounds.
        pack();
        setSize(940, 650); setMinimumSize(new Dimension(800, 610));
        validate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * Control construction retained for handler compatibility.
     * The generated fixed layout is retired; composition is maintained in source.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Control construction - generated layout retired">
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        txt_Login = new javax.swing.JTextField();
        btn_Login = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jPasswordField = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        txt_Login.setBackground(new java.awt.Color(255, 255, 255));
        txt_Login.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txt_Login.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1),
            javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        txt_Login.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_LoginActionPerformed(evt);
            }
        });

        btn_Login.setBackground(new java.awt.Color(34, 197, 94));
        btn_Login.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_Login.setForeground(new java.awt.Color(255, 255, 255));
        btn_Login.setText("LOGIN");
        btn_Login.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 32, 16, 32));
        btn_Login.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_Login.setFocusPainted(false);
        btn_Login.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LoginActionPerformed(evt);
            }
        });

        jLabel2.setText("jLabel2");

        jLabel1.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(17, 24, 39));
        jLabel1.setText("MotorPH");


        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(55, 65, 81));
        jLabel4.setText("Username:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(55, 65, 81));
        jLabel5.setText("Password:");

        jCheckBox1.setBackground(new java.awt.Color(255, 255, 255));
        jCheckBox1.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jCheckBox1.setForeground(new java.awt.Color(107, 114, 128));
        jCheckBox1.setText("Show Password");
        jCheckBox1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });


        jPasswordField.setBackground(new java.awt.Color(255, 255, 255));
        jPasswordField.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPasswordField.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1),
            javax.swing.BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        jPasswordField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPasswordFieldActionPerformed(evt);
            }
        });

    }// </editor-fold>

    //mikko start
    private void btn_LoginActionPerformed(java.awt.event.ActionEvent evt) {
        if (signingIn || (retryTimer != null && retryTimer.isRunning())) return;
        final String username = txt_Login.getText().trim();
        final char[] password = jPasswordField.getPassword();
        if (username.isEmpty() || password.length == 0) {
            Arrays.fill(password, '\0');
            setStatus("Enter both your username and password.", true);
            if (username.isEmpty()) txt_Login.requestFocusInWindow();
            else jPasswordField.requestFocusInWindow();
            return;
        }
        EmpUserSession.getInstance().clearSession();
        jPasswordField.setText("");
        setSigningIn(true);
        setStatus("Signing in\u2026", false);
        new SwingWorker<DatabaseHandler.AuthenticationResult, Void>() {
            @Override protected DatabaseHandler.AuthenticationResult doInBackground() {
                try {
                    return authenticator.apply(username, password);
                } finally {
                    Arrays.fill(password, '\0');
                }
            }
            @Override protected void done() {
                setSigningIn(false);
                if (!isDisplayable()) return;
                try {
                    DatabaseHandler.AuthenticationResult result = get();
                    if (result == null) {
                        jPasswordField.setText("");
                        setStatus("Username or password is incorrect. Try again.", true);
                        jPasswordField.requestFocusInWindow();
                        return;
                    }
                    if (result.isResetRequired()) {
                        EmpUserSession.getInstance().clearSession();
                        setStatus("Change your temporary password to continue.", false);
                        PasswordResetDialog reset = new PasswordResetDialog(frm_Login.this, username,
                                passwordResetter, frm_Login.this::completeAuthentication);
                        reset.setVisible(true);
                        if (isDisplayable() && EmpUserSession.getInstance().getCurrentUser() == null) {
                            setStatus("Password change required. Sign in to try again.", true);
                        }
                        return;
                    }
                    completeAuthentication(result);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    setStatus("Sign in was interrupted. Please try again.", true);
                } catch (ExecutionException ex) {
                    if (ex.getCause() instanceof AuthenticationThrottledException) {
                        startRetryDelay(((AuthenticationThrottledException) ex.getCause()).getRetryAfterSeconds());
                    } else {
                        setStatus("Sign in unavailable. Check the connection and retry.", true);
                    }
                }
            }
        }.execute();
    }//GEN-LAST:event_btn_LoginActionPerformed

    private void completeAuthentication(DatabaseHandler.AuthenticationResult result) {
        if (result == null || result.isResetRequired() || result.getEmployee() == null || result.getRole() == null) {
            EmpUserSession.getInstance().clearSession();
            setStatus("Sign in could not be completed. Please try again.", true);
            return;
        }
        try {
            EmpUserSession.getInstance().establish(result);
            if (authenticatedNavigation == null) new frm_EmpProfile().setVisible(true);
            else authenticatedNavigation.accept(result);
            dispose();
        } catch (PersistenceException ex) {
            EmpUserSession.getInstance().clearSession();
            setStatus("Profile unavailable. Check the connection and retry.", true);
        } catch (AuthorizationException ex) {
            EmpUserSession.getInstance().clearSession();
            setStatus("Sign in could not be completed. Please try again.", true);
        }
    }

    private void startRetryDelay(long retryAfterSeconds) {
        long seconds = Math.max(1, Math.min(retryAfterSeconds, 86400));
        retryDeadlineNanos = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(seconds);
        if (retryTimer != null) retryTimer.stop();
        retryTimer = new Timer(250, event -> updateRetryDelay());
        updateRetryDelay();
        retryTimer.start();
    }

    private void updateRetryDelay() {
        long remaining = retryDeadlineNanos - System.nanoTime();
        if (remaining <= 0) {
            if (retryTimer != null) retryTimer.stop();
            btn_Login.setEnabled(true);
            btn_Login.setText("Sign in");
            setStatus("You can try signing in again.", false);
            return;
        }
        long seconds = (remaining + 999999999L) / 1000000000L;
        btn_Login.setEnabled(false);
        btn_Login.setText("Try again (" + seconds + " s)");
        setStatus("Sign in could not be completed. Try again in " + seconds + " seconds.", true);
    }

    @Override public void dispose() {
        if (retryTimer != null) retryTimer.stop();
        super.dispose();
    }

    private void setSigningIn(boolean busy) {
        signingIn = busy;
        txt_Login.setEnabled(!busy);
        jPasswordField.setEnabled(!busy);
        jCheckBox1.setEnabled(!busy);
        btn_Login.setEnabled(!busy);
        btn_Login.setText(busy ? "Signing in\u2026" : "Sign in");
        setCursor(Cursor.getPredefinedCursor(busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    private void setStatus(String text, boolean error) {
        loginStatus.setForeground(error ? new Color(185, 28, 28) : AppTheme.MUTED);
        loginStatus.setText("<html><div style='width: 280px'>" + text.replace("&", "&amp;").replace("<", "&lt;") + "</div></html>");
        loginStatus.getAccessibleContext().setAccessibleDescription(text);
    }

    //Mikko end
    private void txt_LoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_LoginActionPerformed
        btn_Login.doClick();
    }//GEN-LAST:event_txt_LoginActionPerformed

    private void jPasswordFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPasswordFieldActionPerformed
        btn_Login.doClick();
    }//GEN-LAST:event_jPasswordFieldActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed

        if (jCheckBox1.isSelected()) {
            jPasswordField.setEchoChar((char) 0);
        } else {
            jPasswordField.setEchoChar(passwordEcho);
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(frm_Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frm_Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frm_Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frm_Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frm_Login().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_Login;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField;
    private javax.swing.JTextField txt_Login;
    // End of variables declaration//GEN-END:variables
}
