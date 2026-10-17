package oop_motorph;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

public class frm_EmpAttLeave extends javax.swing.JFrame {

    
    public frm_EmpAttLeave() {
        AuthorizationService.requireAuthenticated();
        initComponents();
        AppTheme.finish(this);


        displayLoggedInEmployeeAttendance();
    
        EmpDetails employee = EmpUserSession.getInstance().getCurrentUser();
        if (employee != null) {
            String role = EmpUserSession.getInstance().getRole();
            if (role == null) role = "EMPLOYEE";
            setRoleBasedAccess(role);
        } else {
            JOptionPane.showMessageDialog(this, "User not logged in!", "Error", JOptionPane.ERROR_MESSAGE);
        }

        composeWorkspace();
    }

public void setRoleBasedAccess(String role) {
        // Disable all by default
        btn_MyRecords.setEnabled(false);
        btn_SalaryAndStatutory.setEnabled(false);
        btn_Profile.setEnabled(false);
        btn_Logout.setEnabled(false);
        btn_EmpRecords.setEnabled(false);
        btn_PayrollSummary.setEnabled(false);
        btn_LeaveApproval.setEnabled(false);
        btn_LeaveRequest.setEnabled(false);
        btn_LeaveMgt.setEnabled(false); // Include if this button exists

        switch (role.toUpperCase()) {
            case "EMPLOYEE" -> {
                btn_MyRecords.setEnabled(true);
                btn_SalaryAndStatutory.setEnabled(true);
                btn_Profile.setEnabled(true);
                btn_Logout.setEnabled(true);
                btn_PayrollSummary.setEnabled(true);
                btn_LeaveRequest.setEnabled(true);
            }

            case "HR" -> {
                btn_MyRecords.setEnabled(true);
                btn_SalaryAndStatutory.setEnabled(true);
                btn_Profile.setEnabled(true);
                btn_EmpRecords.setEnabled(true);
                btn_Logout.setEnabled(true);
                btn_PayrollSummary.setEnabled(true);
                btn_LeaveApproval.setEnabled(true);
                btn_LeaveRequest.setEnabled(true);
                btn_LeaveMgt.setEnabled(true);
            }

            default -> {
                JOptionPane.showMessageDialog(this,
                        "Access denied: This section is for HR or Employee roles only.",
                        "Permission Denied", JOptionPane.WARNING_MESSAGE);
                dispose(); // Close the window if role is unauthorized
            }
        }
    }

// To display the data of the logged in User
    private void displayLoggedInEmployeeAttendance() {
        List<EmpAttLeave> attendanceList = CSVHandler.getAttendanceData();
        DefaultTableModel model = (DefaultTableModel) tbl_Attendance.getModel();
  
        //to disable tbl edit
        tbl_Attendance.setDefaultEditor(Object.class, null);
    
        // Clear existing data
        model.setRowCount(0);

        // Get the currently logged-in employee
        EmpDetails employee = EmpUserSession.getInstance().getCurrentUser();
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "User not logged in!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String loggedInEmpID = employee.getEmpID(); // Get Employee ID
        
        // Variables to track leave summary
        double totalVlCount = 0, totalVlUsed = 0, totalSlCount = 0, totalSlUsed = 0;
        double vlBalance = 0, slBalance = 0;

        // Filter and display only the logged-in employee's records
        for (EmpAttLeave att : attendanceList) {
            if (att.getEmpID().equals(loggedInEmpID)) {
                model.addRow(new Object[]{
                    att.getEmpID(), att.getFirstName(), att.getLastName(), att.getEmployeeStatus(),
                    att.getPosition(), att.getImmediateSupervisor(), att.getAttDateFrom(), att.getAttDateTo(), 
                    att.getTimeIn(), att.getTimeOut(), att.getHoursWorked(), att.getDuration(), 
                    att.getAttendanceType(), att.getAttendanceStatus(), att.getVlCount(), 
                    att.getVlUsed(), att.getVlBalance(), att.getSlCount(), att.getSlUsed(), att.getSlBalance()
                });
                
                // Update leave summary (taking the last values found)
                totalVlCount = parseDoubleSafe(att.getVlCount());
                totalVlUsed = parseDoubleSafe(att.getVlUsed());
                vlBalance = parseDoubleSafe(att.getVlBalance());
                totalSlCount = parseDoubleSafe(att.getSlCount());
                totalSlUsed = parseDoubleSafe(att.getSlUsed());
                slBalance = parseDoubleSafe(att.getSlBalance());
            }
        }
        
        // Update the text fields with leave summary
        txt_vlCount.setText(String.valueOf(totalVlCount));
        txt_vlUsed.setText(String.valueOf(totalVlUsed));
        txt_vlBal.setText(String.valueOf(vlBalance));
        txt_slCount.setText(String.valueOf(totalSlCount));
        txt_slUsed.setText(String.valueOf(totalSlUsed));
        txt_slBal.setText(String.valueOf(slBalance));
    }

    /** Source-owned responsive composition; legacy fixed layout is retired. */
    private void composeWorkspace() {
        WorkspaceShell.bindNavigation(this, "profile", btn_Profile);
        WorkspaceShell.bindNavigation(this, "salary", btn_SalaryAndStatutory);
        WorkspaceShell.bindNavigation(this, "attendance", btn_LeaveMgt);
        WorkspaceShell.bindNavigation(this, "payroll", btn_PayrollSummary);
        WorkspaceShell.bindNavigation(this, "employees", btn_EmpRecords);
        WorkspaceShell.bindNavigation(this, "logout", btn_Logout);
        javax.swing.JPanel content = new javax.swing.JPanel(new java.awt.BorderLayout(0, 20));
        content.setOpaque(false);
        javax.swing.JComponent balances = WorkspaceUI.section("Leave balances", "Values from your attendance and leave record", WorkspaceUI.fields(
            "Vacation leave allocation", txt_vlCount, "Sick leave allocation", txt_slCount,
            "Vacation leave used", txt_vlUsed, "Sick leave used", txt_slUsed,
            "Vacation leave balance", txt_vlBal, "Sick leave balance", txt_slBal));
        content.add(balances, java.awt.BorderLayout.NORTH);
        content.add(WorkspaceUI.table(tbl_Attendance), java.awt.BorderLayout.CENTER);
        WorkspaceUI.primary(btn_LeaveRequest, "Leave / attendance request");
        btn_LeaveApproval.setText("Review requests");
        WorkspaceShell.mount(this, "Attendance & leave", "Time logs and submitted requests", "attendance", content,
            WorkspaceUI.actions(btn_LeaveRequest, btn_LeaveApproval));
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
        btn_LeaveRequest = new javax.swing.JButton();
        btn_LeaveApproval = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_Attendance = new javax.swing.JTable();
        txt_vlCount = new javax.swing.JTextField();
        txt_vlUsed = new javax.swing.JTextField();
        txt_vlBal = new javax.swing.JTextField();
        txt_slCount = new javax.swing.JTextField();
        txt_slUsed = new javax.swing.JTextField();
        txt_slBal = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
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

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.lightGray, null, null));

        btn_LeaveRequest.setBackground(new java.awt.Color(0, 0, 204));
        btn_LeaveRequest.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_LeaveRequest.setForeground(new java.awt.Color(255, 255, 255));
        btn_LeaveRequest.setText("Leave Request");
        btn_LeaveRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LeaveRequestActionPerformed(evt);
            }
        });

        btn_LeaveApproval.setBackground(new java.awt.Color(0, 0, 204));
        btn_LeaveApproval.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_LeaveApproval.setForeground(new java.awt.Color(255, 255, 255));
        btn_LeaveApproval.setText("Leave Approval");
        btn_LeaveApproval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LeaveApprovalActionPerformed(evt);
            }
        });

        tbl_Attendance.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Employee ID", "First Name", "Last Name", "Status", "Position", "Immediate Supervisor", "Date From", "Date To", "Time In", "Time Out", "Hours Worked", "Duration", "AttendanceType", "AttendanceStatus", "VL Count", "VL Used", "VL Balance", "SL Count", "SL Used", "SL Balance"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tbl_Attendance);

        txt_vlCount.setEditable(false);

        txt_vlUsed.setEditable(false);

        txt_vlBal.setEditable(false);

        txt_slCount.setEditable(false);

        txt_slUsed.setEditable(false);

        txt_slBal.setEditable(false);

        jLabel6.setText("VL Count:");

        jLabel7.setText("VL Used:");

        jLabel8.setText("VL Balance:");

        jLabel9.setText("SL Count:");

        jLabel10.setText("SL Used:");

        jLabel11.setText("SL Balance:");


        jLabel2.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 18)); // NOI18N
        jLabel2.setText("MotorPH");

        jPanel3.setBackground(new java.awt.Color(255, 255, 102));


        btn_Profile.setBackground(new java.awt.Color(51, 51, 255));
        btn_Profile.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_Profile.setForeground(new java.awt.Color(255, 255, 255));
        btn_Profile.setText("Profile");
        btn_Profile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ProfileActionPerformed(evt);
            }
        });

        btn_LeaveMgt.setBackground(new java.awt.Color(51, 51, 255));
        btn_LeaveMgt.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_LeaveMgt.setForeground(new java.awt.Color(255, 255, 0));
        btn_LeaveMgt.setText("Attendance & Leave");
        btn_LeaveMgt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LeaveMgtActionPerformed(evt);
            }
        });

        btn_SalaryAndStatutory.setBackground(new java.awt.Color(51, 51, 255));
        btn_SalaryAndStatutory.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_SalaryAndStatutory.setForeground(new java.awt.Color(255, 255, 255));
        btn_SalaryAndStatutory.setText("Salary & Statutory");
        btn_SalaryAndStatutory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SalaryAndStatutoryActionPerformed(evt);
            }
        });

        btn_PayrollSummary.setBackground(new java.awt.Color(51, 51, 255));
        btn_PayrollSummary.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_PayrollSummary.setForeground(new java.awt.Color(255, 255, 255));
        btn_PayrollSummary.setText("Payroll Summary");
        btn_PayrollSummary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_PayrollSummaryActionPerformed(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(245, 28, 71));


        btn_MyRecords.setBackground(new java.awt.Color(51, 51, 255));
        btn_MyRecords.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_MyRecords.setForeground(new java.awt.Color(255, 255, 0));
        btn_MyRecords.setText("My Record");
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
        new frm_EmpProfile().setVisible(true);
        this.dispose();
        
    }//GEN-LAST:event_btn_MyRecordsActionPerformed

    private void btn_LogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LogoutActionPerformed
        
        CSVHandler.handleLogout(this); 
        
    }//GEN-LAST:event_btn_LogoutActionPerformed

    private void btn_LeaveRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LeaveRequestActionPerformed
    int selectedRow = tbl_Attendance.getSelectedRow();
    
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "Please select any row.", "Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    try {
        // Debugging: Check the number of columns
        int columnCount = tbl_Attendance.getColumnCount();
        System.out.println("Total Columns: " + columnCount);

        if (columnCount < 14) { // Adjust based on actual column count in the table
            JOptionPane.showMessageDialog(this, "Table column count mismatch. Expected at least 14 columns.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Define date and time formatters
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm"); // Adjust format if needed

        // Retrieve JTextField values
        String empID = tbl_Attendance.getValueAt(selectedRow, 0).toString();
        String firstName = tbl_Attendance.getValueAt(selectedRow, 1).toString();
        String lastName = tbl_Attendance.getValueAt(selectedRow, 2).toString();
        String empStatus = tbl_Attendance.getValueAt(selectedRow, 3).toString();
        String position = tbl_Attendance.getValueAt(selectedRow, 4).toString();
        String immSupervisor = tbl_Attendance.getValueAt(selectedRow, 5).toString();

        // Convert String to LocalDate
        LocalDate attdateFrom = LocalDate.parse(tbl_Attendance.getValueAt(selectedRow, 6).toString(), dateFormatter);
        LocalDate attdateTo = LocalDate.parse(tbl_Attendance.getValueAt(selectedRow, 7).toString(), dateFormatter);

        // Convert String to LocalTime
        LocalTime timeIn = LocalTime.parse(tbl_Attendance.getValueAt(selectedRow, 8).toString(), timeFormatter);
        LocalTime timeOut = LocalTime.parse(tbl_Attendance.getValueAt(selectedRow, 9).toString(), timeFormatter);

        // Retrieve numeric values safely
        double hoursWorked = parseDoubleSafe(tbl_Attendance.getValueAt(selectedRow, 10));
        double duration = parseDoubleSafe(tbl_Attendance.getValueAt(selectedRow, 11));

        // Retrieve JComboBox values correctly
        String attendanceType = tbl_Attendance.getValueAt(selectedRow, 12).toString();
        String attendanceStatus = tbl_Attendance.getValueAt(selectedRow, 13).toString();

        // Retrieve numeric values for leave balances
        double vlCount = parseDoubleSafe(tbl_Attendance.getValueAt(selectedRow, 14));
        double vlUsed = parseDoubleSafe(tbl_Attendance.getValueAt(selectedRow, 15));
        double vlBal = parseDoubleSafe(tbl_Attendance.getValueAt(selectedRow, 16));
        double slCount = parseDoubleSafe(tbl_Attendance.getValueAt(selectedRow, 17));
        double slUsed = parseDoubleSafe(tbl_Attendance.getValueAt(selectedRow, 18));
        double slBal = parseDoubleSafe(tbl_Attendance.getValueAt(selectedRow, 19));

        // Create the employee attendance object
        EmpAttLeave employeeAttendance = new EmpAttLeave(
            empID, firstName, lastName, empStatus, position, immSupervisor,
            attdateFrom, attdateTo, timeIn, timeOut, hoursWorked, duration,
            attendanceType, attendanceStatus, vlCount, vlUsed, vlBal,
            slCount, slUsed, slBal
        );

        // Open the attendance request form and pass data
        frm_EmpAddAttLeave frm2 = new frm_EmpAddAttLeave(employeeAttendance, false);
        frm2.setVisible(true);

    } catch (DateTimeParseException e) {
        JOptionPane.showMessageDialog(this, "Invalid date/time format: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Invalid numeric format: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error processing attendance data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}

    // Helper method to safely parse doubles
    private double parseDoubleSafe(Object value) {
    try {
        return Double.parseDouble(value.toString());
    } catch (NumberFormatException e) {
        return 0.0;  // Default value if parsing fails
    }
          
    }//GEN-LAST:event_btn_LeaveRequestActionPerformed

    private void btn_EmpRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_EmpRecordsActionPerformed
        new frm_EmployeesRecords().setVisible(true);
        this.dispose();
        
    }//GEN-LAST:event_btn_EmpRecordsActionPerformed

    private void btn_LeaveApprovalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LeaveApprovalActionPerformed
        new frm_EmpAttLeaveApproval().setVisible(true);
       
    }//GEN-LAST:event_btn_LeaveApprovalActionPerformed

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
            java.util.logging.Logger.getLogger(frm_EmpAttLeave.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frm_EmpAttLeave.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frm_EmpAttLeave.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frm_EmpAttLeave.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frm_EmpAttLeave().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_EmpRecords;
    private javax.swing.JButton btn_LeaveApproval;
    private javax.swing.JButton btn_LeaveMgt;
    private javax.swing.JButton btn_LeaveRequest;
    private javax.swing.JButton btn_Logout;
    private javax.swing.JButton btn_MyRecords;
    private javax.swing.JButton btn_PayrollSummary;
    private javax.swing.JButton btn_Profile;
    private javax.swing.JButton btn_SalaryAndStatutory;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
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
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbl_Attendance;
    private javax.swing.JTextField txt_slBal;
    private javax.swing.JTextField txt_slCount;
    private javax.swing.JTextField txt_slUsed;
    private javax.swing.JTextField txt_vlBal;
    private javax.swing.JTextField txt_vlCount;
    private javax.swing.JTextField txt_vlUsed;
    // End of variables declaration//GEN-END:variables

}
