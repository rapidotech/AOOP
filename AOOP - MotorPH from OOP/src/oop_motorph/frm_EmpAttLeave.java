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
        initComponents();
        AppTheme.finish(this);
        
        // Set window properties - larger size to show all content
        setResizable(true);
        setTitle("Employee Attendance");
        setSize(1400, 950); // Increased size to accommodate all content
        setMinimumSize(new java.awt.Dimension(1150, 800));
        setLocationRelativeTo(null); // Center the window after setting size
        
        // Apply modern styling after components are initialized
        applyModernStyling();
        addHoverEffects();
        
        // Icons are already properly positioned by the form designer - no need to reposition
        
        displayLoggedInEmployeeAttendance();
    
        EmpDetails employee = EmpUserSession.getInstance().getCurrentUser();
        if (employee != null) {
            String role = EmpUserSession.getInstance().getRole();
            if (role == null) role = "EMPLOYEE";
            setRoleBasedAccess(role);
        } else {
            JOptionPane.showMessageDialog(this, "User not logged in!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        // Force button sizes after everything is fully initialized
        forceButtonSizes();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        
        // Force navigation button sizes after layout
        java.awt.Dimension navButtonSize = new java.awt.Dimension(200, 45);
        
        if (btn_Profile != null) {
            btn_Profile.setSize(navButtonSize);
            btn_Profile.setPreferredSize(navButtonSize);
        }
        if (btn_LeaveMgt != null) {
            btn_LeaveMgt.setSize(navButtonSize);
            btn_LeaveMgt.setPreferredSize(navButtonSize);
        }
        if (btn_SalaryAndStatutory != null) {
            btn_SalaryAndStatutory.setSize(navButtonSize);
            btn_SalaryAndStatutory.setPreferredSize(navButtonSize);
        }
        if (btn_PayrollSummary != null) {
            btn_PayrollSummary.setSize(navButtonSize);
            btn_PayrollSummary.setPreferredSize(navButtonSize);
        }
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
    
    private void forceButtonSizes() {
        // Use SwingUtilities.invokeLater to ensure this runs after layout is complete
        javax.swing.SwingUtilities.invokeLater(() -> {
            // Force navigation button sizes to ensure they match the profile form
            java.awt.Dimension navButtonSize = new java.awt.Dimension(200, 45);
            
            // Set sizes for all navigation buttons
            javax.swing.JButton[] navButtons = {btn_Profile, btn_LeaveMgt, btn_SalaryAndStatutory, btn_PayrollSummary};
            
            for (javax.swing.JButton button : navButtons) {
                button.setPreferredSize(navButtonSize);
                button.setMinimumSize(navButtonSize);
                button.setMaximumSize(navButtonSize);
                button.setSize(navButtonSize);
                
                // Force the button to maintain its size
                button.putClientProperty("JComponent.sizeVariant", "regular");
            }
            
            // Force the entire form to revalidate and repaint
            this.revalidate();
            this.repaint();
            
            // Also force each button to revalidate
            for (javax.swing.JButton button : navButtons) {
                button.revalidate();
                button.repaint();
            }
        });
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(189, 189, 189)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(btn_LeaveRequest, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txt_vlBal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
                            .addComponent(txt_vlUsed, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txt_vlCount))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 173, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btn_LeaveApproval, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel11)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_slUsed, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_slCount, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_slBal, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(173, 173, 173))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_LeaveRequest)
                    .addComponent(btn_LeaveApproval))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txt_slCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_vlCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel10)
                        .addComponent(txt_slUsed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txt_vlUsed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel7)))
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_slBal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txt_vlBal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
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

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1032, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(56, 56, 56))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel5))
                                .addGap(67, 67, 67))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(72, 72, 72)
                                .addComponent(jLabel4)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btn_EmpRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btn_MyRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btn_Logout, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(31, 31, 31))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addGap(55, 55, 55)))))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btn_Profile, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55)
                        .addComponent(btn_LeaveMgt, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(btn_SalaryAndStatutory, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addComponent(btn_PayrollSummary, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_Profile)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_LeaveMgt)
                    .addComponent(btn_SalaryAndStatutory)
                    .addComponent(btn_PayrollSummary))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addGap(37, 37, 37)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
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
    
    private void applyModernStyling() {
        // Style the main container to match Profile page layout
        jPanel1.setLayout(new java.awt.BorderLayout());
        jPanel1.setBackground(new Color(249, 250, 251));
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Main content area styling
        jPanel2.setBackground(new Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
            javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        // Ensure navigation panel has proper styling
        jPanel3.setBackground(new Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Style sidebar container with proper background and layout
        if (jPanel4 != null) {
            jPanel4.setBackground(new Color(255, 255, 255));
            jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 15, 20, 15));
        }
        if (jPanel5 != null) {
            jPanel5.setBackground(new Color(255, 255, 255));
        }
        if (jPanel6 != null) {
            jPanel6.setBackground(new Color(255, 255, 255));
        }
        
        // Hide MotorPH logo and version text to match profile page
        if (jLabel2 != null) {
            jLabel2.setVisible(false); // MotorPH logo
        }
        if (jLabel5 != null) {
            jLabel5.setVisible(false); // Version text
        }
        
        // Style text fields with enhanced modern borders
        styleTextFieldEnhanced(txt_vlCount);
        styleTextFieldEnhanced(txt_vlUsed);
        styleTextFieldEnhanced(txt_vlBal);
        styleTextFieldEnhanced(txt_slCount);
        styleTextFieldEnhanced(txt_slUsed);
        styleTextFieldEnhanced(txt_slBal);
        
        // Style labels with enhanced modern typography
        styleLabelEnhanced(jLabel6, false);
        styleLabelEnhanced(jLabel7, false);
        styleLabelEnhanced(jLabel8, false);
        styleLabelEnhanced(jLabel9, false);
        styleLabelEnhanced(jLabel10, false);
        styleLabelEnhanced(jLabel11, false);
        
        // Style attendance table
        styleTableEnhanced();
        
        // Style action buttons
        styleActionButtonEnhanced(btn_LeaveRequest, new Color(59, 130, 246));
        styleActionButtonEnhanced(btn_LeaveApproval, new Color(59, 130, 246));
        
        // Style navigation buttons with enhanced modern design and better spacing
        styleNavigationButtonEnhanced(btn_Profile, new Color(107, 114, 128), false);
        styleNavigationButtonEnhanced(btn_LeaveMgt, new Color(34, 197, 94), true); // Active green for attendance
        styleNavigationButtonEnhanced(btn_SalaryAndStatutory, new Color(107, 114, 128), false);
        styleNavigationButtonEnhanced(btn_PayrollSummary, new Color(107, 114, 128), false);
        
        // Style left sidebar buttons to match Profile page exactly
        styleSidebarButtonEnhanced(btn_MyRecords, new Color(107, 114, 128), false);
        styleSidebarButtonEnhanced(btn_EmpRecords, new Color(107, 114, 128), false);
        styleSidebarButtonEnhanced(btn_Logout, new Color(239, 68, 68), false); // Red for logout
    }
    
    private void styleTextFieldEnhanced(javax.swing.JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(249, 250, 251));
        field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(1, 1, 2, 2),
                javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 6), 1)
            ),
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                javax.swing.BorderFactory.createEmptyBorder(10, 16, 10, 16)
            )
        ));
        field.setForeground(new Color(55, 65, 81));
    }
    
    private void styleLabelEnhanced(javax.swing.JLabel label, boolean isTitle) {
        if (isTitle) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 22));
            label.setForeground(new Color(17, 24, 39));
        } else {
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(new Color(75, 85, 99));
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0));
            // Add right alignment for consistent label positioning
            label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        }
    }
    
    private void styleTableEnhanced() {
        // Style the table with modern appearance to match profile page
        tbl_Attendance.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl_Attendance.setRowHeight(38);
        tbl_Attendance.setGridColor(new Color(229, 231, 235));
        tbl_Attendance.setSelectionBackground(new Color(219, 234, 254));
        tbl_Attendance.setSelectionForeground(new Color(30, 64, 175));
        tbl_Attendance.setBackground(new Color(255, 255, 255));
        tbl_Attendance.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tbl_Attendance.getTableHeader().setBackground(new Color(249, 250, 251));
        tbl_Attendance.getTableHeader().setForeground(new Color(55, 65, 81));
        tbl_Attendance.getTableHeader().setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(209, 213, 219)));
        tbl_Attendance.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 40));
        
        // Style the scroll pane to match profile page
        jScrollPane1.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(2, 2, 4, 4),
                javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 8), 1)
            ),
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2)
            )
        ));
        jScrollPane1.setBackground(new Color(255, 255, 255));
        jScrollPane1.getViewport().setBackground(new Color(255, 255, 255));
    }
    
    private void styleActionButtonEnhanced(javax.swing.JButton button, Color backgroundColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(backgroundColor);
        button.setForeground(new Color(255, 255, 255));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Make buttons larger to match profile page styling
        button.setPreferredSize(new java.awt.Dimension(150, 45));
        button.setMinimumSize(new java.awt.Dimension(150, 45));
        
        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(2, 2, 4, 4),
                javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 8), 1)
            ),
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(backgroundColor.darker(), 1),
                javax.swing.BorderFactory.createEmptyBorder(10, 18, 10, 18)
            )
        ));
    }
    
    private void styleNavigationButtonEnhanced(javax.swing.JButton button, Color backgroundColor, boolean isActive) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(new Color(255, 255, 255));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Set consistent preferred size for all navigation buttons - wider for better text display
        button.setPreferredSize(new java.awt.Dimension(200, 45));
        button.setMinimumSize(new java.awt.Dimension(200, 45));
        button.setMaximumSize(new java.awt.Dimension(200, 45));
        
        // Ensure text is centered and properly aligned
        button.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        button.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        
        if (isActive) {
            // Active button with enhanced border highlight and shadow
            button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createEmptyBorder(1, 1, 3, 3),
                    javax.swing.BorderFactory.createLineBorder(new Color(34, 197, 94, 40), 1)
                ),
                javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new Color(34, 197, 94), 2),
                    javax.swing.BorderFactory.createEmptyBorder(10, 16, 10, 16)
                )
            ));
        } else {
            // Regular button with subtle shadow and generous padding
            button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(1, 1, 3, 3),
                javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 8), 1),
                    javax.swing.BorderFactory.createEmptyBorder(12, 18, 12, 18)
                )
            ));
        }
    }
    
    private void styleSidebarButtonEnhanced(javax.swing.JButton button, Color backgroundColor, boolean isActive) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Match Profile page font exactly
        button.setBackground(backgroundColor);
        button.setForeground(new Color(255, 255, 255));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Force exact uniform size for ALL sidebar buttons - exactly like Profile page
        button.setPreferredSize(new java.awt.Dimension(170, 45));
        button.setMinimumSize(new java.awt.Dimension(170, 45));
        button.setMaximumSize(new java.awt.Dimension(170, 45));
        button.setSize(170, 45);
        
        // Perfect alignment and spacing for sidebar buttons - exactly like Profile page
        button.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        button.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        // Clean, uniform styling for all sidebar buttons - exactly matching Profile page
        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(2, 2, 4, 4),
                javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 8), 1)
            ),
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(156, 163, 175), 1),
                javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12)
            )
        ));
    }
    
    private void addHoverEffects() {
        addButtonHoverEnhanced(btn_Profile, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_LeaveMgt, new Color(34, 197, 94), new Color(22, 163, 74));
        addButtonHoverEnhanced(btn_SalaryAndStatutory, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_PayrollSummary, new Color(107, 114, 128), new Color(75, 85, 99));
        
        // Add hover effects for sidebar buttons
        addButtonHoverEnhanced(btn_MyRecords, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_EmpRecords, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_Logout, new Color(239, 68, 68), new Color(220, 38, 38));
        
        // Add hover effects for action buttons
        addButtonHoverEnhanced(btn_LeaveRequest, new Color(59, 130, 246), new Color(37, 99, 235));
        addButtonHoverEnhanced(btn_LeaveApproval, new Color(59, 130, 246), new Color(37, 99, 235));
    }
    
    private void addButtonHoverEnhanced(javax.swing.JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(normalColor);
                }
            }
        });
    }
}
