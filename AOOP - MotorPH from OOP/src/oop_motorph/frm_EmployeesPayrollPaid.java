
package oop_motorph;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class frm_EmployeesPayrollPaid extends javax.swing.JFrame implements WorkspaceScreen {


    /** Code-led layout. The archived NetBeans .form layout is retired. */
    @Override public void composeWorkspace() {
        WorkspaceShell.bindNavigation(this,"profile",btn_MyRecords);
        WorkspaceShell.bindNavigation(this,"employees",btn_EmpRecords);
        WorkspaceShell.bindNavigation(this,"processing",jButton1);
        WorkspaceShell.bindNavigation(this,"logout",btn_Logout);
        javax.swing.JPanel content=new javax.swing.JPanel(new java.awt.BorderLayout(0,16));content.setOpaque(false);
        content.add(WorkspaceUI.table(tbl_Payroll),java.awt.BorderLayout.CENTER);
        javax.swing.JPanel report=WorkspaceUI.column(WorkspaceUI.fields("Report period from",cal_printpayDateFrom,"Report period to",cal_printpayDateTo),WorkspaceUI.actions(WorkspaceUI.secondary(btn_printPayReport,"Print payroll report"),WorkspaceUI.secondary(btn_printPayslip,"Print payslip")));
        content.add(WorkspaceUI.section("Reports","Choose a date range for the payroll report.",report),java.awt.BorderLayout.SOUTH);
        WorkspaceShell.mount(this,"Payroll approvals","Verify payroll, approve payment and review recorded decisions.","approvals",content,
            WorkspaceUI.actions(WorkspaceUI.primary(btn_werifyPay,"Verify payroll"),WorkspaceUI.secondary(btn_AppPay,"Approve payment"),WorkspaceUI.secondary(btn_undoVerify,"Undo verification"),WorkspaceUI.secondary(btn_undoApp,"Undo approval")));
    }

    public frm_EmployeesPayrollPaid() {
        AuthorizationService.requireHr();
        initComponents();
        AppTheme.finish(this);

        // Set the window to center and disable resizable
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("Payroll Processing");

        // Disable table editing
        tbl_Payroll.setDefaultEditor(Object.class, null);

        // Load payroll data
        loadPayrollData();

        // Get the current user's role and set role-based access
        EmpDetails employee = EmpUserSession.getInstance().getCurrentUser();
        if (employee != null) {
            String role = EmpUserSession.getInstance().getRole();
            setRoleBasedAccess(role); // Apply role-based access controls
        } else {
            JOptionPane.showMessageDialog(this, "User not logged in!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPayrollData() {
        // Approval states come from the protected database ledger.
        List<String[]> payrollData = CSVHandler.loadPayrollLedger();

        // Define column headers
        String[] columns = {
            "Transaction ID", "Emp ID", "First Name", "Last Name",
            "Pay Date From", "Pay Date To", "Basic Salary", "Hourly Rate",
            "Total Allowances", "Hrs Per Month", "Total Hrs Worked", "Adj Earnings",
            "Total Earnings", "Tardiness Absences", "EE Tax", "EE SSS",
            "EE Pagibig", "EE Philhealth", "Adj Deductions", "Total Deductions", "Net Pay", "Pay Status"
        };

        // Create a table model
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        // Add all rows (Approved & Pending) to the model
        for (String[] row : payrollData) {
            if (row.length >= 22) {  // Show ALL entries
                model.addRow(row);
            }
        }

        // Set the model to tbl_Payroll
        tbl_Payroll.setModel(model);
    }

    private String generatePayslip(int selectedRow) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a payroll record first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        // Extract data from the selected row
        String empID = tbl_Payroll.getValueAt(selectedRow, 1).toString();
        String firstName = tbl_Payroll.getValueAt(selectedRow, 2).toString();
        String lastName = tbl_Payroll.getValueAt(selectedRow, 3).toString();
        String payDateFrom = tbl_Payroll.getValueAt(selectedRow, 4).toString();
        String payDateTo = tbl_Payroll.getValueAt(selectedRow, 5).toString();
        String basicSalary = tbl_Payroll.getValueAt(selectedRow, 6).toString();
        String hourlyRate = tbl_Payroll.getValueAt(selectedRow, 7).toString();
        String totalAllow = tbl_Payroll.getValueAt(selectedRow, 8).toString();
        String totalHoursWorked = tbl_Payroll.getValueAt(selectedRow, 10).toString();
        String totalEarnings = tbl_Payroll.getValueAt(selectedRow, 12).toString();
        String totalDeductions = tbl_Payroll.getValueAt(selectedRow, 19).toString();
        String netPay = tbl_Payroll.getValueAt(selectedRow, 20).toString();

        // Create a formatted payslip
        return "=================================\n"
             + "           EMPLOYEE PAYSLIP       \n"
             + "=================================\n"
             + "Employee ID: " + empID + "\n"
             + "Name: " + firstName + " " + lastName + "\n"
             + "Pay Period: " + payDateFrom + " to " + payDateTo + "\n"
             + "---------------------------------\n"
             + "Monthly Basic Salary: ₱" + basicSalary + "\n"
             + "\n"
             + "Hourly Rate: ₱" + hourlyRate + "\n"
             + "Hours Worked: " + totalHoursWorked + " hrs\n"
             + "Allowances: ₱" + totalAllow + "\n"
             + "Total Earnings: ₱" + totalEarnings + "\n"
             + "Total Deductions: ₱" + totalDeductions + "\n"
             + "---------------------------------\n"
             + "NET PAY: ₱" + netPay + "\n"
             + "=================================\n";
    }

    public void setRoleBasedAccess(String role) {
        // Default: Disable all buttons
        btn_AppPay.setEnabled(false);
        btn_undoApp.setEnabled(false);
        btn_werifyPay.setEnabled(false);
        btn_undoVerify.setEnabled(false);
        btn_printPayslip.setEnabled(false);
        btn_printPayReport.setEnabled(false);
        btn_werifyPay.setEnabled(false);
        btn_undoVerify.setEnabled(false);



        // Enable buttons based on role (case-insensitive)
        switch (role.toUpperCase()) {
            case "EMPLOYEE":
                // Employees cannot access payroll processing
                break;
            case "FINANCE":
                // Finance can access:
                btn_printPayslip.setEnabled(true);
                btn_printPayReport.setEnabled(true);
                break;
            case "HR":
                // HR can access:
                //no access
                break;
            case "DEPTHEAD":
                // Dept-Head can access:
                //no access
                break;
            case "FINANCE TL":
                // Finance TL can access:
                btn_printPayslip.setEnabled(true);
                btn_printPayReport.setEnabled(true);
                btn_werifyPay.setEnabled(true);
                btn_undoVerify.setEnabled(true);
                break;
            case "PAYROLL MANAGER":
                // Payroll Manager can access:
                btn_printPayslip.setEnabled(true);
                btn_printPayReport.setEnabled(true);
                btn_AppPay.setEnabled(true);
                btn_undoApp.setEnabled(true);
                break;
            case "ACCOUNTING":
                // Accounting can access:
                btn_printPayslip.setEnabled(true);
                btn_printPayReport.setEnabled(true);
                break;
            case "ADMIN":
                // Admin can access:
                btn_AppPay.setEnabled(true);
                btn_undoApp.setEnabled(true);
                btn_werifyPay.setEnabled(true);
                btn_undoVerify.setEnabled(true);
                btn_printPayslip.setEnabled(true);
                btn_printPayReport.setEnabled(true);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Invalid role: " + role, "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }
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
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_Payroll = new javax.swing.JTable();
        btn_AppPay = new javax.swing.JButton();
        btn_undoApp = new javax.swing.JButton();
        btn_werifyPay = new javax.swing.JButton();
        btn_undoVerify = new javax.swing.JButton();
        btn_printPayslip = new javax.swing.JButton();
        btn_printPayReport = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
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
        jButton1 = new javax.swing.JButton();
        cal_printpayDateFrom = new com.toedter.calendar.JDateChooser();
        cal_printpayDateTo = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.lightGray, null, null));

        tbl_Payroll.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Transac ID", "EmpID", "FirstName", "LastName", "Payroll Date From", "Payroll Date From", "Basic", "Hourly Rate", "Allowances", "Hrs Per Month", "Hrs Worked", "Adj Earnings", "Total Earnings", "Tardiness Hours", "EE Tax Deduction", "EE SSS Deduction", "EE Pagibig Deduction", "EE Philhealth Deduction", "Adj Deductions", "Total Deduction", "Netpay", "Pay Status"
            }
        ));
        jScrollPane1.setViewportView(tbl_Payroll);

        btn_AppPay.setText("APPROVE PAYMENT");
        btn_AppPay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_AppPayActionPerformed(evt);
            }
        });

        btn_undoApp.setText("UNDO APPROVAL");
        btn_undoApp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_undoAppActionPerformed(evt);
            }
        });

        btn_werifyPay.setText("VERIFY");
        btn_werifyPay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_werifyPayActionPerformed(evt);
            }
        });

        btn_undoVerify.setText("UNDO VERIFICATION");
        btn_undoVerify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_undoVerifyActionPerformed(evt);
            }
        });

        btn_printPayslip.setText("VIEW/PRINT PAYSLIP");
        btn_printPayslip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_printPayslipActionPerformed(evt);
            }
        });

        btn_printPayReport.setText("PRINT REPORT");
        btn_printPayReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_printPayReportActionPerformed(evt);
            }
        });



        jLabel2.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        jLabel2.setText("MotorPH");

        jPanel3.setBackground(new java.awt.Color(255, 255, 102));



        jPanel4.setBackground(new java.awt.Color(245, 28, 71));



        btn_MyRecords.setBackground(new java.awt.Color(51, 51, 255));
        btn_MyRecords.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_MyRecords.setForeground(new java.awt.Color(255, 255, 255));
        btn_MyRecords.setText("My Records");
        btn_MyRecords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_MyRecordsActionPerformed(evt);
            }
        });

        btn_EmpRecords.setBackground(new java.awt.Color(51, 51, 255));
        btn_EmpRecords.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_EmpRecords.setForeground(new java.awt.Color(255, 255, 255));
        btn_EmpRecords.setText("Employee Records");
        btn_EmpRecords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_EmpRecordsActionPerformed(evt);
            }
        });

        btn_Logout.setBackground(new java.awt.Color(51, 51, 255));
        btn_Logout.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_Logout.setForeground(new java.awt.Color(255, 255, 255));
        btn_Logout.setText("Logout");
        btn_Logout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LogoutActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oop_motorph/img/img_myrecord.png"))); // NOI18N

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oop_motorph/img/img_emprecords.png"))); // NOI18N

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oop_motorph/img/img_logout.png"))); // NOI18N

        jPanel5.setBackground(new java.awt.Color(255, 255, 102));



        jPanel6.setBackground(new java.awt.Color(255, 255, 102));



        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        jLabel5.setText("Version 1.30");

        jButton1.setBackground(new java.awt.Color(51, 51, 255));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("BACK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel6.setText("Date From:");

        jLabel7.setText("Date To:");



        jPanel1.getAccessibleContext().setAccessibleName("Profile");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_MyRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_MyRecordsActionPerformed

        new frm_EmpProfile().setVisible(true);
        this.dispose();

    }//GEN-LAST:event_btn_MyRecordsActionPerformed

    private void btn_LogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LogoutActionPerformed

    CSVHandler.handleLogout(this);

    }//GEN-LAST:event_btn_LogoutActionPerformed

    private void btn_AppPayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_AppPayActionPerformed
        changePayrollStatus("Verified", "Approved");
    }//GEN-LAST:event_btn_AppPayActionPerformed

    private void btn_undoAppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_undoAppActionPerformed
        changePayrollStatus("Approved", "Pending");
    }//GEN-LAST:event_btn_undoAppActionPerformed

    private void btn_werifyPayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_werifyPayActionPerformed
        changePayrollStatus("Pending", "Verified");
    }//GEN-LAST:event_btn_werifyPayActionPerformed

    private void btn_undoVerifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_undoVerifyActionPerformed
        changePayrollStatus("Verified", "Pending");
    }//GEN-LAST:event_btn_undoVerifyActionPerformed

    private void changePayrollStatus(String expectedStatus, String newStatus) {
        int selectedRow = tbl_Payroll.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a payroll entry first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String currentStatus = String.valueOf(tbl_Payroll.getValueAt(selectedRow, 21));
        if (!expectedStatus.equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This action requires a payroll entry with status '" + expectedStatus + "'.",
                    "Status unchanged", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String transactionID = String.valueOf(tbl_Payroll.getValueAt(selectedRow, 0));
        String employeeID = String.valueOf(tbl_Payroll.getValueAt(selectedRow, 1));
        String dateFrom = String.valueOf(tbl_Payroll.getValueAt(selectedRow, 4));
        String dateTo = String.valueOf(tbl_Payroll.getValueAt(selectedRow, 5));
        CSVHandler.updatePayrollStatus(transactionID, employeeID, dateFrom, dateTo, newStatus,
                "Payroll status saved successfully.");
        // A failed write leaves the displayed state unchanged.
        tbl_Payroll.setValueAt(newStatus, selectedRow, 21);
        JOptionPane.showMessageDialog(this, "Payroll status saved as " + newStatus + ".",
                "Status saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void btn_EmpRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_EmpRecordsActionPerformed
        new frm_EmployeesRecords().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_EmpRecordsActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         new frm_EmployeesPayrollProcess().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void btn_printPayslipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_printPayslipActionPerformed
    int selectedRow = tbl_Payroll.getSelectedRow();
    String payslip = generatePayslip(selectedRow);

    if (payslip != null) {
        JTextArea textArea = new JTextArea(payslip);
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

        int option = JOptionPane.showConfirmDialog(null, new javax.swing.JScrollPane(textArea),
                                                   "Payslip Preview", JOptionPane.YES_NO_OPTION,
                                                   JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            try {
                boolean complete = textArea.print();
                if (complete) {
                    JOptionPane.showMessageDialog(this, "Printing successful!", "Print", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Printing canceled!", "Print", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Print Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    }//GEN-LAST:event_btn_printPayslipActionPerformed

    private void btn_printPayReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_printPayReportActionPerformed

  if (cal_printpayDateFrom.getDate() == null || cal_printpayDateTo.getDate() == null) {
        JOptionPane.showMessageDialog(this, "Please select both 'Pay Date From' and 'Pay Date To' before printing.",
                                      "Date Selection Required", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Ensure that 'Pay Date From' is not greater than 'Pay Date To'
    if (cal_printpayDateFrom.getDate().after(cal_printpayDateTo.getDate())) {
        JOptionPane.showMessageDialog(this, "'Pay Date From' must be earlier than or equal to 'Pay Date To'.",
                                      "Invalid Date Range", JOptionPane.ERROR_MESSAGE);
        return;
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH);
    String selectedDateFrom = dateFormat.format(cal_printpayDateFrom.getDate());
    String selectedDateTo = dateFormat.format(cal_printpayDateTo.getDate());

    CSVHandler.printApprovedPayroll(tbl_Payroll, selectedDateFrom, selectedDateTo);

    }//GEN-LAST:event_btn_printPayReportActionPerformed

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
            java.util.logging.Logger.getLogger(frm_EmployeesPayrollPaid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frm_EmployeesPayrollPaid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frm_EmployeesPayrollPaid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frm_EmployeesPayrollPaid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>


        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frm_EmployeesPayrollPaid().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_AppPay;
    private javax.swing.JButton btn_EmpRecords;
    private javax.swing.JButton btn_Logout;
    private javax.swing.JButton btn_MyRecords;
    private javax.swing.JButton btn_printPayReport;
    private javax.swing.JButton btn_printPayslip;
    private javax.swing.JButton btn_undoApp;
    private javax.swing.JButton btn_undoVerify;
    private javax.swing.JButton btn_werifyPay;
    private com.toedter.calendar.JDateChooser cal_printpayDateFrom;
    private com.toedter.calendar.JDateChooser cal_printpayDateTo;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbl_Payroll;
    // End of variables declaration//GEN-END:variables
}
