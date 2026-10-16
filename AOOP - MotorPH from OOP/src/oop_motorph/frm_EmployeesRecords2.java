

package oop_motorph;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import oop_motorph.EmpDetails;
import oop_motorph.CSVHandler;
import oop_motorph.DatabaseHandler;

public class frm_EmployeesRecords2 extends javax.swing.JFrame {
    private EmpDetails empDetails;
    private boolean isEditMode = false;
    private EmpDetails newEmployeeData;
    private String existingEmpID;
    // Map to store supervisor display name and EmployeeID
    private Map<String, String> supervisorMap;
    
    public frm_EmployeesRecords2(EmpDetails empDetails, boolean isEditMode) {
        // Autopopulate supervisor list from employee table, store EmployeeID and name
        supervisorMap = new LinkedHashMap<>();
        supervisorMap.put("Select", null);
        supervisorMap.put("N/A", null);
        java.util.List<EmpDetails> employees = CSVHandler.getEmployeeData();
        for (EmpDetails emp : employees) {
            String name = emp.getFirstName() + " " + emp.getLastName();
            supervisorMap.put(name + " (" + emp.getEmpID() + ")", emp.getEmpID());
        }

        initComponents();
        AppTheme.finish(this);
        this.empDetails = empDetails;
        this.isEditMode = isEditMode;
        // Set supervisor dropdown model
        cbox_immSupervisor.setModel(new javax.swing.DefaultComboBoxModel<>(supervisorMap.keySet().toArray(new String[0])));
        populateFields();
        setFieldsEditable(empDetails == null || isEditMode);

        setLocationRelativeTo(null);
        setResizable(false);

        // Set appropriate title based on mode
        if (empDetails == null) {
            setTitle("Add Employee");
        } else if (isEditMode) {
            setTitle("Edit Employee");
        } else {
            setTitle("View Employee");
        }
    }

    private void populateFields() {
        if (empDetails != null) {
            existingEmpID = empDetails.getEmpID(); // Store the original ID before editing
            txt_empID.setText(empDetails.getEmpID());
            txt_firstName.setText(empDetails.getFirstName());
            txt_lastName.setText(empDetails.getLastName());
            txt_birthdate.setText(empDetails.getBirthdate());
            txt_address.setText(empDetails.getAddress());
            txt_phoneNo.setText(empDetails.getPhoneNumber());
            cbox_empStatus.setSelectedItem(empDetails.getEmployeeStatus());
            cbox_position.setSelectedItem(empDetails.getPosition());
            // Find supervisor display name by EmployeeID
            String supervisorIdOrName = empDetails.getImmediateSupervisor();
            String supervisorDisplay = "Select";
            if (supervisorIdOrName != null && !supervisorIdOrName.isEmpty()) {
                for (Map.Entry<String, String> entry : supervisorMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    // Skip 'Select' and 'N/A' entries
                    if (key.equals("Select") || key.equals("N/A")) continue;
                    // Match by EmployeeID
                    if (supervisorIdOrName.equals(String.valueOf(value))) {
                        supervisorDisplay = key;
                        break;
                    }
                    // Match by supervisor name (for legacy CSV)
                    int idxParen = key.indexOf(" (");
                    if (idxParen > 0) {
                        String nameOnly = key.substring(0, idxParen);
                        if (supervisorIdOrName.equals(nameOnly)) {
                            supervisorDisplay = key;
                            break;
                        }
                    }
                }
                // If not found, fallback to N/A
                if (supervisorDisplay.equals("Select")) {
                    supervisorDisplay = "N/A";
                }
            } else {
                supervisorDisplay = "N/A";
            }
            cbox_immSupervisor.setSelectedItem(supervisorDisplay);
        } else {
            // Clear fields for Add mode
            existingEmpID = null;
            // Auto-generate the next employee ID
            int nextId = DatabaseHandler.getNextEmployeeId();
            txt_empID.setText(String.valueOf(nextId));
            txt_firstName.setText("");
            txt_lastName.setText("");
            txt_birthdate.setText("");
            txt_address.setText("");
            txt_phoneNo.setText("");
            cbox_empStatus.setSelectedIndex(0);
            cbox_position.setSelectedIndex(0);
            cbox_immSupervisor.setSelectedIndex(0);
        }
}
    public EmpDetails getNewEmployeeData() {
        return newEmployeeData;
    }
    
    private void setFieldsEditable(boolean editable) {
        // Employee ID is never editable (primary key - auto-generated in add mode, immutable in edit mode)
        txt_empID.setEditable(false);
        txt_firstName.setEditable(editable);
        txt_lastName.setEditable(editable);
        txt_birthdate.setEditable(editable);
        txt_address.setEditable(editable);
        txt_phoneNo.setEditable(editable);
        cbox_empStatus.setEnabled(editable);
        cbox_position.setEnabled(editable);
        cbox_immSupervisor.setEnabled(editable);
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
        jLabel14 = new javax.swing.JLabel();
        cbox_empStatus = new javax.swing.JComboBox<>();
        cbox_position = new javax.swing.JComboBox<>();
        cbox_immSupervisor = new javax.swing.JComboBox<>();
        jLabel15 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        btn_EditEmp = new javax.swing.JButton();
        btn_SaveEmp = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED, null, java.awt.Color.lightGray, null, null));

        txt_empID.setEditable(false);

        txt_firstName.setEditable(false);

        txt_lastName.setEditable(false);

        txt_birthdate.setEditable(false);

        txt_address.setEditable(false);

        txt_phoneNo.setEditable(false);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Employee ID:");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("First Name:");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setText("Last Name:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("Birthdate  (YYYY-MM-DD):");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("Address:");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setText("Phone Number:");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setText("Status:");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel13.setText("Position:");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel14.setText("Immediate Supervisor:");

        cbox_empStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select", "Regular", "Probation" }));
        cbox_empStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbox_empStatusActionPerformed(evt);
            }
        });

        cbox_position.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select", "HR Manager", "Account Team Leader", "HR Team Leader", "Payroll Manager", "HR Rank and File", "Payroll Rank and File", "Account Rank and File", "Payroll Team Leader", "Account Manager", "Chief Executive Officer", "Chief Operating Officer", "Chief Finance Officer", "Chief Marketing Officer", "IT Operations and Systems", "Accounting Head", "Sales & Marketing", "Supply Chain and Logistics", "Customer Service and Relations" }));

        // Supervisor dropdown is set in constructor, not here
        // ...existing code...

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oop_motorph/img/img_profilepic.png"))); // NOI18N

        jLabel17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oop_motorph/img/img_editemp.png"))); // NOI18N
        jLabel17.setText("jLabel17");

        jLabel19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/oop_motorph/img/img_Sav.png"))); // NOI18N

        btn_EditEmp.setFont(new java.awt.Font("Segoe UI", 0, 7)); // NOI18N
        btn_EditEmp.setText("EDIT");
        btn_EditEmp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_EditEmpActionPerformed(evt);
            }
        });

        btn_SaveEmp.setFont(new java.awt.Font("Segoe UI", 0, 6)); // NOI18N
        btn_SaveEmp.setText("SAVE");
        btn_SaveEmp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SaveEmpActionPerformed(evt);
            }
        });

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
                        .addComponent(jLabel7))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btn_EditEmp, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(36, 36, 36)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel19)
                            .addComponent(btn_SaveEmp, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 104, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbox_immSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(cbox_position, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbox_empStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_phoneNo, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_address, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_birthdate, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_firstName, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_lastName, javax.swing.GroupLayout.PREFERRED_SIZE, 500, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(42, 42, 42))
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
                        .addGap(12, 12, 12)
                        .addComponent(jLabel9)
                        .addGap(3, 3, 3)
                        .addComponent(txt_lastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(15, 15, 15)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_birthdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(13, 13, 13)
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
                .addComponent(cbox_empStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel13)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(cbox_position, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(1, 1, 1)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbox_immSupervisor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(17, 17, 17))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btn_EditEmp)
                            .addComponent(btn_SaveEmp))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

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
            .addGap(0, 810, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 102));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 40, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 902, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel1.getAccessibleContext().setAccessibleName("Profile");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_SaveEmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SaveEmpActionPerformed
    
    String empID = txt_empID.getText().trim();
    String firstName = CSVHandler.capitalizeWords(txt_firstName.getText().trim(), 50);
    String lastName = CSVHandler.capitalizeWords(txt_lastName.getText().trim(), 50);

    // Check if first name or last name contains a comma
    if (firstName.contains(",") || lastName.contains(",")) {
        JOptionPane.showMessageDialog(this, "First name and last name cannot contain commas.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Get birthdate and validate YYYY-MM-DD format
    String birthdate = txt_birthdate.getText().trim();

    // Validate YYYY-MM-DD format
    if (!birthdate.matches("\\d{4}-\\d{2}-\\d{2}")) {
        JOptionPane.showMessageDialog(this, "Invalid birthdate format! Use YYYY-MM-DD (e.g., 2002-03-10).", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    System.out.println("Formatted Birthdate: " + birthdate);
    String address = CSVHandler.validateAddress(txt_address.getText().trim());

    String phoneNo = txt_phoneNo.getText().trim();
    //Validate Phone Number
    if (!CSVHandler.isValidPhone(phoneNo)) {
        JOptionPane.showMessageDialog(this, "Phone number must contain only numbers and hyphens, and be up to 11 characters long.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        return;
    }

    String empStatus = (String) cbox_empStatus.getSelectedItem();
    String position = (String) cbox_position.getSelectedItem();
    String supervisorDisplay = (String) cbox_immSupervisor.getSelectedItem();
    String supervisorEmpID = supervisorMap.get(supervisorDisplay); // This is the EmployeeID, not the name

    // Check for empty fields
    if (empID.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || birthdate.isEmpty() || 
        address.isEmpty() || phoneNo.isEmpty() || empStatus == null || "Select".equals(empStatus) || position == null || "Select".equals(position)
        || supervisorDisplay == null || "Select".equals(supervisorDisplay)) {
        JOptionPane.showMessageDialog(this, "All fields must be filled out!", "Missing Information", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Validate Employee ID: Only numeric and max 10 characters
    if (!empID.matches("\\d{1,10}")) {  
        JOptionPane.showMessageDialog(this, "Employee ID must be numeric and up to 10 digits only.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Validate First Name & Last Name: Max 50 characters
    if (firstName.length() > 50) {
        JOptionPane.showMessageDialog(this, "First name cannot exceed 50 characters.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        return;
    }

    if (lastName.length() > 50) {
        JOptionPane.showMessageDialog(this, "Last name cannot exceed 50 characters.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Additional validation is done above for YYYY-MM-DD format

    // Use supervisorEmpID for database (pass EmployeeID, not name)
    EmpDetails updatedEmp = new EmpDetails(empID, firstName, lastName, birthdate, address, phoneNo, empStatus, position, supervisorEmpID);

    // Assign correct existing employee ID
    String existingEmpID = (isEditMode && empDetails != null) ? empDetails.getEmpID() : null;

    if (isEditMode) {
        // Fix duplicate check
        if (CSVHandler.employeeExists(empID, existingEmpID)) {
            JOptionPane.showMessageDialog(this, "Employee ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Ensure supervisor is saved as EmployeeID
        CSVHandler.updateEmployee(updatedEmp);
    } else {
        if (CSVHandler.employeeExists(empID, null)) {
            JOptionPane.showMessageDialog(this, "Employee ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CSVHandler.addEmployee(updatedEmp);
    }

    JOptionPane.showMessageDialog(this, "Employee record successfully saved!");
    dispose();

    }//GEN-LAST:event_btn_SaveEmpActionPerformed

    private void cbox_empStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbox_empStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbox_empStatusActionPerformed

    private void btn_EditEmpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_EditEmpActionPerformed
        isEditMode = true;
        setFieldsEditable(true);
        setTitle("Edit Employee");
    }//GEN-LAST:event_btn_EditEmpActionPerformed
 
    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_EditEmp;
    private javax.swing.JButton btn_SaveEmp;
    private javax.swing.JComboBox<String> cbox_empStatus;
    private javax.swing.JComboBox<String> cbox_immSupervisor;
    private javax.swing.JComboBox<String> cbox_position;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel19;
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
