package oop_motorph;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.util.*;
import java.util.List;

public class frm_EmpPayroll extends javax.swing.JFrame {

    public frm_EmpPayroll() {
        AuthorizationService.requireAuthenticated();
        initComponents();
        AppTheme.finish(this);
        
        
        // Disable table editing
        tbl_Payroll.setDefaultEditor(Object.class, null);

        EmpDetails employee = EmpUserSession.getInstance().getCurrentUser();
        String role = EmpUserSession.getInstance().getRole();
        
        if (employee != null && role != null) {
            // Load payroll data and set role-based access
            loadPayrollData(employee);
            setRoleBasedAccess(role);
        } else {
            JOptionPane.showMessageDialog(this, "No employee data or role found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    
        composeWorkspace();
    }

    private void setOptimalColumnWidths() {
        // Set much larger column widths to completely prevent truncation
        if (tbl_Payroll.getColumnModel().getColumnCount() >= 20) {
            // Date columns - extra wide for full date display
            tbl_Payroll.getColumnModel().getColumn(0).setPreferredWidth(200); // Period Start
            tbl_Payroll.getColumnModel().getColumn(1).setPreferredWidth(200); // Period End
            
            // ID columns - wider
            tbl_Payroll.getColumnModel().getColumn(2).setPreferredWidth(150); // Payroll ID
            tbl_Payroll.getColumnModel().getColumn(3).setPreferredWidth(150); // Employee ID
            
            // Hours columns - wider for numbers and "hrs" text
            tbl_Payroll.getColumnModel().getColumn(4).setPreferredWidth(160); // Regular Hours
            tbl_Payroll.getColumnModel().getColumn(5).setPreferredWidth(160); // Overtime Hours
            tbl_Payroll.getColumnModel().getColumn(6).setPreferredWidth(160); // Total Hours
            
            // Currency columns - very wide for monetary values with ₱ symbol
            tbl_Payroll.getColumnModel().getColumn(7).setPreferredWidth(180); // Salary
            tbl_Payroll.getColumnModel().getColumn(8).setPreferredWidth(160); // Rice
            tbl_Payroll.getColumnModel().getColumn(9).setPreferredWidth(160); // Phone
            tbl_Payroll.getColumnModel().getColumn(10).setPreferredWidth(160); // Clothing
            tbl_Payroll.getColumnModel().getColumn(11).setPreferredWidth(190); // Total Allowances
            tbl_Payroll.getColumnModel().getColumn(12).setPreferredWidth(180); // Gross
            tbl_Payroll.getColumnModel().getColumn(13).setPreferredWidth(190); // Taxable Income
            
            // Deduction columns - wider for currency with ₱ symbol
            tbl_Payroll.getColumnModel().getColumn(14).setPreferredWidth(160); // SSS
            tbl_Payroll.getColumnModel().getColumn(15).setPreferredWidth(160); // PhilHealth
            tbl_Payroll.getColumnModel().getColumn(16).setPreferredWidth(160); // PagIBIG
            tbl_Payroll.getColumnModel().getColumn(17).setPreferredWidth(160); // Tax
            tbl_Payroll.getColumnModel().getColumn(18).setPreferredWidth(180); // Deductions
            
            // Net Pay - widest for final amount (most important)
            tbl_Payroll.getColumnModel().getColumn(19).setPreferredWidth(200); // Net Pay
            
            // Set larger minimum widths to prevent truncation
            for (int i = 0; i < tbl_Payroll.getColumnModel().getColumnCount(); i++) {
                tbl_Payroll.getColumnModel().getColumn(i).setMinWidth(140); // Much larger minimum
                tbl_Payroll.getColumnModel().getColumn(i).setMaxWidth(500); // Larger maximum
            }
            
            // Allow all columns to be resizable for user customization
            for (int i = 0; i < tbl_Payroll.getColumnModel().getColumnCount(); i++) {
                tbl_Payroll.getColumnModel().getColumn(i).setResizable(true);
            }
        }
        
        // Force table to recalculate layout
        tbl_Payroll.revalidate();
        tbl_Payroll.repaint();
        if (jScrollPane1 != null) {
            jScrollPane1.revalidate();
            jScrollPane1.repaint();
        }
    }

    /** Keep payroll units and monetary formatting while using the shared table palette. */
    private void configurePayrollValues() {
        tbl_Payroll.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            { putClientProperty("html.disable", Boolean.TRUE); }
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                    boolean focused, int row, int column) {
                super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                int modelColumn = table.convertColumnIndexToModel(column);
                String text = value == null ? "" : value.toString();
                setHorizontalAlignment(modelColumn >= 7 ? SwingConstants.RIGHT : modelColumn >= 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
                if (text.matches("\\d+(\\.\\d+)?")) {
                    if (modelColumn >= 7 && modelColumn <= 19) text = String.format("\u20b1%.2f", Double.parseDouble(text));
                    else if (modelColumn >= 4 && modelColumn <= 6) text += " hrs";
                }
                setText(text);
                setFont(new Font("Segoe UI", modelColumn == 19 ? Font.BOLD : Font.PLAIN, 13));
                if (!selected) { setBackground(row % 2 == 0 ? Color.WHITE : AppTheme.SURFACE); setForeground(AppTheme.INK); }
                return this;
            }
        });
    }

    private void loadPayrollData(EmpDetails employee) {
        List<String[]> payrollData = DatabaseHandler.readPayrollFromDatabase();

        String[] columns = {
                "Period Start", "Period End", "Payroll ID", "EmployeeID",
                "Regular Hours", "Overtime Hours", "Total Hours",
                "Salary", "Rice", "Phone", "Clothing", "Total Allowances",
                "Gross", "Taxable Income", "SSS", "PhilHealth", "PagIBIG",
                "Tax", "Deductions", "Net Pay"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0);

        for (String[] row : payrollData) {
            if (row.length >= 20 && row[3].trim().equals(employee.getEmpID().trim())) {
                model.addRow(Arrays.copyOf(row, 20));
            }
        }

        tbl_Payroll.setModel(model);
        
        // Apply column widths AFTER setting the model
        setOptimalColumnWidths();
    }

    public void setRoleBasedAccess(String role) {
        // Disable all buttons by default
        btn_MyRecords.setEnabled(false);
        btn_LeaveMgt.setEnabled(false);
        btn_SalaryAndStatutory.setEnabled(false);
        btn_Profile.setEnabled(false);
        btn_EmpRecords.setEnabled(false);
        btn_printPayslip.setEnabled(false); // A valid payroll period must be selected first.
        btn_Logout.setEnabled(false);

        switch (role.toUpperCase()) {
            case "HR":
                // HR gets full access
                btn_MyRecords.setEnabled(true);
                btn_LeaveMgt.setEnabled(true);
                btn_SalaryAndStatutory.setEnabled(true);
                btn_Profile.setEnabled(true);
                btn_EmpRecords.setEnabled(true);
                btn_Logout.setEnabled(true);
                break;

            case "EMPLOYEE":
                // Employees have access to personal info only
                btn_MyRecords.setEnabled(true);
                btn_LeaveMgt.setEnabled(true);
                btn_SalaryAndStatutory.setEnabled(true);
                btn_Profile.setEnabled(true);
                btn_Logout.setEnabled(true);
                break;

            default:
                JOptionPane.showMessageDialog(this, "Invalid role: " + role, "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    private String generatePayslip(int selectedRow) {
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a payroll record first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        String startDate = tbl_Payroll.getValueAt(selectedRow, 0).toString();
        String endDate = tbl_Payroll.getValueAt(selectedRow, 1).toString();
        String payrollID = tbl_Payroll.getValueAt(selectedRow, 2).toString();
        String empID = tbl_Payroll.getValueAt(selectedRow, 3).toString();

        String regularHours = tbl_Payroll.getValueAt(selectedRow, 4).toString();
        String overtimeHours = tbl_Payroll.getValueAt(selectedRow, 5).toString();
        String totalHours = tbl_Payroll.getValueAt(selectedRow, 6).toString();

        String salary = tbl_Payroll.getValueAt(selectedRow, 7).toString();
        String rice = tbl_Payroll.getValueAt(selectedRow, 8).toString();
        String phone = tbl_Payroll.getValueAt(selectedRow, 9).toString();
        String clothing = tbl_Payroll.getValueAt(selectedRow, 10).toString();
        String totalAllow = tbl_Payroll.getValueAt(selectedRow, 11).toString();

        String gross = tbl_Payroll.getValueAt(selectedRow, 12).toString();
        String taxable = tbl_Payroll.getValueAt(selectedRow, 13).toString();

        String sss = tbl_Payroll.getValueAt(selectedRow, 14).toString();
        String philhealth = tbl_Payroll.getValueAt(selectedRow, 15).toString();
        String pagibig = tbl_Payroll.getValueAt(selectedRow, 16).toString();
        String tax = tbl_Payroll.getValueAt(selectedRow, 17).toString();
        String deductions = tbl_Payroll.getValueAt(selectedRow, 18).toString();
        String netPay = tbl_Payroll.getValueAt(selectedRow, 19).toString();

        return """
    <html>
    <head>
        <style>
            body {
                font-family: Arial, sans-serif;
                background: #f3f3f3;
                padding: 20px;
            }
            table {
                border-collapse: collapse;
                width: 100%%;
                background-color: #fff;
                border: 1px solid #ccc;
            }
            th {
                background-color: #2c3e50;
                color: white;
                padding: 10px;
                text-align: left;
            }
            td {
                padding: 8px 12px;
                border-bottom: 1px solid #eee;
                vertical-align: top;
            }
            .section {
                background-color: #f9f9f9;
                font-weight: bold;
                color: #2c3e50;
            }
            .highlight {
                background-color: #eafaf1;
                color: #2e7d32;
                font-weight: bold;
            }
        </style>
    </head>
    <body>
        <h2 style='text-align:center;'>MOTORPH Payslip</h2>
        <table>
            <tr class='section'><td colspan='6'>Employee & Payroll Info</td></tr>
            <tr>
                <td><strong>Payroll ID</strong></td><td>%s</td>
                <td><strong>Employee ID</strong></td><td>%s</td>
                <td><strong>Pay Period</strong></td><td>%s to %s</td>
            </tr>
            <tr class='section'><td colspan='6'>Work Hours</td></tr>
            <tr>
                <td><strong>Regular Hours</strong></td><td>%s hrs</td>
                <td><strong>Overtime Hours</strong></td><td>%s hrs</td>
                <td><strong>Total Hours</strong></td><td>%s hrs</td>
            </tr>
            <tr class='section'><td colspan='6'>Earnings & Allowances</td></tr>
            <tr>
                <td><strong>Basic Salary</strong></td><td>₱%s</td>
                <td><strong>Rice Allowance</strong></td><td>₱%s</td>
                <td><strong>Phone Allowance</strong></td><td>₱%s</td>
            </tr>
            <tr>
                <td><strong>Clothing Allowance</strong></td><td>₱%s</td>
                <td><strong>Total Allowances</strong></td><td colspan='3'>₱%s</td>
            </tr>
            <tr class='section'><td colspan='6'>Deductions</td></tr>
            <tr>
                <td><strong>SSS</strong></td><td>₱%s</td>
                <td><strong>PhilHealth</strong></td><td>₱%s</td>
                <td><strong>Pag-IBIG</strong></td><td>₱%s</td>
            </tr>
            <tr>
                <td><strong>Tax Withheld</strong></td><td>₱%s</td>
                <td><strong>Total Deductions</strong></td><td colspan='3'>₱%s</td>
            </tr>
            <tr class='section'><td colspan='6'>Summary</td></tr>
            <tr>
                <td><strong>Gross Pay</strong></td><td>₱%s</td>
                <td><strong>Taxable Income</strong></td><td>₱%s</td>
                <td class='highlight'><strong>NET PAY</strong></td><td class='highlight'>₱%s</td>
            </tr>
        </table>
        <p style='text-align:center; font-size:11px; color:#777; margin-top:15px;'>
            This is a system-generated payslip. No signature required.
        </p>
    </body>
    </html>
    """.formatted(
                payrollID, empID, startDate, endDate,
                regularHours, overtimeHours, totalHours,
                salary, rice, phone, clothing, totalAllow,
                sss, philhealth, pagibig, tax, deductions,
                gross, taxable, netPay
        );
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Control construction - generated layout retired">
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_Payroll = new javax.swing.JTable();
        btn_printPayslip = new javax.swing.JButton();
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

        // Payroll table setup
        tbl_Payroll.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Period Start", "Period End", "Payroll ID", "EmployeeID", "Regular Hours", "Overtime Hours", "Total Hours", "Salary", "Rice", "Phone", "Clothing", "Total Allowances", "Gross", "Taxable Income", "SSS", "PhilHealth", "PagIBIG", "Tax", "Deductions", "Net Pay"
            }
        ));
        jScrollPane1.setViewportView(tbl_Payroll);

        btn_printPayslip.setBackground(new java.awt.Color(34, 197, 94));
        btn_printPayslip.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_printPayslip.setForeground(new java.awt.Color(255, 255, 255));
        btn_printPayslip.setText("VIEW/PRINT PAYSLIP");
        btn_printPayslip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_printPayslipActionPerformed(evt);
            }
        });


        jPanel3.setBackground(new java.awt.Color(255, 255, 255));


        btn_Profile.setBackground(new java.awt.Color(107, 114, 128));
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

        btn_PayrollSummary.setBackground(new java.awt.Color(34, 197, 94));
        btn_PayrollSummary.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_PayrollSummary.setForeground(new java.awt.Color(255, 255, 255));
        btn_PayrollSummary.setText("Payroll Summary");
        btn_PayrollSummary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_PayrollSummaryActionPerformed(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(248, 250, 252));


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

        jPanel5.setBackground(new java.awt.Color(248, 250, 252));


        jPanel6.setBackground(new java.awt.Color(248, 250, 252));


        jPanel1.getAccessibleContext().setAccessibleName("Payroll Summary");

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

    private void btn_printPayslipActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = tbl_Payroll.getSelectedRow();
        String payslipHTML = generatePayslip(selectedRow);

        if (payslipHTML != null) {
            try {
                // Render HTML into a JEditorPane
                JEditorPane editorPane = new JEditorPane();
                editorPane.setContentType("text/html");
                editorPane.setText(payslipHTML);
                editorPane.setSize(1000, editorPane.getPreferredSize().height);
                editorPane.setEditable(false);

                // Force layout & calculate correct height
                editorPane.revalidate();
                editorPane.repaint();

                // Optional preview panel
                JScrollPane preview = new JScrollPane(editorPane);
                String[] options = {"Print", "Exit"};
                int option = JOptionPane.showOptionDialog(null, preview, "Payslip Preview",
                        JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                if (option == 0) { // Print option selected
                    // Force layout again just before image capture
                    editorPane.setSize(1000, editorPane.getPreferredSize().height);

                    // Create BufferedImage of appropriate height
                    int width = editorPane.getWidth();
                    int height = editorPane.getPreferredSize().height;

                    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = image.createGraphics();
                    editorPane.printAll(g2d);
                    g2d.dispose();

                    // Set up printer job to print the image
                    PrinterJob job = PrinterJob.getPrinterJob();
                    job.setPrintable((graphics, pageFormat, pageIndex) -> {
                        if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

                        double x = pageFormat.getImageableX();
                        double y = pageFormat.getImageableY();

                        // Scale image if it's wider than printable area
                        double scaleX = pageFormat.getImageableWidth() / image.getWidth();
                        double scaleY = pageFormat.getImageableHeight() / image.getHeight();
                        double scale = Math.min(scaleX, scaleY);

                        Graphics2D g = (Graphics2D) graphics;
                        g.translate(x, y);
                        g.scale(scale, scale);
                        g.drawImage(image, 0, 0, null);

                        return Printable.PAGE_EXISTS;
                    });

                    if (job.printDialog()) {
                        job.print();
                        JOptionPane.showMessageDialog(this, "Printing successful!", "Print", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Print Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
//GEN-LAST:event_btn_printPayslipActionPerformed

    /** Source-owned responsive composition; legacy fixed layout is retired. */
    private void composeWorkspace() {
        WorkspaceShell.bindNavigation(this, "profile", btn_Profile);
        WorkspaceShell.bindNavigation(this, "salary", btn_SalaryAndStatutory);
        WorkspaceShell.bindNavigation(this, "attendance", btn_LeaveMgt);
        WorkspaceShell.bindNavigation(this, "payroll", btn_PayrollSummary);
        WorkspaceShell.bindNavigation(this, "employees", btn_EmpRecords);
        WorkspaceShell.bindNavigation(this, "logout", btn_Logout);
        tbl_Payroll.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        configurePayrollValues();
        tbl_Payroll.putClientProperty("motorph.emptyMessage", "No payroll periods available. Contact HR if you expect a payslip.");
        WorkspaceUI.primary(btn_printPayslip, "Print payslip");
        tbl_Payroll.getSelectionModel().addListSelectionListener(event -> updatePrintAction());
        tbl_Payroll.getModel().addTableModelListener(event -> updatePrintAction());
        updatePrintAction();
        WorkspaceShell.mount(this, "Payroll summary", "Review a payroll period and print its payslip", "payroll",
            WorkspaceUI.table(tbl_Payroll), WorkspaceUI.actions(btn_printPayslip));
    }

    private void updatePrintAction() {
        int row = tbl_Payroll.getSelectedRow();
        String role = EmpUserSession.getInstance().getRole();
        boolean authorized = "EMPLOYEE".equalsIgnoreCase(role) || "HR".equalsIgnoreCase(role);
        boolean selected = row >= 0 && row < tbl_Payroll.getRowCount() && tbl_Payroll.getColumnCount() >= 20;
        btn_printPayslip.setEnabled(authorized && selected);
        btn_printPayslip.setToolTipText(selected ? "Print the selected payroll period" : "Select a payroll period to print its payslip");
    }

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
            java.util.logging.Logger.getLogger(frm_EmpPayroll.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(frm_EmpPayroll.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(frm_EmpPayroll.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(frm_EmpPayroll.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
    

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new frm_EmpPayroll().setVisible(true);
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
    private javax.swing.JButton btn_printPayslip;
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
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbl_Payroll;
    // End of variables declaration//GEN-END:variables
}
