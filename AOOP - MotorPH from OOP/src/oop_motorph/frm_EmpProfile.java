package oop_motorph;

import javax.swing.JOptionPane;
import java.awt.*;
import java.util.List;


public class frm_EmpProfile extends javax.swing.JFrame {

    
    public frm_EmpProfile() {
        AuthorizationService.requireAuthenticated();
        initComponents();
        AppTheme.finish(this);
        
        // Set window properties - larger size to show all content
        setResizable(true);
        setTitle("Profile");
        setSize(1400, 950); // Increased size to accommodate all content
        setMinimumSize(new java.awt.Dimension(1150, 800));
        setLocationRelativeTo(null); // Center the window after setting size
        
        // Initialize combo boxes
        cmb_empStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Regular", "Probationary"}));
        cbox_position.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"HR Manager", "Account Team Leader", "Payroll Manager", "HR Rank and File", "Payroll Rank and File", "Account Rank and File"}));
        
        // Show only this employee's supervisor in the read-only profile.
        loadSupervisors();

        // Apply modern styling after components are initialized
        applyModernStyling();
        addHoverEffects();

        EmpDetails employee = EmpUserSession.getInstance().getCurrentUser();
        String role = EmpUserSession.getInstance().getRole();
        
    if (employee != null && role != null) {
        // Load employee data and set role-based access
        loadEmployeeData(employee);
        setRoleBasedAccess(role);
        if ("HR".equalsIgnoreCase(role)) SecurityActivityPanel.attach(this);
    } else {
        JOptionPane.showMessageDialog(this, "No employee data or role found!", "Error", JOptionPane.ERROR_MESSAGE);
    }
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
                javax.swing.BorderFactory.createEmptyBorder(16, 20, 16, 20) // Reduced from 20,24,24,24
            )
        ));
        
        // Style the navigation area with minimal border - reduced padding since title is removed
        jPanel3.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        
        // Style text fields with enhanced modern borders and improved spacing
        styleTextFieldEnhanced(txt_empID);
        styleTextFieldEnhanced(txt_firstName);
        styleTextFieldEnhanced(txt_lastName);
        styleTextFieldEnhanced(txt_birthdate);
        styleTextFieldEnhanced(txt_address);
        styleTextFieldEnhanced(txt_phoneNo);
        
        // Style combo boxes with enhanced styling
        styleComboBoxEnhanced(cmb_empStatus);
        styleComboBoxEnhanced(cbox_position);
        styleComboBoxEnhanced(cmb_Supervisor);
        
        // Style labels with enhanced modern typography
        styleLabelEnhanced(jLabel7, false);
        styleLabelEnhanced(jLabel8, false);
        styleLabelEnhanced(jLabel9, false);
        styleLabelEnhanced(jLabel6, false);
        styleLabelEnhanced(jLabel10, false);
        styleLabelEnhanced(jLabel11, false);
        styleLabelEnhanced(jLabel12, false);
        styleLabelEnhanced(jLabel13, false);
        styleLabelEnhanced(cbox_immSupervisor, false);
        
        // Style main title with enhanced typography and spacing
        // Title removed per user request
        
        // Style version label if it exists
        // Version label removed per user request
        
        // Add enhanced styling to profile picture area with rounded appearance
        if (jLabel15 != null) {
            jLabel15.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4),
                    javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 12), 1)
                ),
                javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new Color(229, 231, 235), 2),
                    javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12)
                )
            ));
        }
        
        // Style navigation buttons with enhanced modern design and better spacing
        styleNavigationButtonEnhanced(btn_Profile, new Color(34, 197, 94), true); // Active green
        styleNavigationButtonEnhanced(btn_LeaveMgt, new Color(107, 114, 128), false);
        styleNavigationButtonEnhanced(btn_SalaryAndStatutory, new Color(107, 114, 128), false);
        styleNavigationButtonEnhanced(btn_PayrollSummary, new Color(107, 114, 128), false);
        
        // Style left sidebar buttons with different sizing
        styleSidebarButtonEnhanced(btn_MyRecords, new Color(107, 114, 128), false);
        styleSidebarButtonEnhanced(btn_EmpRecords, new Color(107, 114, 128), false);
        styleSidebarButtonEnhanced(btn_Logout, new Color(239, 68, 68), false); // Red for logout
        
        // Add generous spacing improvements to the overall layout - comfortable padding for better UX
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
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
                javax.swing.BorderFactory.createEmptyBorder(10, 16, 10, 16) // Reduced from 14,18,14,18
            )
        ));
        field.setForeground(new Color(55, 65, 81));
    }
    
    private void styleComboBoxEnhanced(javax.swing.JComboBox<String> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBackground(new Color(249, 250, 251));
        comboBox.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createEmptyBorder(1, 1, 2, 2),
                javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 6), 1)
            ),
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
                javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14) // Reduced from 12,16,12,16
            )
        ));
        comboBox.setForeground(new Color(55, 65, 81));
    }
    
    private void styleLabelEnhanced(javax.swing.JLabel label, boolean isTitle) {
        if (isTitle) {
            label.setFont(new Font("Segoe UI", Font.BOLD, 22));
            label.setForeground(new Color(17, 24, 39));
        } else {
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(new Color(75, 85, 99));
            label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 0)); // Reduced from 8 to 4
        }
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
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(backgroundColor);
        button.setForeground(new Color(255, 255, 255));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        
        // Force exact uniform size for ALL sidebar buttons
        button.setPreferredSize(new java.awt.Dimension(170, 45));
        button.setMinimumSize(new java.awt.Dimension(170, 45));
        button.setMaximumSize(new java.awt.Dimension(170, 45));
        button.setSize(170, 45);
        
        // Perfect alignment and spacing for sidebar buttons
        button.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        button.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        button.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        
        // Clean, uniform styling for all sidebar buttons
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
        addButtonHoverEnhanced(btn_Profile, new Color(34, 197, 94), new Color(22, 163, 74));
        addButtonHoverEnhanced(btn_LeaveMgt, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_SalaryAndStatutory, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_PayrollSummary, new Color(107, 114, 128), new Color(75, 85, 99));
        
        // Add hover effects for sidebar buttons
        addButtonHoverEnhanced(btn_MyRecords, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_EmpRecords, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_Logout, new Color(239, 68, 68), new Color(220, 38, 38));
    }
    
    private void addButtonHoverEnhanced(javax.swing.JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                    // Different hover effects for navigation vs sidebar buttons
                    if (button == btn_Profile) {
                        // Active Profile button hover
                        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            javax.swing.BorderFactory.createCompoundBorder(
                                javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 4),
                                javax.swing.BorderFactory.createLineBorder(new Color(34, 197, 94, 60), 1)
                            ),
                            javax.swing.BorderFactory.createCompoundBorder(
                                javax.swing.BorderFactory.createLineBorder(new Color(34, 197, 94), 2),
                                javax.swing.BorderFactory.createEmptyBorder(10, 16, 10, 16)
                            )
                        ));
                    } else if (button == btn_LeaveMgt || button == btn_SalaryAndStatutory || button == btn_PayrollSummary) {
                        // Navigation buttons hover
                        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            javax.swing.BorderFactory.createEmptyBorder(0, 0, 4, 4),
                            javax.swing.BorderFactory.createCompoundBorder(
                                javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 15), 1),
                                javax.swing.BorderFactory.createEmptyBorder(12, 18, 12, 18)
                            )
                        ));
                    } else {
                        // Sidebar buttons hover - uniform effect
                        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            javax.swing.BorderFactory.createCompoundBorder(
                                javax.swing.BorderFactory.createEmptyBorder(1, 1, 5, 5),
                                javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 12), 1)
                            ),
                            javax.swing.BorderFactory.createCompoundBorder(
                                javax.swing.BorderFactory.createLineBorder(new Color(156, 163, 175), 1),
                                javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12)
                            )
                        ));
                    }
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(normalColor);
                    // Reset to normal border
                    if (button == btn_Profile) {
                        // Active Profile button normal
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
                    } else if (button == btn_LeaveMgt || button == btn_SalaryAndStatutory || button == btn_PayrollSummary) {
                        // Navigation buttons normal
                        button.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                            javax.swing.BorderFactory.createEmptyBorder(1, 1, 3, 3),
                            javax.swing.BorderFactory.createCompoundBorder(
                                javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 8), 1),
                                javax.swing.BorderFactory.createEmptyBorder(12, 18, 12, 18)
                            )
                        ));
                    } else {
                        // Sidebar buttons normal - restore uniform styling
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
                }
            }
        });
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

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oop_motorph/img/img_profilepic.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(txt_empID, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addComponent(jLabel15))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(103, 103, 103)
                        .addComponent(jLabel7)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 104, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmb_Supervisor, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addComponent(cbox_immSupervisor)
                    .addComponent(cbox_position, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmb_empStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addComponent(txt_phoneNo, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addComponent(txt_address, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_birthdate, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_firstName, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addComponent(txt_lastName, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txt_firstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(jLabel9)
                        .addGap(3, 3, 3)
                        .addComponent(txt_lastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_birthdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(8, 8, 8)
                        .addComponent(jLabel10))
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_phoneNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_empID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmb_empStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(jLabel13)
                .addGap(6, 6, 6)
                .addComponent(cbox_position, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(cbox_immSupervisor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmb_Supervisor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        jLabel2.setFont(new java.awt.Font("Arial Rounded MT Bold", 1, 32)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(17, 24, 39));
        jLabel2.setText("MotorPH");

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(229, 231, 235)),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 4, Short.MAX_VALUE)
        );

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
