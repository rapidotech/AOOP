package oop_motorph;

import javax.swing.JOptionPane;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;


public class frm_EmpSalary extends javax.swing.JFrame {


    public frm_EmpSalary() {
        AuthorizationService.requireAuthenticated();
        initComponents();
        AppTheme.finish(this);

        EmpDetails employee = EmpUserSession.getInstance().getCurrentUser();
        String role = EmpUserSession.getInstance().getRole();

        if (employee != null && role != null) {
            // Load salary data from CSV
            List<EmpSalaryDetails> salaryData = CSVHandler.getSalaryData();
            EmpSalaryDetails salaryDetails = null;

            // Find the salary details for the logged-in employee
            for (EmpSalaryDetails details : salaryData) {
                if (details.getEmpID().equals(employee.getEmpID())) {
                    salaryDetails = details;
                    break;
                }
            }

            if (salaryDetails != null) {
                // Set the salary data in the form
                setSalaryData(salaryDetails);
            } else {
                JOptionPane.showMessageDialog(this, "Salary data not found for the user!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            setRoleBasedAccess(role);
        } else {
            JOptionPane.showMessageDialog(this, "No employee data or role found!", "Error", JOptionPane.ERROR_MESSAGE);
        }

        composeWorkspace();
    }

    // Method to set salary data in the form
    private void setSalaryData(EmpSalaryDetails salaryDetails) {
        txt_sssNo.setText(salaryDetails.getSssNo());
        txt_philhealthNo.setText(salaryDetails.getPhilhealthNo());
        txt_tinNo.setText(salaryDetails.getTinNo());
        txt_pagibigNo.setText(salaryDetails.getPagibigNo());
        txt_basicSalary.setText(String.valueOf(salaryDetails.getBasicSalary()));
        txt_riceAllow.setText(String.valueOf(salaryDetails.getRiceSubsidy()));
        txt_phoneAllow.setText(String.valueOf(salaryDetails.getPhoneAllowance()));
        txt_clothingAllow.setText(String.valueOf(salaryDetails.getClothingAllowance()));
        txt_grossSemi.setText(String.valueOf(salaryDetails.getGrossSemi()));
        txt_hourlyRate.setText(String.valueOf(salaryDetails.getHourlyRate()));
    }

    // Set role-based access controls
    public void setRoleBasedAccess(String role) {
        // Disable all by default
        btn_LeaveMgt.setEnabled(false);
        btn_Profile.setEnabled(false);
        btn_Logout.setEnabled(false);
        btn_EmpRecords.setEnabled(false);
        btn_PayrollSummary.setEnabled(false);

        switch (role.toUpperCase()) {
            case "EMPLOYEE" -> {
                btn_LeaveMgt.setEnabled(true);
                btn_Profile.setEnabled(true);
                btn_Logout.setEnabled(true);
                btn_PayrollSummary.setEnabled(true);
            }
            case "HR" -> {
                btn_LeaveMgt.setEnabled(true);
                btn_Profile.setEnabled(true);
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

    /** Source-owned responsive composition; legacy fixed layout is retired. */
    private void composeWorkspace() {
        WorkspaceShell.bindNavigation(this, "profile", btn_Profile);
        WorkspaceShell.bindNavigation(this, "salary", btn_SalaryAndStatutory);
        WorkspaceShell.bindNavigation(this, "attendance", btn_LeaveMgt);
        WorkspaceShell.bindNavigation(this, "payroll", btn_PayrollSummary);
        WorkspaceShell.bindNavigation(this, "employees", btn_EmpRecords);
        WorkspaceShell.bindNavigation(this, "logout", btn_Logout);
        javax.swing.JComponent content = WorkspaceUI.column(
            WorkspaceUI.section("Compensation", "Salary and rates on record", WorkspaceUI.fields(
                "Basic salary", txt_basicSalary, "Gross semimonthly rate", txt_grossSemi, "Hourly rate", txt_hourlyRate)),
            WorkspaceUI.section("Allowances", "Benefits on record", WorkspaceUI.fields(
                "Rice subsidy", txt_riceAllow, "Phone allowance", txt_phoneAllow, "Clothing allowance", txt_clothingAllow)),
            WorkspaceUI.section("Statutory identification", "Government contribution references", WorkspaceUI.fields(
                "SSS number", txt_sssNo, "PhilHealth number", txt_philhealthNo,
                "Tax identification number", txt_tinNo, "Pag-IBIG number", txt_pagibigNo)));
        WorkspaceShell.mount(this, "Salary & statutory", "Compensation, allowances and contribution references", "salary", WorkspaceUI.scroll(content), null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * Control construction retained for handler compatibility.
     * The generated fixed layout is retired; composition is maintained in source.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Control construction - generated layout retired">
    // Legacy control identifiers retained; WorkspaceShell owns the visible composition.
    private void initComponents() {
        jPanel1 = new JPanel();
        jPanel2 = new JPanel();
        txt_sssNo = new JTextField();
        txt_philhealthNo = new JTextField();
        txt_pagibigNo = new JTextField();
        txt_tinNo = new JTextField();
        txt_basicSalary = new JTextField();
        txt_riceAllow = new JTextField();
        txt_phoneAllow = new JTextField();
        txt_clothingAllow = new JTextField();
        txt_grossSemi = new JTextField();
        txt_hourlyRate = new JTextField();
        jLabel6 = new JLabel();
        jLabel7 = new JLabel();
        jLabel8 = new JLabel();
        jLabel9 = new JLabel();
        jLabel10 = new JLabel();
        jLabel11 = new JLabel();
        jLabel12 = new JLabel();
        jLabel13 = new JLabel();
        jLabel14 = new JLabel();
        jLabel15 = new JLabel();
        jPanel3 = new JPanel();
        btn_Profile = new JButton();
        btn_LeaveMgt = new JButton();
        btn_SalaryAndStatutory = new JButton();
        btn_PayrollSummary = new JButton();
        jPanel4 = new JPanel();
        btn_MyRecords = new JButton();
        btn_EmpRecords = new JButton();
        btn_Logout = new JButton();
        jLabel1 = new JLabel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        jPanel5 = new JPanel();
        jPanel6 = new JPanel();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        var contentPane = getContentPane();

        //======== jPanel1 ========
        {
            jPanel1.setBorder(new CompoundBorder());

            //======== jPanel2 ========
            {
                jPanel2.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED, null, Color.lightGray, null, null));

                //---- txt_sssNo ----
                txt_sssNo.setEditable(false);

                //---- txt_philhealthNo ----
                txt_philhealthNo.setEditable(false);

                //---- txt_pagibigNo ----
                txt_pagibigNo.setEditable(false);

                //---- txt_tinNo ----
                txt_tinNo.setEditable(false);

                //---- txt_basicSalary ----
                txt_basicSalary.setEditable(false);

                //---- txt_riceAllow ----
                txt_riceAllow.setEditable(false);

                //---- txt_phoneAllow ----
                txt_phoneAllow.setEditable(false);

                //---- txt_clothingAllow ----
                txt_clothingAllow.setEditable(false);

                //---- txt_grossSemi ----
                txt_grossSemi.setEditable(false);

                //---- txt_hourlyRate ----
                txt_hourlyRate.setEditable(false);

                //---- jLabel6 ----
                jLabel6.setText("SSS Number:");

                //---- jLabel7 ----
                jLabel7.setText("Philhealth Number:");

                //---- jLabel8 ----
                jLabel8.setText("Pagibig Number:");

                //---- jLabel9 ----
                jLabel9.setText("TIN Number:");

                //---- jLabel10 ----
                jLabel10.setText("Basic Salary:");

                //---- jLabel11 ----
                jLabel11.setText("Rice Subsidy Allowance:");

                //---- jLabel12 ----
                jLabel12.setText("Phone Allowance:");

                //---- jLabel13 ----
                jLabel13.setText("Clothing Allowance:");

                //---- jLabel14 ----
                jLabel14.setText("Gross Semi-Monthly Rate:");

                //---- jLabel15 ----
                jLabel15.setText("Hourly Rate:");


            }

            //======== jPanel3 ========
            {


            }

            //---- btn_Profile ----
            btn_Profile.setText("Profile");
            btn_Profile.addActionListener(e -> btn_ProfileActionPerformed(e));

            //---- btn_LeaveMgt ----
            btn_LeaveMgt.setText("Attendance & Leave");
            btn_LeaveMgt.addActionListener(e -> btn_LeaveMgtActionPerformed(e));

            //---- btn_SalaryAndStatutory ----
            btn_SalaryAndStatutory.setText("Salary & Statutory");
            btn_SalaryAndStatutory.addActionListener(e -> btn_SalaryAndStatutoryActionPerformed(e));

            //---- btn_PayrollSummary ----
            btn_PayrollSummary.setText("Payroll Summary");
            btn_PayrollSummary.addActionListener(e -> btn_PayrollSummaryActionPerformed(e));        //======== jPanel4 ========
        {


            }

            //---- btn_MyRecords ----
            btn_MyRecords.setText("My Records");
            btn_MyRecords.addActionListener(e -> btn_MyRecordsActionPerformed(e));

            //---- btn_EmpRecords ----
            btn_EmpRecords.setText("Employee Records");
            btn_EmpRecords.addActionListener(e -> btn_EmpRecordsActionPerformed(e));

            //---- btn_Logout ----
            btn_Logout.setText("Logout");
            btn_Logout.addActionListener(e -> btn_LogoutActionPerformed(e));

            //---- jLabel1 ----


            //---- jLabel3 ----


            //---- jLabel4 ----


            //======== jPanel5 ========
            {


            }

            //======== jPanel6 ========
            {


            }


        }

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

        new frm_EmpProfile().setVisible(true);
        this.dispose();

    }//GEN-LAST:event_btn_MyRecordsActionPerformed

    private void btn_LogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LogoutActionPerformed
        CSVHandler.handleLogout(this);

    }//GEN-LAST:event_btn_LogoutActionPerformed

    private void btn_EmpRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_EmpRecordsActionPerformed

        new frm_EmployeesRecords().setVisible(true);
        this.dispose();

    }//GEN-LAST:event_btn_EmpRecordsActionPerformed

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
            java.util.logging.Logger.getLogger(frm_EmpSalary.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frm_EmpSalary.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frm_EmpSalary.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frm_EmpSalary.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>


        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frm_EmpSalary().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Legacy control identifiers retained; WorkspaceShell owns the visible composition.
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JTextField txt_sssNo;
    private JTextField txt_philhealthNo;
    private JTextField txt_pagibigNo;
    private JTextField txt_tinNo;
    private JTextField txt_basicSalary;
    private JTextField txt_riceAllow;
    private JTextField txt_phoneAllow;
    private JTextField txt_clothingAllow;
    private JTextField txt_grossSemi;
    private JTextField txt_hourlyRate;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JLabel jLabel10;
    private JLabel jLabel11;
    private JLabel jLabel12;
    private JLabel jLabel13;
    private JLabel jLabel14;
    private JLabel jLabel15;
    private JPanel jPanel3;
    private JButton btn_Profile;
    private JButton btn_LeaveMgt;
    private JButton btn_SalaryAndStatutory;
    private JButton btn_PayrollSummary;
    private JPanel jPanel4;
    private JButton btn_MyRecords;
    private JButton btn_EmpRecords;
    private JButton btn_Logout;
    private JLabel jLabel1;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JPanel jPanel5;
    private JPanel jPanel6;
    // End of variables declaration//GEN-END:variables

}
