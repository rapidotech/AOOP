package oop_motorph;

import javax.swing.JOptionPane;
import java.awt.*;
import java.util.List;


public class frm_EmpProfile extends javax.swing.JFrame {

    
    public frm_EmpProfile() {
        AuthorizationService.requireAuthenticated();
        initComponents();
        AppTheme.finish(this);
        
        
        // Initialize combo boxes
        cmb_empStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Regular", "Probationary"}));
        cbox_position.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"HR Manager", "Account Team Leader", "Payroll Manager", "HR Rank and File", "Payroll Rank and File", "Account Rank and File"}));
        
        // Show only this employee's supervisor in the read-only profile.
        loadSupervisors();

        EmpDetails employee = EmpUserSession.getInstance().getCurrentUser();
        String role = EmpUserSession.getInstance().getRole();
        
    if (employee != null && role != null) {
        // Load employee data and set role-based access
        loadEmployeeData(employee);
        setRoleBasedAccess(role);

    } else {
        JOptionPane.showMessageDialog(this, "No employee data or role found!", "Error", JOptionPane.ERROR_MESSAGE);
    }

        composeWorkspace();
    }

    // Load employee data into the form fields
    private void loadEmployeeData(EmpDetails employee) {
        txt_empID.setText(employee.getEmpID());
        txt_firstName.setText(employee.getFirstName());
        txt_lastName.setText(employee.getLastName());
        txt_birthdate.setText(employee.getBirthdate());
        txt_address.setText(employee.getAddress());
        txt_phoneNo.setText(employee.getPhoneNumber());

        cmb_empStatus.setSelectedItem(employee.getEmployeeStatus());
        cbox_position.setSelectedItem(employee.getPosition());
        cmb_Supervisor.setSelectedItem(employee.getImmediateSupervisor());
    }
 
    // Set role-based access controls
    private void setRoleBasedAccess(String role) {
        // Disable all by default
        btn_LeaveMgt.setEnabled(false);
        btn_SalaryAndStatutory.setEnabled(false);
        btn_Logout.setEnabled(false);
        btn_EmpRecords.setEnabled(false);
        btn_PayrollSummary.setEnabled(false);
        cmb_empStatus.setEnabled(false);
        cbox_position.setEnabled(false);
        cmb_Supervisor.setEnabled(false);

        switch (role.toUpperCase()) {
            case "EMPLOYEE" -> {
                btn_LeaveMgt.setEnabled(true);
                btn_SalaryAndStatutory.setEnabled(true);
                btn_Logout.setEnabled(true);
                btn_PayrollSummary.setEnabled(true);
            }
            case "HR" -> {
                btn_LeaveMgt.setEnabled(true);
                btn_SalaryAndStatutory.setEnabled(true);
                btn_Logout.setEnabled(true);
                btn_EmpRecords.setEnabled(true);
                btn_PayrollSummary.setEnabled(true);
            }
            default -> {
                JOptionPane.showMessageDialog(this, "Access denied for role: " + role, "Permission Denied", JOptionPane.ERROR_MESSAGE);
                dispose(); // Close this frame if role is unauthorized
            }
        }
    }

    // The profile needs no list of other employees or supervisors.
    private void loadSupervisors() {
        EmpDetails employee = EmpUserSession.getInstance().getCurrentUser();
        String supervisor = employee.getImmediateSupervisor();
        cmb_Supervisor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            supervisor == null || supervisor.isBlank() ? "No supervisor" : supervisor
        }));
        cmb_Supervisor.setEnabled(false);
    }

    /** Source-owned responsive composition; legacy fixed layout is retired. */
    private void composeWorkspace() {
        WorkspaceShell.bindNavigation(this, "profile", btn_Profile);
        WorkspaceShell.bindNavigation(this, "salary", btn_SalaryAndStatutory);
        WorkspaceShell.bindNavigation(this, "attendance", btn_LeaveMgt);
        WorkspaceShell.bindNavigation(this, "payroll", btn_PayrollSummary);
        WorkspaceShell.bindNavigation(this, "employees", btn_EmpRecords);
        WorkspaceShell.bindNavigation(this, "logout", btn_Logout);
        javax.swing.JComponent content = WorkspaceUI.column(
            WorkspaceUI.section("Personal information", "Your employee record", WorkspaceUI.fields(
                "Employee ID", txt_empID, "First name", txt_firstName,
                "Last name", txt_lastName, "Date of birth", txt_birthdate,
                "Phone number", txt_phoneNo, "Address", txt_address)),
            WorkspaceUI.section("Employment", "Current role and reporting line", WorkspaceUI.fields(
                "Employment status", cmb_empStatus, "Position", cbox_position,
                "Immediate supervisor", cmb_Supervisor)));
        WorkspaceShell.mount(this, "My profile", "Personal and employment information", "profile", WorkspaceUI.scroll(content), null);
        if ("HR".equalsIgnoreCase(EmpUserSession.getInstance().getRole())) SecurityActivityPanel.attach(this);
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
        jPanel2 = new javax.swing.JPanel();
        txt_empID = new javax.swing.JTextField();
        txt_firstName = new javax.swing.JTextField();
        txt_lastName = new javax.swing.JTextField();
        txt_birthdate = new javax.swing.JTextField();
        txt_address = new javax.swing.JTextField();
        txt_phoneNo = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        cbox_immSupervisor = new javax.swing.JLabel();
        cmb_empStatus = new javax.swing.JComboBox<>();
        cbox_position = new javax.swing.JComboBox<>();
        cmb_Supervisor = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        btn_Profile = new javax.swing.JButton();
        btn_LeaveMgt = new javax.swing.JButton();
        btn_SalaryAndStatutory = new javax.swing.JButton();
        btn_PayrollSummary = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        btn_MyRecords = new javax.swing.JButton();
        btn_EmpRecords = new javax.swing.JButton();
        btn_Logout = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(249, 250, 251));
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createEmptyBorder(8, 8, 12, 12),
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(229, 231, 235), 1),
                javax.swing.BorderFactory.createEmptyBorder(32, 32, 32, 32)
            )
        ));

        txt_empID.setEditable(false);
        txt_empID.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txt_empID.setBackground(new java.awt.Color(249, 250, 251));
        txt_empID.setForeground(new java.awt.Color(55, 65, 81));

        txt_firstName.setEditable(false);
        txt_firstName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txt_firstName.setBackground(new java.awt.Color(249, 250, 251));
        txt_firstName.setForeground(new java.awt.Color(55, 65, 81));

        txt_lastName.setEditable(false);
        txt_lastName.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txt_lastName.setBackground(new java.awt.Color(249, 250, 251));
        txt_lastName.setForeground(new java.awt.Color(55, 65, 81));

        txt_birthdate.setEditable(false);
        txt_birthdate.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txt_birthdate.setBackground(new java.awt.Color(249, 250, 251));
        txt_birthdate.setForeground(new java.awt.Color(55, 65, 81));
        txt_birthdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_birthdateActionPerformed(evt);
            }
        });

        txt_address.setEditable(false);
        txt_address.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txt_address.setBackground(new java.awt.Color(249, 250, 251));
        txt_address.setForeground(new java.awt.Color(55, 65, 81));

        txt_phoneNo.setEditable(false);
        txt_phoneNo.setFont(new java.awt.Font("Segoe UI", 0, 14));
        txt_phoneNo.setBackground(new java.awt.Color(249, 250, 251));
        txt_phoneNo.setForeground(new java.awt.Color(55, 65, 81));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(75, 85, 99));
        jLabel7.setText("Employee ID:");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(75, 85, 99));
        jLabel8.setText("First Name:");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(75, 85, 99));
        jLabel9.setText("Last Name:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(75, 85, 99));
        jLabel6.setText("Birthdate:");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(75, 85, 99));
        jLabel10.setText("Address:");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(75, 85, 99));
        jLabel11.setText("Phone Number:");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(75, 85, 99));
        jLabel12.setText("Status:");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(75, 85, 99));
        jLabel13.setText("Position:");

        cbox_immSupervisor.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        cbox_immSupervisor.setForeground(new java.awt.Color(75, 85, 99));
        cbox_immSupervisor.setText("Immediate Supervisor:");

        cmb_empStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmb_empStatus.setEnabled(false);

        cbox_position.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbox_position.setEnabled(false);

        cmb_Supervisor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmb_Supervisor.setEnabled(false);

        jLabel2.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 32)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(17, 24, 39));
        jLabel2.setText("MotorPH");

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(229, 231, 235)),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));


        btn_Profile.setBackground(new java.awt.Color(34, 197, 94));
        btn_Profile.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_Profile.setForeground(new java.awt.Color(255, 255, 255));
        btn_Profile.setText("Profile");
        btn_Profile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ProfileActionPerformed(evt);
            }
        });

        btn_LeaveMgt.setBackground(new java.awt.Color(107, 114, 128));
        btn_LeaveMgt.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_LeaveMgt.setForeground(new java.awt.Color(255, 255, 255));
        btn_LeaveMgt.setText("Attendance & Leave");
        btn_LeaveMgt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LeaveMgtActionPerformed(evt);
            }
        });

        btn_SalaryAndStatutory.setBackground(new java.awt.Color(107, 114, 128));
        btn_SalaryAndStatutory.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_SalaryAndStatutory.setForeground(new java.awt.Color(255, 255, 255));
        btn_SalaryAndStatutory.setText("Salary & Statutory");
        btn_SalaryAndStatutory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SalaryAndStatutoryActionPerformed(evt);
            }
        });

        btn_PayrollSummary.setBackground(new java.awt.Color(107, 114, 128));
        btn_PayrollSummary.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_PayrollSummary.setForeground(new java.awt.Color(255, 255, 255));
        btn_PayrollSummary.setText("Payroll Summary");
        btn_PayrollSummary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_PayrollSummaryActionPerformed(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(245, 28, 71));


        btn_MyRecords.setBackground(new java.awt.Color(107, 114, 128));
        btn_MyRecords.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_MyRecords.setForeground(new java.awt.Color(255, 255, 255));
        btn_MyRecords.setText("My Records");
        btn_MyRecords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_MyRecordsActionPerformed(evt);
            }
        });

        btn_EmpRecords.setBackground(new java.awt.Color(107, 114, 128));
        btn_EmpRecords.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_EmpRecords.setForeground(new java.awt.Color(255, 255, 255));
        btn_EmpRecords.setText("Employee Records");
        btn_EmpRecords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_EmpRecordsActionPerformed(evt);
            }
        });

        btn_Logout.setBackground(new java.awt.Color(239, 68, 68));
        btn_Logout.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_Logout.setForeground(new java.awt.Color(255, 255, 255));
        btn_Logout.setText("Logout");
        btn_Logout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LogoutActionPerformed(evt);
            }
        });

        jPanel5.setBackground(new java.awt.Color(255, 255, 102));


        jPanel6.setBackground(new java.awt.Color(255, 255, 102));


        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel5.setText("Version 1.30");


        jPanel1.getAccessibleContext().setAccessibleName("Profile");

    }// </editor-fold>

    private void btn_PayrollSummaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_PayrollSummaryActionPerformed
         
        new frm_EmpPayroll().setVisible(true);
        this.dispose();
        
    }//GEN-LAST:event_btn_PayrollSummaryActionPerformed

    private void btn_SalaryAndStatutoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SalaryAndStatutoryActionPerformed
        
        new frm_EmpSalary().setVisible(true);
        this.dispose();
        
    }//GEN-LAST:event_btn_SalaryAndStatutoryActionPerformed

    private void btn_LeaveMgtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LeaveMgtActionPerformed
        
        new frm_EmpAttLeave().setVisible(true);
        this.dispose();
        
    }//GEN-LAST:event_btn_LeaveMgtActionPerformed

    private void btn_ProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ProfileActionPerformed
        
        new frm_EmpProfile().setVisible(true);
        this.dispose();
        
    }//GEN-LAST:event_btn_ProfileActionPerformed

    private void btn_MyRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_MyRecordsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_MyRecordsActionPerformed

    private void btn_LogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LogoutActionPerformed
        CSVHandler.handleLogout(this); 

    }//GEN-LAST:event_btn_LogoutActionPerformed

    private void btn_EmpRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_EmpRecordsActionPerformed
        
        new frm_EmployeesRecords().setVisible(true);
        this.dispose();
        
    }//GEN-LAST:event_btn_EmpRecordsActionPerformed

    private void txt_birthdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_birthdateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_birthdateActionPerformed
 
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
            java.util.logging.Logger.getLogger(frm_EmpProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frm_EmpProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frm_EmpProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frm_EmpProfile.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frm_EmpProfile().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_EmpRecords;
    private javax.swing.JButton btn_LeaveMgt;
    private javax.swing.JButton btn_Logout;
    private javax.swing.JButton btn_MyRecords;
    private javax.swing.JButton btn_PayrollSummary;
    private javax.swing.JButton btn_Profile;
    private javax.swing.JButton btn_SalaryAndStatutory;
    private javax.swing.JLabel cbox_immSupervisor;
    private javax.swing.JComboBox<String> cbox_position;
    private javax.swing.JComboBox<String> cmb_Supervisor;
    private javax.swing.JComboBox<String> cmb_empStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTextField txt_address;
    private javax.swing.JTextField txt_birthdate;
    private javax.swing.JTextField txt_empID;
    private javax.swing.JTextField txt_firstName;
    private javax.swing.JTextField txt_lastName;
    private javax.swing.JTextField txt_phoneNo;
    // End of variables declaration//GEN-END:variables
}
