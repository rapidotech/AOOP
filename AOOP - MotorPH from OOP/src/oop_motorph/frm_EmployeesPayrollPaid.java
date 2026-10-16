
package oop_motorph;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class frm_EmployeesPayrollPaid extends javax.swing.JFrame {

   
    public frm_EmployeesPayrollPaid() {
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
        // Approval states belong to the explicit local ledger, not calculated database rows.
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btn_werifyPay, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_undoVerify, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_AppPay, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_undoApp, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                        .addComponent(btn_printPayReport, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_printPayslip, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_AppPay)
                    .addComponent(btn_undoApp)
                    .addComponent(btn_werifyPay)
                    .addComponent(btn_undoVerify)
                    .addComponent(btn_printPayslip)
                    .addComponent(btn_printPayReport))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 471, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel2.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        jLabel2.setText("MotorPH");

        jPanel3.setBackground(new java.awt.Color(255, 255, 102));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 17, Short.MAX_VALUE)
        );

        jPanel4.setBackground(new java.awt.Color(245, 28, 71));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

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

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 102));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addGap(42, 42, 42))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(64, 64, 64)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel1)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btn_EmpRecords)
                                    .addComponent(btn_MyRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btn_Logout, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(62, 62, 62)
                                .addComponent(jLabel4)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)))
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(jLabel2)
                .addGap(145, 145, 145)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cal_printpayDateFrom, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(116, 116, 116)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cal_printpayDateTo, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jButton1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cal_printpayDateFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel6))
                            .addComponent(cal_printpayDateTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_MyRecords)
                        .addGap(36, 36, 36)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_EmpRecords)
                        .addGap(42, 42, 42)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_Logout))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

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
