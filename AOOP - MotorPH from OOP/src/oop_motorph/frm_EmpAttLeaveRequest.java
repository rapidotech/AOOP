
package oop_motorph;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.swing.JOptionPane;


public class frm_EmpAttLeaveRequest extends javax.swing.JFrame {
    private EmpAttLeave empAttLeave;
    private boolean isEditMode;

    public frm_EmpAttLeaveRequest() {
        this(null, false);
    }

    public frm_EmpAttLeaveRequest(EmpAttLeave empAttLeave, boolean isEditMode) {
        AuthorizationService.requireAuthenticated();
        if (empAttLeave != null) AuthorizationService.requireEmployeeOrHr(empAttLeave.getEmpID());
        initComponents();
        AppTheme.finish(this);
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("Leave / Attendance Request");
        this.empAttLeave = empAttLeave;
        this.isEditMode = isEditMode;
        populateFields();

        composeWorkspace();
    }

    /** Source-owned responsive composition; legacy fixed layout is retired. */
    private void composeWorkspace() {
        WorkspaceUI.primary(btn_SubmitLeaveAtt, "Submit request");
        WorkspaceUI.secondary(btn_EditLeaveAtt, "Edit request");
        javax.swing.JComponent content = WorkspaceUI.column(
            WorkspaceUI.section("Employee", "The employee associated with this request", WorkspaceUI.fields(
                "Employee ID", txt_empID, "First name", txt_firstName, "Last name", txt_lastName,
                "Employment status", txt_status, "Position", txt_position, "Immediate supervisor", cbox_immSupervisor)),
            WorkspaceUI.section("Request details", "Dates, time and request classification", WorkspaceUI.fields(
                "From", dc_dateFrom, "To", dc_dateTo, "Time in (HH:mm)", txt_timeIn, "Time out (HH:mm)", txt_timeOut,
                "Hours worked", txt_hoursWorked, "Duration", txt_duration,
                "Attendance type", cbox_attendanceType, "Request status", cbox_attendanceStat)),
            WorkspaceUI.section("Leave record", "Allocation, use and remaining balance", WorkspaceUI.fields(
                "Vacation leave allocation", txt_vlCount, "Sick leave allocation", txt_slCount,
                "Vacation leave used", txt_vlUsed, "Sick leave used", txt_slUsed,
                "Vacation leave balance", txt_vlBal, "Sick leave balance", txt_slBal)));
        WorkspaceShell.mount(this, "Leave / attendance request", "Review the details before submitting", "attendance", WorkspaceUI.scroll(content),
            WorkspaceUI.actions(btn_EditLeaveAtt, btn_SubmitLeaveAtt));
    }

    private void populateFields() {
    if (empAttLeave != null) {
        // Define formatters for date and time
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Declare LocalDate variables
        LocalDate attDateFrom = null;
        LocalDate attDateTo = null;

        // Check if getAttDateFrom() and getAttDateTo() return String or LocalDate
        Object attDateFromObj = empAttLeave.getAttDateFrom();
        Object attDateToObj = empAttLeave.getAttDateTo();

        if (attDateFromObj instanceof String) {
            attDateFrom = LocalDate.parse((String) attDateFromObj, dateFormatter);
        } else if (attDateFromObj instanceof LocalDate) {
            attDateFrom = (LocalDate) attDateFromObj;
        }

        if (attDateToObj instanceof String) {
            attDateTo = LocalDate.parse((String) attDateToObj, dateFormatter);
        } else if (attDateToObj instanceof LocalDate) {
            attDateTo = (LocalDate) attDateToObj;
        }

        // Set text fields
        txt_empID.setText(empAttLeave.getEmpID());
        txt_firstName.setText(empAttLeave.getFirstName());
        txt_lastName.setText(empAttLeave.getLastName());
        txt_status.setText(empAttLeave.getEmployeeStatus());
        txt_position.setText(empAttLeave.getPosition());

        // Set JComboBox values
        cbox_immSupervisor.setSelectedItem(empAttLeave.getImmediateSupervisor());
        cbox_attendanceType.setSelectedItem(empAttLeave.getAttendanceType());
        cbox_attendanceStat.setSelectedItem(empAttLeave.getAttendanceStatus());

        // Convert LocalDate to Date for JDateChooser
        if (attDateFrom != null) {
            dc_dateFrom.setDate(java.sql.Date.valueOf(attDateFrom));
        } else {
            dc_dateFrom.setDate(null);
        }

        if (attDateTo != null) {
            dc_dateTo.setDate(java.sql.Date.valueOf(attDateTo));
        } else {
            dc_dateTo.setDate(null);
        }

        // Set time fields safely (checking for null)
        if (empAttLeave.getTimeIn() != null) {
            txt_timeIn.setText(empAttLeave.getTimeIn().format(timeFormatter));
        } else {
            txt_timeIn.setText("");
        }

        if (empAttLeave.getTimeOut() != null) {
            txt_timeOut.setText(empAttLeave.getTimeOut().format(timeFormatter));
        } else {
            txt_timeOut.setText("");
        }

        // Set numeric fields
        txt_hoursWorked.setText(String.valueOf(empAttLeave.getHoursWorked()));
        txt_duration.setText(String.valueOf(empAttLeave.getDuration()));
        txt_vlCount.setText(String.valueOf(empAttLeave.getVlCount()));
        txt_vlUsed.setText(String.valueOf(empAttLeave.getVlUsed()));
        txt_vlBal.setText(String.valueOf(empAttLeave.getVlBalance()));
        txt_slCount.setText(String.valueOf(empAttLeave.getSlCount()));
        txt_slUsed.setText(String.valueOf(empAttLeave.getSlUsed()));
        txt_slBal.setText(String.valueOf(empAttLeave.getSlBalance()));

        // Disable editing if not in edit mode
        setFieldsEditable(isEditMode);
    }
}


    private void setFieldsEditable(boolean editable) {
    // These fields remain non-editable
    txt_empID.setEditable(false);
    txt_firstName.setEditable(false);
    txt_lastName.setEditable(false);
    txt_status.setEditable(false);
    txt_position.setEditable(false);
    txt_vlCount.setEditable(false);
    txt_hoursWorked.setEditable(false);
    txt_duration.setEditable(false);
    cbox_attendanceStat.setEditable(false);

    // Editable when in edit mode
    cbox_immSupervisor.setEnabled(editable);
    cbox_attendanceType.setEnabled(editable);
    // cbox_attendanceStat.setEnabled(editable);
    dc_dateFrom.setEnabled(editable);
    dc_dateTo.setEnabled(editable);
    txt_timeIn.setEditable(editable);
    txt_timeOut.setEditable(editable);
    txt_slCount.setEditable(editable);
    txt_slUsed.setEditable(editable);
    txt_slBal.setEditable(editable);
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
        txt_status = new javax.swing.JTextField();
        txt_position = new javax.swing.JTextField();
        txt_timeIn = new javax.swing.JTextField();
        txt_timeOut = new javax.swing.JTextField();
        txt_hoursWorked = new javax.swing.JTextField();
        txt_duration = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        cbox_attendanceType = new javax.swing.JComboBox<>();
        cbox_attendanceStat = new javax.swing.JComboBox<>();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        cbox_immSupervisor = new javax.swing.JComboBox<>();
        txt_vlCount = new javax.swing.JTextField();
        txt_slCount = new javax.swing.JTextField();
        txt_vlUsed = new javax.swing.JTextField();
        txt_slUsed = new javax.swing.JTextField();
        txt_vlBal = new javax.swing.JTextField();
        txt_slBal = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        dc_dateFrom = new com.toedter.calendar.JDateChooser();
        dc_dateTo = new com.toedter.calendar.JDateChooser();
        jPanel3 = new javax.swing.JPanel();
        btn_EditLeaveAtt = new javax.swing.JButton();
        btn_SubmitLeaveAtt = new javax.swing.JButton();
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

        txt_status.setEditable(false);

        txt_position.setEditable(false);
        txt_position.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_positionActionPerformed(evt);
            }
        });

        txt_timeIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_timeInActionPerformed(evt);
            }
        });

        txt_timeOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_timeOutActionPerformed(evt);
            }
        });

        txt_hoursWorked.setEditable(false);

        txt_duration.setEditable(false);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("Employee ID:");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("First Name:");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Last Name:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Status:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Position:");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("Supervisor:");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Date From:");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel8.setText("Date To:");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setText("Time In:");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("Time Out:");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setText("Hrs Worked:");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel12.setText("Duration:");

        cbox_attendanceType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select", "Work", "VL Request", "SL Request", "Overtime", "Unpaid Leave" }));

        cbox_attendanceStat.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pending" }));
        cbox_attendanceStat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbox_attendanceStatActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel13.setText("Type:");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel14.setText("Status:");

        cbox_immSupervisor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select", "N/A", "Romualdez Fredrick", "Villanueva Andrea Mae", "Alvaro Roderick", "San Jose Brad", "Salcedo Anthony", "Mata Christian", "Lim Antonio", "De Leon Selena" }));
        cbox_immSupervisor.setEnabled(false);

        txt_vlCount.setEditable(false);
        txt_vlCount.setBackground(new java.awt.Color(255, 255, 204));

        txt_slCount.setEditable(false);
        txt_slCount.setBackground(new java.awt.Color(255, 255, 204));

        txt_vlUsed.setEditable(false);
        txt_vlUsed.setBackground(new java.awt.Color(255, 255, 204));

        txt_slUsed.setEditable(false);
        txt_slUsed.setBackground(new java.awt.Color(255, 255, 204));

        txt_vlBal.setEditable(false);
        txt_vlBal.setBackground(new java.awt.Color(255, 255, 204));

        txt_slBal.setEditable(false);
        txt_slBal.setBackground(new java.awt.Color(255, 255, 204));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel15.setText("VL Count:");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel16.setText("SL Count:");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel17.setText("VL Used:");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel18.setText("SL Used:");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel19.setText("VL Balance:");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel20.setText("SL Balance:");


        jPanel3.setBackground(new java.awt.Color(255, 255, 102));


        btn_EditLeaveAtt.setBackground(new java.awt.Color(51, 51, 255));
        btn_EditLeaveAtt.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_EditLeaveAtt.setForeground(new java.awt.Color(255, 255, 255));
        btn_EditLeaveAtt.setText("Edit");
        btn_EditLeaveAtt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_EditLeaveAttActionPerformed(evt);
            }
        });

        btn_SubmitLeaveAtt.setBackground(new java.awt.Color(51, 51, 255));
        btn_SubmitLeaveAtt.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_SubmitLeaveAtt.setForeground(new java.awt.Color(255, 255, 255));
        btn_SubmitLeaveAtt.setText("Submit");
        btn_SubmitLeaveAtt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_SubmitLeaveAttActionPerformed(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(245, 28, 71));


        jPanel5.setBackground(new java.awt.Color(255, 255, 102));


        jPanel6.setBackground(new java.awt.Color(255, 255, 102));


        jPanel1.getAccessibleContext().setAccessibleName("Profile");

    }// </editor-fold>

    private void btn_SubmitLeaveAttActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_SubmitLeaveAttActionPerformed

       if (empAttLeave != null) {
        try {
            // Retrieve old date values before editing
            String oldDateFrom = empAttLeave.getAttDateFrom() != null
                ? empAttLeave.getAttDateFrom().format(DateTimeFormatter.ofPattern("dd-MMM-yy"))
                : "";
            String oldDateTo = empAttLeave.getAttDateTo() != null
                ? empAttLeave.getAttDateTo().format(DateTimeFormatter.ofPattern("dd-MMM-yy"))
                : "";

      // Get dates
            LocalDate dateFrom = dc_dateFrom.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate dateTo = dc_dateTo.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Validate date range
            if (dateFrom.isAfter(dateTo)) {
                JOptionPane.showMessageDialog(this, "'Date From' cannot be later than 'Date To'.", "Invalid Date Selection", JOptionPane.ERROR_MESSAGE);
                return; // Stop execution
            }

            // Fix for the Duration and Hours Worked Calculation
            // Compute duration

            // Set default Time In and Time Out for SL/VL requests
            String attendanceType = cbox_attendanceType.getSelectedItem().toString();
            if (attendanceType.equals("SL Request") || attendanceType.equals("VL Request")) {
                txt_timeIn.setText("08:00");
                txt_timeOut.setText("17:00");
            }
            // Get and set time values
            String timeInStr = txt_timeIn.getText().trim();
            String timeOutStr = txt_timeOut.getText().trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            if (!timeInStr.isEmpty() && !timeOutStr.isEmpty()) {
                LocalTime timeIn = LocalTime.parse(timeInStr, formatter);
                LocalTime timeOut = LocalTime.parse(timeOutStr, formatter);
                empAttLeave.setTimeIn(timeIn);
                empAttLeave.setTimeOut(timeOut);
            } else {
                System.err.println("Error: Time In/Out values are missing!");
            }

            // Compute duration
            long duration = ChronoUnit.DAYS.between(dateFrom, dateTo) + 1;
            txt_duration.setText(String.valueOf(duration));
            empAttLeave.setDuration(duration);

            // Compute hours worked
            double hoursWorked = 0.0;
            long minutesWorked = Duration.between(empAttLeave.getTimeIn(), empAttLeave.getTimeOut()).toMinutes();

            // Special handling for Overtime & Unpaid Leave
            if (attendanceType.equals("Overtime") /*|| attendanceType.equals("Unpaid Leave") */) {
                hoursWorked = (minutesWorked / 60.0) * duration;
            } else {
                hoursWorked = ((minutesWorked / 60.0) - 1) * duration;
            }

            // Round to 2 decimal places
            hoursWorked = Math.round(hoursWorked * 100.0) / 100.0;
            txt_hoursWorked.setText(String.valueOf(hoursWorked));
            empAttLeave.setHoursWorked(hoursWorked);

             // Validate Attendance Type selection
            if (attendanceType.equalsIgnoreCase("Select")) {
                JOptionPane.showMessageDialog(this, "Please select a valid Attendance Type.", "Invalid Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }   // Fix for the Duration and Hours Worked Calculation

            // Update details based on input fields
            empAttLeave.setImmediateSupervisor(
                cbox_immSupervisor.getSelectedItem() != null
                ? cbox_immSupervisor.getSelectedItem().toString().trim()
                : ""
            );

            empAttLeave.setAttendanceType(
                cbox_attendanceType.getSelectedItem() != null
                ? cbox_attendanceType.getSelectedItem().toString().trim()
                : ""
            );

            // Automatically set Attendance Status to "Pending"
            empAttLeave.setAttendanceStatus("Pending");

            // Handle Date Pickers
            if (dc_dateFrom.getDate() != null) {
                empAttLeave.setAttDateFrom(
                    dc_dateFrom.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                );
            }

            if (dc_dateTo.getDate() != null) {
                empAttLeave.setAttDateTo(
                    dc_dateTo.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                );
            }

            // Handle Time Inputs
            if (!txt_timeIn.getText().isEmpty()) {
                empAttLeave.setTimeIn(
                    LocalTime.parse(txt_timeIn.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"))
                );
            }

            if (!txt_timeOut.getText().isEmpty()) {
                empAttLeave.setTimeOut(
                    LocalTime.parse(txt_timeOut.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"))
                );
            }

            // Save edits by passing old date values for row identification
            CSVHandler.saveEditAttendanceRequest(empAttLeave, oldDateFrom, oldDateTo);

            JOptionPane.showMessageDialog(this, "Attendance request submitted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error submitting attendance request: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
       }
    }//GEN-LAST:event_btn_SubmitLeaveAttActionPerformed

    private void btn_EditLeaveAttActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_EditLeaveAttActionPerformed

        cbox_immSupervisor.setEnabled(false);
        setFieldsEditable(true); // Enable editing


    }//GEN-LAST:event_btn_EditLeaveAttActionPerformed

    private void txt_positionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_positionActionPerformed

    }//GEN-LAST:event_txt_positionActionPerformed

    private void txt_timeInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_timeInActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_timeInActionPerformed

    private void cbox_attendanceStatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbox_attendanceStatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbox_attendanceStatActionPerformed

    private void txt_timeOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_timeOutActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_timeOutActionPerformed

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
            java.util.logging.Logger.getLogger(frm_EmpAttLeaveRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frm_EmpAttLeaveRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frm_EmpAttLeaveRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frm_EmpAttLeaveRequest.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>


        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frm_EmpAttLeaveRequest().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_EditLeaveAtt;
    private javax.swing.JButton btn_SubmitLeaveAtt;
    private javax.swing.JComboBox<String> cbox_attendanceStat;
    private javax.swing.JComboBox<String> cbox_attendanceType;
    private javax.swing.JComboBox<String> cbox_immSupervisor;
    private com.toedter.calendar.JDateChooser dc_dateFrom;
    private com.toedter.calendar.JDateChooser dc_dateTo;
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
    private javax.swing.JLabel jLabel20;
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
    private javax.swing.JTextField txt_duration;
    private javax.swing.JTextField txt_empID;
    private javax.swing.JTextField txt_firstName;
    private javax.swing.JTextField txt_hoursWorked;
    private javax.swing.JTextField txt_lastName;
    private javax.swing.JTextField txt_position;
    private javax.swing.JTextField txt_slBal;
    private javax.swing.JTextField txt_slCount;
    private javax.swing.JTextField txt_slUsed;
    private javax.swing.JTextField txt_status;
    private javax.swing.JTextField txt_timeIn;
    private javax.swing.JTextField txt_timeOut;
    private javax.swing.JTextField txt_vlBal;
    private javax.swing.JTextField txt_vlCount;
    private javax.swing.JTextField txt_vlUsed;
    // End of variables declaration//GEN-END:variables
}
