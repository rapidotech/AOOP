
package oop_motorph;

import javax.swing.JOptionPane;
import javax.swing.JTextField;


public class frm_EmployeesSalary2 extends javax.swing.JFrame implements WorkspaceScreen {

    private EmpSalaryDetails empSalaryDetails;
    private boolean isEditMode; // To determine if we are in edit mode or add mode
    private String originalEmpID; // To store the original empID for editing

    // Constructor for Edit Mode (with employee details)
    /** Code-led layout. The archived NetBeans .form layout is retired. */
    @Override public void composeWorkspace() {
        javax.swing.JButton back=new javax.swing.JButton("Back to compensation");back.addActionListener(e->WorkspaceShell.open(this,"compensation"));
        javax.swing.JComponent content=WorkspaceUI.scroll(WorkspaceUI.column(
            WorkspaceUI.section("Employee","",WorkspaceUI.fields("Employee ID",txt_empID,"First name",txt_firstName,"Last name",txt_lastName)),
            WorkspaceUI.section("Salary & allowances","Enter amounts in Philippine pesos.",WorkspaceUI.fields("Basic salary",txt_basicSalary,"Rice subsidy",txt_riceAllow,"Phone allowance",txt_phoneAllow,"Clothing allowance",txt_clothingAllow)),
            WorkspaceUI.section("Statutory identifiers","",WorkspaceUI.fields("SSS number",txt_sssNo,"PhilHealth number",txt_philhealthNo,"Pag-IBIG number",txt_pagibigNo,"TIN",txt_tinNo)),
            WorkspaceUI.section("Calculated rates","Calculated from the saved basic salary.",WorkspaceUI.fields("Gross semi-monthly",txt_grossSemi,"Hourly rate",txt_hourlyRate))));
        WorkspaceShell.mount(this,isEditMode?"Compensation details":"Add compensation","Maintain salary, allowances and statutory identifiers.","compensation",content,
            WorkspaceUI.actions(WorkspaceUI.primary(btn_SavePayInfo,"Save compensation"),WorkspaceUI.secondary(btn_EditPayInfo,"Edit compensation"),WorkspaceUI.secondary(back,"Back to compensation")));
    }

    public frm_EmployeesSalary2(String empID, String firstName, String lastName, String sssNo, String philhealthNo,
                                String pagibigNo, String tinNo, String basicSalary, String riceSubsidy,
                                String phoneAllowance, String clothingAllowance, String grossSemi, String hourlyRate) {
        AuthorizationService.requireHr();
        initComponents();
        AppTheme.finish(this);
        setLocationRelativeTo(null); // Center window
        setResizable(false);         // Disable resizing
        setTitle("Profile");

        // Initialize fields with the provided data (Edit Mode)
        setFieldsForEdit(empID, firstName, lastName, sssNo, philhealthNo, pagibigNo, tinNo,
                         basicSalary, riceSubsidy, phoneAllowance, clothingAllowance, grossSemi, hourlyRate);

        isEditMode = true;  // This is in edit mode
        originalEmpID = empID; // Store the original EmpID for editing
    }

    // Constructor for Add Mode (with empty fields)
    public frm_EmployeesSalary2() {
        AuthorizationService.requireHr();
        initComponents();
        AppTheme.finish(this);
        setLocationRelativeTo(null); // Center window
        setResizable(false);         // Disable resizing
        setTitle("Profile");

        // Initialize fields for adding new data (Add Mode)
        setFieldsForAdd();

        isEditMode = false;  // This is in add mode
        originalEmpID = null; // No original EmpID in add mode
    }

    // Helper method to set fields for editing
    private void setFieldsForEdit(String empID, String firstName, String lastName, String sssNo, String philhealthNo,
                                  String pagibigNo, String tinNo, String basicSalary, String riceSubsidy,
                                  String phoneAllowance, String clothingAllowance, String grossSemi, String hourlyRate) {
        txt_empID.setText(empID);
        txt_firstName.setText(firstName);
        txt_lastName.setText(lastName);
        txt_sssNo.setText(sssNo);
        txt_philhealthNo.setText(philhealthNo);
        txt_pagibigNo.setText(pagibigNo);
        txt_tinNo.setText(tinNo);
        txt_basicSalary.setText(basicSalary);
        txt_riceAllow.setText(riceSubsidy);
        txt_phoneAllow.setText(phoneAllowance);
        txt_clothingAllow.setText(clothingAllowance);
        txt_grossSemi.setText(grossSemi);
        txt_hourlyRate.setText(hourlyRate);
    }

    // Helper method to set fields for adding new employee (defaults)
    private void setFieldsForAdd() {
        txt_empID.setText("");
        txt_firstName.setText("");
        txt_lastName.setText("");
        txt_sssNo.setText("");
        txt_philhealthNo.setText("");
        txt_pagibigNo.setText("");
        txt_tinNo.setText("");
        txt_basicSalary.setText("");
        txt_riceAllow.setText("");
        txt_phoneAllow.setText("");
        txt_clothingAllow.setText("");
        txt_grossSemi.setText("");
        txt_hourlyRate.setText("");
    }

    // Method to get new employee salary data from the fields
    public EmpSalaryDetails getNewEmployeeSalaryData() {
        try {
            String empID = txt_empID.getText();
            String firstName = txt_firstName.getText();
            String lastName = txt_lastName.getText();
            String sssNo = txt_sssNo.getText();
            String philhealthNo = txt_philhealthNo.getText();
            String pagibigNo = txt_pagibigNo.getText();
            String tinNo = txt_tinNo.getText();

            // Convert String inputs to double
            double basicSalary = Double.parseDouble(txt_basicSalary.getText());
            double riceAllow = Double.parseDouble(txt_riceAllow.getText());
            double phoneAllow = Double.parseDouble(txt_phoneAllow.getText());
            double clothingAllow = Double.parseDouble(txt_clothingAllow.getText());
            double grossSemi = Double.parseDouble(txt_grossSemi.getText());
            double hourlyRate = Double.parseDouble(txt_hourlyRate.getText());

            // Create an EmpSalaryDetails object with the data
            EmpSalaryDetails newSalary = new EmpSalaryDetails(empID, firstName, lastName, sssNo, philhealthNo,
                pagibigNo, tinNo, basicSalary, riceAllow, phoneAllow, clothingAllow, grossSemi, hourlyRate);
            return newSalary;  // Return the newly created EmpSalaryDetails object
        } catch (NumberFormatException e) {
            // Handle invalid input (e.g., if the user enters non-numeric data)
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values for salary fields.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // Method to toggle the editability of the fields
    public void setFieldsEditable(boolean editable) {
        txt_empID.setEditable(editable);
        txt_firstName.setEditable(editable);
        txt_lastName.setEditable(editable);
        txt_sssNo.setEditable(editable);
        txt_philhealthNo.setEditable(editable);
        txt_pagibigNo.setEditable(editable);
        txt_tinNo.setEditable(editable);
        txt_basicSalary.setEditable(editable);
        txt_riceAllow.setEditable(editable);
        txt_phoneAllow.setEditable(editable);
        txt_clothingAllow.setEditable(editable);
        txt_grossSemi.setEditable(editable);
        txt_hourlyRate.setEditable(editable);
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
        jLabel17 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        btn_EditPayInfo = new javax.swing.JButton();
        btn_SavePayInfo = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txt_sssNo = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txt_philhealthNo = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txt_pagibigNo = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txt_tinNo = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txt_basicSalary = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txt_riceAllow = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        txt_phoneAllow = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txt_clothingAllow = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        txt_grossSemi = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        txt_hourlyRate = new javax.swing.JTextField();
        txt_lastName = new javax.swing.JTextField();
        txt_firstName = new javax.swing.JTextField();
        txt_empID = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.lightGray, null, null));

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oop_motorph/img/img_editemp.png"))); // NOI18N
        jLabel17.setText("jLabel17");

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oop_motorph/img/img_Sav.png"))); // NOI18N

        btn_EditPayInfo.setFont(new java.awt.Font("Segoe UI", 0, 7)); // NOI18N
        btn_EditPayInfo.setText("EDIT");
        btn_EditPayInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_EditPayInfoActionPerformed(evt);
            }
        });

        btn_SavePayInfo.setFont(new java.awt.Font("Segoe UI", 0, 6)); // NOI18N
        btn_SavePayInfo.setText("SAVE");
        btn_SavePayInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SavePayInfoActionPerformed(evt);
            }
        });

        jLabel6.setText("TIN Number:");

        txt_sssNo.setEditable(false);

        jLabel7.setText("SSS Number:");

        txt_philhealthNo.setEditable(false);

        jLabel8.setText("Philhealth Number:");

        txt_pagibigNo.setEditable(false);

        jLabel9.setText("Pagibig Number:");

        txt_tinNo.setEditable(false);

        jLabel10.setText("Basic Salary:");

        txt_basicSalary.setEditable(false);

        jLabel11.setText("Rice Subsidy Allowance:");

        txt_riceAllow.setEditable(false);

        jLabel12.setText("Phone Allowance:");

        txt_phoneAllow.setEditable(false);
        txt_phoneAllow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_phoneAllowActionPerformed(evt);
            }
        });

        jLabel13.setText("Clothing Allowance:");

        txt_clothingAllow.setEditable(false);

        jLabel14.setText("Gross Semi-Monthly Rate:");

        txt_grossSemi.setEditable(false);

        jLabel15.setText("Hourly Rate:");

        txt_hourlyRate.setEditable(false);

        txt_lastName.setEditable(false);

        txt_firstName.setEditable(false);
        txt_firstName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_firstNameActionPerformed(evt);
            }
        });

        txt_empID.setEditable(false);

        jLabel1.setText("Employee ID:");

        jLabel2.setText("First Name:");

        jLabel3.setText("Last Name:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 9)); // NOI18N
        jLabel4.setText("X X - X X X X X X X - X");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 9)); // NOI18N
        jLabel5.setText("12 numeric digits ");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 9)); // NOI18N
        jLabel16.setText("12 numeric digits ");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 9)); // NOI18N
        jLabel18.setText(" X X X - X X X - X X X - X X X");



        jPanel3.setBackground(new java.awt.Color(255, 255, 102));



        jPanel4.setBackground(new java.awt.Color(245, 28, 71));



        jPanel5.setBackground(new java.awt.Color(255, 255, 102));



        jPanel6.setBackground(new java.awt.Color(255, 255, 102));



        jPanel1.getAccessibleContext().setAccessibleName("Profile");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_SavePayInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SavePayInfoActionPerformed

    String empID = txt_empID.getText();
    String firstName = txt_firstName.getText();
    String lastName = txt_lastName.getText();
    String sssNo = txt_sssNo.getText();
    String philhealthNo = txt_philhealthNo.getText();
    String tinNo = txt_tinNo.getText();
    String pagibigNo = txt_pagibigNo.getText();

    // Regular Expressions for Validation
    String sssPattern = "\\d{2}-\\d{7}-\\d";  // Format: 45-5866331-6
    String philhealthPattern = "\\d{12}";     // 12-digit numeric
    String pagibigPattern = "\\d{12}";        // 12-digit numeric
    String tinPattern = "\\d{3}-\\d{3}-\\d{3}-\\d{3}"; // Format: 560-735-732-000
    String salaryPattern = "^\\d{1,5}(\\.\\d{1,2})?$"; // Max 8 chars (including decimal)

    // Validate SSS Number
    if (!sssNo.matches(sssPattern)) {
        JOptionPane.showMessageDialog(this, "Invalid SSS Number format! Format: XX-XXXXXXX-X",
                                      "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Validate PhilHealth Number
    if (!philhealthNo.matches(philhealthPattern)) {
        JOptionPane.showMessageDialog(this, "Invalid PhilHealth Number! Must be 12 digits.",
                                      "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Validate Pag-IBIG Number
    if (!pagibigNo.matches(pagibigPattern)) {
        JOptionPane.showMessageDialog(this, "Invalid Pag-IBIG Number! Must be 12 digits.",
                                      "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Validate TIN Number
    if (!tinNo.matches(tinPattern)) {
        JOptionPane.showMessageDialog(this, "Invalid TIN Number format! Format: XXX-XXX-XXX-XXX",
                                      "Input Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Validate Salary & Allowance Fields
    double basicSalary = 0.0, riceAllow = 0.0, phoneAllow = 0.0, clothingAllow = 0.0, grossSemi = 0.0, hourlyRate = 0.0;
    JTextField[] salaryFields = {txt_basicSalary, txt_riceAllow, txt_phoneAllow, txt_clothingAllow, txt_grossSemi, txt_hourlyRate};

    for (JTextField field : salaryFields) {
        String input = field.getText().trim();
        if (!input.matches(salaryPattern)) {
            JOptionPane.showMessageDialog(this, "Invalid input in " + field.getName() +
                                          "! Must be numeric, max 8 chars, up to 2 decimal places.",
                                          "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    try {
        basicSalary = Double.parseDouble(txt_basicSalary.getText());
        riceAllow = Double.parseDouble(txt_riceAllow.getText());
        phoneAllow = Double.parseDouble(txt_phoneAllow.getText());
        clothingAllow = Double.parseDouble(txt_clothingAllow.getText());
        grossSemi = Double.parseDouble(txt_grossSemi.getText());
        hourlyRate = Double.parseDouble(txt_hourlyRate.getText());
    } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(this, "Please enter valid numeric values for salary and allowances.",
                                      "Invalid Input", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Create new EmpSalaryDetails object
    EmpSalaryDetails updatedSalary = new EmpSalaryDetails(empID, firstName, lastName, sssNo, philhealthNo, tinNo, pagibigNo,
        basicSalary, riceAllow, phoneAllow, clothingAllow, grossSemi, hourlyRate);

    String existingEmpID = (isEditMode && empSalaryDetails != null) ? empSalaryDetails.getEmpID() : null;

    if (isEditMode) {
        if (existingEmpID != null && !existingEmpID.equals(empID) && CSVHandler.employeeExists(empID, null)) {
            JOptionPane.showMessageDialog(this, "Employee ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CSVHandler.updateEmployeeSalary(updatedSalary);
    } else {
        if (CSVHandler.employeeExists(empID, null)) {
            JOptionPane.showMessageDialog(this, "Employee ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CSVHandler.addEmployeeSalary(updatedSalary);
    }

    // Add new employee to Employees and Attendance files
    EmpDetails newEmployee = new EmpDetails(empID, firstName, lastName, "", "", "", "", "", "");
    CSVHandler.addOrUpdateEmployeeInSalary(newEmployee);

    JOptionPane.showMessageDialog(this, "Employee salary record successfully saved!");
    dispose();


    }//GEN-LAST:event_btn_SavePayInfoActionPerformed

    private void txt_phoneAllowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_phoneAllowActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_phoneAllowActionPerformed

    private void txt_firstNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_firstNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_firstNameActionPerformed

    private void btn_EditPayInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_EditPayInfoActionPerformed
    setFieldsEditable(true);
    isEditMode = true;

    // Ensure txt_empID, txt_firstName, and txt_lastName remain not editable
    txt_empID.setEditable(false);
    txt_firstName.setEditable(false);
    txt_lastName.setEditable(false);




    }//GEN-LAST:event_btn_EditPayInfoActionPerformed

    /**
     * @param args the command line arguments
     */


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_EditPayInfo;
    private javax.swing.JButton btn_SavePayInfo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
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
    private javax.swing.JTextField txt_basicSalary;
    private javax.swing.JTextField txt_clothingAllow;
    private javax.swing.JTextField txt_empID;
    private javax.swing.JTextField txt_firstName;
    private javax.swing.JTextField txt_grossSemi;
    private javax.swing.JTextField txt_hourlyRate;
    private javax.swing.JTextField txt_lastName;
    private javax.swing.JTextField txt_pagibigNo;
    private javax.swing.JTextField txt_philhealthNo;
    private javax.swing.JTextField txt_phoneAllow;
    private javax.swing.JTextField txt_riceAllow;
    private javax.swing.JTextField txt_sssNo;
    private javax.swing.JTextField txt_tinNo;
    // End of variables declaration//GEN-END:variables
}
