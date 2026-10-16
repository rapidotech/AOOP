package oop_motorph;

import javax.swing.table.DefaultTableModel;
import java.util.List;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.Cursor;

public class frm_EmployeesRecords extends javax.swing.JFrame {

    public frm_EmployeesRecords() {
        AuthorizationService.requireHr();
        initComponents();
        AppTheme.finish(this);
        
        // Set window properties - larger size to show all content
        setResizable(true);
        setTitle("Employee Records");
        setSize(1400, 950); // Increased size to accommodate all content
        setMinimumSize(new java.awt.Dimension(1150, 800));
        setLocationRelativeTo(null); // Center the window after setting size
        
        // Apply modern styling after components are initialized
        applyModernStyling();
        addHoverEffects();

        // Display employee data in table
        displayEmployeeData();
        
        EmpDetails employee = EmpUserSession.getInstance().getCurrentUser();
        String role = EmpUserSession.getInstance().getRole();
        
        if (employee != null && role != null) {
            // Set role-based access
            setRoleBasedAccess(role);
        } else {
            JOptionPane.showMessageDialog(this, "No employee data or role found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayEmployeeData() {
        List<EmpDetails> employees = CSVHandler.getEmployeeData();
        DefaultTableModel model = (DefaultTableModel) tbl_Employees.getModel();
        
        // Disable table editing
        tbl_Employees.setDefaultEditor(Object.class, null);
        
        // Clear existing rows
        model.setRowCount(0);

        // Add all employees to the table
        for (EmpDetails emp : employees) {
            model.addRow(new Object[]{
                emp.getEmpID(), emp.getFirstName(), emp.getLastName(), emp.getBirthdate(),
                emp.getAddress(), emp.getPhoneNumber(), emp.getEmployeeStatus(),
                emp.getPosition(), emp.getImmediateSupervisor()
            });
        }
        model.fireTableDataChanged();  
    }

    private void applyModernStyling() {
        // Style the main panels with clean background
        jPanel1.setBackground(new Color(249, 250, 251));
        jPanel2.setBackground(new Color(255, 255, 255));
        jPanel3.setBackground(new Color(255, 255, 255));
        
        // Style navigation panel
        if (jPanel4 != null) {
            jPanel4.setBackground(new Color(255, 255, 255));
        }
        if (jPanel5 != null) {
            jPanel5.setBackground(new Color(255, 255, 255));
        }
        if (jPanel6 != null) {
            jPanel6.setBackground(new Color(255, 255, 255));
        }
        
        // Add enhanced card-like styling to main content panel - reduced padding for more space
        jPanel2.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(4, 4, 8, 8),
                javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 8), 1)
            ),
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                javax.swing.BorderFactory.createEmptyBorder(16, 20, 16, 20)            )
        ));
        
        // Style the navigation area with minimal border - reduced padding since title is removed
        jPanel3.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        
        // Style the table with enhanced styling
        styleTableEnhanced();
        
        // Style action buttons with enhanced styling
        styleActionButtonEnhanced(btn_ViewEmp, new Color(59, 130, 246));
        styleActionButtonEnhanced(btn_AddEmp, new Color(34, 197, 94));
        styleActionButtonEnhanced(btn_DelEmp, new Color(239, 68, 68));
        styleActionButtonEnhanced(btn_refresh, new Color(107, 114, 128));
        
        // Style navigation buttons with enhanced modern design and better spacing
        styleNavigationButtonEnhanced(btn_Profile, new Color(34, 197, 94), true); // Active green - this is the Employee Records page
        styleNavigationButtonEnhanced(btn_LeaveMgt, new Color(107, 114, 128), false);
        styleNavigationButtonEnhanced(btn_SalaryAndStatutory, new Color(107, 114, 128), false);
        styleNavigationButtonEnhanced(btn_PayrollSummary, new Color(107, 114, 128), false);
        
        // Style left sidebar buttons with different sizing
        styleSidebarButtonEnhanced(btn_MyRecords, new Color(107, 114, 128), false);
        styleSidebarButtonEnhanced(btn_EmpRecords, new Color(34, 197, 94), true); // Active green
        styleSidebarButtonEnhanced(btn_Logout, new Color(239, 68, 68), false); // Red for logout
        
        // Add minimal spacing improvements to the overall layout - reduced top padding
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 12, 12, 12));
    }
    
    private void styleTableEnhanced() {
        tbl_Employees.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl_Employees.setBackground(new Color(255, 255, 255));
        tbl_Employees.setForeground(new Color(55, 65, 81));
        tbl_Employees.setGridColor(new Color(229, 231, 235));
        tbl_Employees.setRowHeight(40);
        tbl_Employees.setShowVerticalLines(true);
        tbl_Employees.setShowHorizontalLines(true);
        tbl_Employees.setIntercellSpacing(new java.awt.Dimension(1, 1));
        
        // Style table header
        if (tbl_Employees.getTableHeader() != null) {
            tbl_Employees.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            tbl_Employees.getTableHeader().setBackground(new Color(249, 250, 251));
            tbl_Employees.getTableHeader().setForeground(new Color(75, 85, 99));
            tbl_Employees.getTableHeader().setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));
        }
        
        // Style scroll pane
        if (jScrollPane1 != null) {
            jScrollPane1.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
                javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));
        }
    }
    
    private void styleActionButtonEnhanced(javax.swing.JButton button, Color backgroundColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 11));
        button.setBackground(backgroundColor);
        button.setForeground(new Color(255, 255, 255));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Set consistent preferred size for action buttons
        button.setPreferredSize(new java.awt.Dimension(80, 30));
        button.setMinimumSize(new java.awt.Dimension(80, 30));
        
        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createEmptyBorder(1, 1, 2, 2),
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 8), 1),
                javax.swing.BorderFactory.createEmptyBorder(6, 12, 6, 12)
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
        button.setPreferredSize(new java.awt.Dimension(220, 45));
        button.setMinimumSize(new java.awt.Dimension(220, 45));
    }
    
    private void styleSidebarButtonEnhanced(javax.swing.JButton button, Color backgroundColor, boolean isActive) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(new Color(255, 255, 255));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Set consistent preferred size for all sidebar buttons
        button.setPreferredSize(new java.awt.Dimension(170, 45));
        button.setMinimumSize(new java.awt.Dimension(170, 45));
    }
    
    private void addHoverEffects() {
        // Add hover effects to navigation buttons
        addButtonHoverEnhanced(btn_Profile, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_LeaveMgt, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_SalaryAndStatutory, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_PayrollSummary, new Color(107, 114, 128), new Color(75, 85, 99));
        
        // Add hover effects to sidebar buttons
        addButtonHoverEnhanced(btn_MyRecords, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_EmpRecords, new Color(34, 197, 94), new Color(22, 163, 74));
        addButtonHoverEnhanced(btn_Logout, new Color(239, 68, 68), new Color(220, 38, 38));
        
        // Add hover effects to action buttons
        addButtonHoverEnhanced(btn_ViewEmp, new Color(59, 130, 246), new Color(37, 99, 235));
        addButtonHoverEnhanced(btn_AddEmp, new Color(34, 197, 94), new Color(22, 163, 74));
        addButtonHoverEnhanced(btn_DelEmp, new Color(239, 68, 68), new Color(220, 38, 38));
        addButtonHoverEnhanced(btn_refresh, new Color(107, 114, 128), new Color(75, 85, 99));
    }
    
    private void addButtonHoverEnhanced(javax.swing.JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(normalColor);
            }
        });
    }

    // Set role-based access controls
    private void setRoleBasedAccess(String role) {
        // Implementation for role-based access
        switch (role.toUpperCase()) {
            case "EMPLOYEE":
                btn_AddEmp.setEnabled(false);
                btn_DelEmp.setEnabled(false);
                btn_ViewEmp.setEnabled(true);
                btn_refresh.setEnabled(true);
                break;
            case "HR":
                btn_AddEmp.setEnabled(true);
                btn_DelEmp.setEnabled(true);
                btn_ViewEmp.setEnabled(true);
                btn_refresh.setEnabled(true);
                break;
            default:
                btn_AddEmp.setEnabled(false);
                btn_DelEmp.setEnabled(false);
                btn_ViewEmp.setEnabled(false);
                btn_refresh.setEnabled(false);
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
        tbl_Employees = new javax.swing.JTable();
        btn_ViewEmp = new javax.swing.JButton();
        btn_AddEmp = new javax.swing.JButton();
        btn_DelEmp = new javax.swing.JButton();
        btn_refresh = new javax.swing.JButton();
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

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.lightGray, null, null));

        tbl_Employees.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Employee ID", "First Name", "Last Name", "Birthday", "Address", "Phone Number", "Status", "Position", "Immediate Supervisor"
            }
        ));
        jScrollPane1.setViewportView(tbl_Employees);

        btn_ViewEmp.setBackground(new java.awt.Color(0, 0, 204));
        btn_ViewEmp.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_ViewEmp.setForeground(new java.awt.Color(255, 255, 255));
        btn_ViewEmp.setText("View Employee");
        btn_ViewEmp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ViewEmpActionPerformed(evt);
            }
        });

        btn_AddEmp.setBackground(new java.awt.Color(0, 0, 204));
        btn_AddEmp.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_AddEmp.setForeground(new java.awt.Color(255, 255, 255));
        btn_AddEmp.setText("ADD");
        btn_AddEmp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_AddEmpActionPerformed(evt);
            }
        });

        btn_DelEmp.setBackground(new java.awt.Color(0, 0, 204));
        btn_DelEmp.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_DelEmp.setForeground(new java.awt.Color(255, 255, 255));
        btn_DelEmp.setText("DEL");
        btn_DelEmp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_DelEmpActionPerformed(evt);
            }
        });

        btn_refresh.setBackground(new java.awt.Color(0, 0, 204));
        btn_refresh.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_refresh.setForeground(new java.awt.Color(255, 255, 255));
        btn_refresh.setText("REF");
        btn_refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_refreshActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 882, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(btn_ViewEmp)
                .addGap(50, 50, 50)
                .addComponent(btn_AddEmp)
                .addGap(50, 50, 50)
                .addComponent(btn_DelEmp)
                .addGap(50, 50, 50)
                .addComponent(btn_refresh)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_ViewEmp)
                    .addComponent(btn_AddEmp)
                    .addComponent(btn_DelEmp)
                    .addComponent(btn_refresh))
                .addContainerGap())
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel2.setText("MotorPH");

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        btn_Profile.setBackground(new java.awt.Color(0, 0, 204));
        btn_Profile.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_Profile.setForeground(new java.awt.Color(255, 255, 255));
        btn_Profile.setText("Employees Profile");
        btn_Profile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_ProfileActionPerformed(evt);
            }
        });

        btn_LeaveMgt.setBackground(new java.awt.Color(0, 0, 204));
        btn_LeaveMgt.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_LeaveMgt.setForeground(new java.awt.Color(255, 255, 255));
        btn_LeaveMgt.setText("Employees Attendance & Leave");
        btn_LeaveMgt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_LeaveMgtActionPerformed(evt);
            }
        });

        btn_SalaryAndStatutory.setBackground(new java.awt.Color(0, 0, 204));
        btn_SalaryAndStatutory.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_SalaryAndStatutory.setForeground(new java.awt.Color(255, 255, 255));
        btn_SalaryAndStatutory.setText("Employees Salary & Statutory");
        btn_SalaryAndStatutory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SalaryAndStatutoryActionPerformed(evt);
            }
        });

        btn_PayrollSummary.setBackground(new java.awt.Color(0, 0, 204));
        btn_PayrollSummary.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_PayrollSummary.setForeground(new java.awt.Color(255, 255, 255));
        btn_PayrollSummary.setText("Payroll Processing");
        btn_PayrollSummary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_PayrollSummaryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        btn_MyRecords.setBackground(new java.awt.Color(102, 102, 102));
        btn_MyRecords.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_MyRecords.setForeground(new java.awt.Color(255, 255, 255));
        btn_MyRecords.setText("My Records");
        btn_MyRecords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_MyRecordsActionPerformed(evt);
            }
        });

        btn_EmpRecords.setBackground(new java.awt.Color(102, 102, 102));
        btn_EmpRecords.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_EmpRecords.setForeground(new java.awt.Color(255, 255, 255));
        btn_EmpRecords.setText("Employee Records");
        btn_EmpRecords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_EmpRecordsActionPerformed(evt);
            }
        });

        btn_Logout.setBackground(new java.awt.Color(102, 102, 102));
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

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Version 1.30");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(jLabel1))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btn_MyRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(jLabel3))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn_EmpRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(jLabel4))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(btn_Logout, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btn_Profile, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(55, 55, 55)
                        .addComponent(btn_LeaveMgt, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(btn_SalaryAndStatutory, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addComponent(btn_PayrollSummary, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_Profile)
                    .addComponent(btn_LeaveMgt)
                    .addComponent(btn_SalaryAndStatutory)
                    .addComponent(btn_PayrollSummary))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(btn_MyRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(btn_EmpRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(5, 5, 5)
                        .addComponent(btn_Logout, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jPanel1.getAccessibleContext().setAccessibleName("Employee Records");

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

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_PayrollSummaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_PayrollSummaryActionPerformed
        new frm_EmployeesPayrollProcess().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_PayrollSummaryActionPerformed

    private void btn_SalaryAndStatutoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SalaryAndStatutoryActionPerformed
        new frm_EmployeesSalary().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_SalaryAndStatutoryActionPerformed

    private void btn_LeaveMgtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LeaveMgtActionPerformed
        new frm_EmployeesAttLeave().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_LeaveMgtActionPerformed

    private void btn_ProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ProfileActionPerformed
        // Already on Employee Records page (which handles employee profiles)
    }//GEN-LAST:event_btn_ProfileActionPerformed

    private void btn_MyRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_MyRecordsActionPerformed
        new frm_EmpProfile().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_MyRecordsActionPerformed

    private void btn_LogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_LogoutActionPerformed
        CSVHandler.handleLogout(this);
    }//GEN-LAST:event_btn_LogoutActionPerformed

    private void btn_EmpRecordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_EmpRecordsActionPerformed
        // Already on Employee Records page
    }//GEN-LAST:event_btn_EmpRecordsActionPerformed

    private void btn_ViewEmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_ViewEmpActionPerformed
        int selectedRow = tbl_Employees.getSelectedRow();
        if (selectedRow >= 0) {
            // Get the employee data from the selected row
            String empID = (String) tbl_Employees.getValueAt(selectedRow, 0);
            String firstName = (String) tbl_Employees.getValueAt(selectedRow, 1);
            String lastName = (String) tbl_Employees.getValueAt(selectedRow, 2);
            String birthdate = (String) tbl_Employees.getValueAt(selectedRow, 3);
            String address = (String) tbl_Employees.getValueAt(selectedRow, 4);
            String phoneNumber = (String) tbl_Employees.getValueAt(selectedRow, 5);
            String employeeStatus = (String) tbl_Employees.getValueAt(selectedRow, 6);
            String position = (String) tbl_Employees.getValueAt(selectedRow, 7);
            String immediateSupervisor = (String) tbl_Employees.getValueAt(selectedRow, 8);
            
            // Create EmpDetails object
            EmpDetails empDetails = new EmpDetails(empID, firstName, lastName, birthdate, 
                                                 address, phoneNumber, employeeStatus, 
                                                 position, immediateSupervisor);
            
            // Open in view mode directly
            frm_EmployeesRecords2 viewForm = new frm_EmployeesRecords2(empDetails, false);
            viewForm.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select an employee to view.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btn_ViewEmpActionPerformed

    private void btn_AddEmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_AddEmpActionPerformed
        // Open add employee dialog
        frm_EmployeesRecords2 addForm = new frm_EmployeesRecords2(null, false);
        addForm.setVisible(true);
        
        // Refresh data after dialog is closed (in case data was added)
        displayEmployeeData();
    }//GEN-LAST:event_btn_AddEmpActionPerformed

    private void btn_DelEmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_DelEmpActionPerformed
        int selectedRow = tbl_Employees.getSelectedRow();
        if (selectedRow >= 0) {
            // Get the employee ID from the selected row
            String empID = (String) tbl_Employees.getValueAt(selectedRow, 0);
            String firstName = (String) tbl_Employees.getValueAt(selectedRow, 1);
            String lastName = (String) tbl_Employees.getValueAt(selectedRow, 2);
            
            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete employee: " + firstName + " " + lastName + " (ID: " + empID + ")?", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // Delete the employee
                    CSVHandler.deleteEmployee(empID);
                    
                    // Refresh the table
                    displayEmployeeData();
                    
                    JOptionPane.showMessageDialog(this, 
                        "Employee " + firstName + " " + lastName + " has been deleted successfully.", 
                        "Delete Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                        "Error deleting employee: " + e.getMessage(), 
                        "Delete Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.", 
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_btn_DelEmpActionPerformed

    private void btn_refreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_refreshActionPerformed
        displayEmployeeData();
    }//GEN-LAST:event_btn_refreshActionPerformed

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
            java.util.logging.Logger.getLogger(frm_EmployeesRecords.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frm_EmployeesRecords.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frm_EmployeesRecords.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frm_EmployeesRecords.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frm_EmployeesRecords().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_AddEmp;
    private javax.swing.JButton btn_DelEmp;
    private javax.swing.JButton btn_EmpRecords;
    private javax.swing.JButton btn_LeaveMgt;
    private javax.swing.JButton btn_Logout;
    private javax.swing.JButton btn_MyRecords;
    private javax.swing.JButton btn_PayrollSummary;
    private javax.swing.JButton btn_Profile;
    private javax.swing.JButton btn_SalaryAndStatutory;
    private javax.swing.JButton btn_ViewEmp;
    private javax.swing.JButton btn_refresh;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbl_Employees;
    // End of variables declaration//GEN-END:variables
}
