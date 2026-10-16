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
        setResizable(false);
        setTitle("Welcome to MotorPH");
        AppTheme.finish(this);
    }

    private void configureLogin() {
        passwordEcho = jPasswordField.getEchoChar();
        jLabel1.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 40));
        jLabel3.setForeground(AppTheme.MUTED);
        jLabel3.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        jLabel1.setAlignmentX(Component.LEFT_ALIGNMENT);
        jLabel3.setAlignmentX(Component.LEFT_ALIGNMENT);
        jLabel3.setName("companyName");
        Dimension companySize = jLabel3.getPreferredSize();
        companySize.width = jLabel3.getFontMetrics(jLabel3.getFont()).stringWidth(jLabel3.getText()) + 4;
        jLabel3.setMinimumSize(companySize);
        jLabel3.setPreferredSize(companySize);
        jLabel3.setMaximumSize(companySize);
        jLabel2.setText("");
        jLabel2.getAccessibleContext().setAccessibleName("MotorPH brand artwork");
        jLabel6.getAccessibleContext().setAccessibleName("MotorPH motorcycle logo");
        ImageIcon logo = (ImageIcon) jLabel6.getIcon();
        jLabel6.setIcon(new ImageIcon(logo.getImage().getScaledInstance(112, 112, Image.SCALE_SMOOTH)));
        jLabel6.setPreferredSize(new Dimension(112, 112));
        jLabel4.setLabelFor(txt_Login);
        jLabel5.setLabelFor(jPasswordField);
        jLabel4.setDisplayedMnemonic('U');
        jLabel5.setDisplayedMnemonic('P');
        txt_Login.setName("username");
        jPasswordField.setName("password");
        btn_Login.setName("signIn");
        loginStatus.setName("loginStatus");
        loginStatus.setForeground(AppTheme.MUTED);
        loginStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginStatus.setPreferredSize(new Dimension(402, 34));
        loginStatus.getAccessibleContext().setAccessibleName("Sign in status");
        btn_Login.setText("Sign in");
        btn_Login.setBackground(AppTheme.GREEN);
        btn_Login.setMnemonic('S');
        jCheckBox1.setText("Show password");
        jCheckBox1.setMnemonic('H');
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.add(Box.createVerticalStrut(20));
        brand.add(jLabel1);
        brand.add(Box.createVerticalStrut(5));
        brand.add(jLabel3);
        header.add(brand, BorderLayout.CENTER);
        header.add(jLabel6, BorderLayout.EAST);
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; c.insets = new Insets(0, 0, 7, 0);
        fields.add(jLabel4, c);
        c.gridy++; c.insets = new Insets(0, 0, 18, 0); fields.add(txt_Login, c);
        c.gridy++; c.insets = new Insets(0, 0, 7, 0); fields.add(jLabel5, c);
        c.gridy++; c.insets = new Insets(0, 0, 10, 0); fields.add(jPasswordField, c);
        c.gridy++; c.insets = new Insets(0, 0, 16, 0); fields.add(jCheckBox1, c);
        c.gridy++; c.insets = new Insets(0, 0, 12, 0); fields.add(btn_Login, c);
        c.gridy++; c.insets = new Insets(0, 0, 0, 0); fields.add(loginStatus, c);
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(20, 32, 16, 32));
        body.add(header, BorderLayout.NORTH);
        body.add(fields, BorderLayout.CENTER);
        jPanel1.removeAll();
        jPanel1.setLayout(new BorderLayout());
        jPanel1.add(body, BorderLayout.CENTER);
        jPanel1.add(jLabel2, BorderLayout.SOUTH);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jPanel1, BorderLayout.CENTER);
        getRootPane().setDefaultButton(btn_Login);
        setSize(480, 700);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        txt_Login = new javax.swing.JTextField();
        btn_Login = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
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

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oop_motorph/img/img_Login.png"))); // NOI18N
        jLabel2.setText("jLabel2");

        jLabel1.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 48)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(17, 24, 39));
        jLabel1.setText("MotorPH");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(107, 114, 128));
        jLabel3.setText("Red Dragon Technologies Inc.");

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

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oop_motorph/img/img_Logo_XS.png"))); // NOI18N

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(120, 120, 120)
                                                .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(40, 40, 40)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel3)
                                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                        .addComponent(jLabel4)
                                                                        .addComponent(jLabel5))
                                                                .addGap(18, 18, 18)
                                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(txt_Login, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                                                                        .addComponent(jPasswordField, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                                                                        .addComponent(btn_Login, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))))))
                                .addContainerGap(40, Short.MAX_VALUE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(53, 53, 53)
                                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel3))
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(18, 18, 18)
                                                .addComponent(jLabel6)))
                                .addGap(55, 55, 55)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(txt_Login, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel4))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGap(24, 24, 24)
                                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(21, 21, 21))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)))
                                .addComponent(jCheckBox1)
                                .addGap(18, 18, 18)
                                .addComponent(btn_Login)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField;
    private javax.swing.JTextField txt_Login;
    // End of variables declaration//GEN-END:variables
}
