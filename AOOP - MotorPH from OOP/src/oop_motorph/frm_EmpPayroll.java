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
        
        // Set window properties - larger size to show all content
        setResizable(true);
        setTitle("Payroll Summary");
        setSize(1400, 950); // Increased size to accommodate all content
        setMinimumSize(new java.awt.Dimension(1150, 800));
        setLocationRelativeTo(null); // Center the window after setting size
        
        // Disable table editing
        tbl_Payroll.setDefaultEditor(Object.class, null);

        // Apply modern styling after components are initialized
        applyModernStyling();
        addHoverEffects();

        EmpDetails employee = EmpUserSession.getInstance().getCurrentUser();
        String role = EmpUserSession.getInstance().getRole();
        
        if (employee != null && role != null) {
            // Load payroll data and set role-based access
            loadPayrollData(employee);
            setRoleBasedAccess(role);
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
                javax.swing.BorderFactory.createEmptyBorder(16, 20, 16, 20)
            )
        ));
        
        // Style the navigation area with minimal border - reduced padding since title is removed
        jPanel3.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
            javax.swing.BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        
        // Style the payroll table
        stylePayrollTable();
        
        // Style buttons
        styleButtonEnhanced(btn_printPayslip, new Color(34, 197, 94)); // Green for print
        
        // Style navigation buttons with enhanced modern design and better spacing
        styleNavigationButtonEnhanced(btn_Profile, new Color(107, 114, 128), false);
        styleNavigationButtonEnhanced(btn_LeaveMgt, new Color(107, 114, 128), false);
        styleNavigationButtonEnhanced(btn_SalaryAndStatutory, new Color(107, 114, 128), false);
        styleNavigationButtonEnhanced(btn_PayrollSummary, new Color(34, 197, 94), true); // Active green
        
        // Style left sidebar buttons with different sizing
        styleSidebarButtonEnhanced(btn_MyRecords, new Color(107, 114, 128), false);
        styleSidebarButtonEnhanced(btn_EmpRecords, new Color(107, 114, 128), false);
        styleSidebarButtonEnhanced(btn_Logout, new Color(239, 68, 68), false); // Red for logout
        
        // Add generous spacing improvements to the overall layout - comfortable padding for better UX
        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
    }
    
    private void stylePayrollTable() {
        // Enable horizontal scrolling for better data visibility
        tbl_Payroll.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        
        // Set optimal column widths for different data types
        if (tbl_Payroll.getColumnModel().getColumnCount() > 0) {
            // Period columns
            tbl_Payroll.getColumnModel().getColumn(0).setPreferredWidth(100); // Period Start
            tbl_Payroll.getColumnModel().getColumn(1).setPreferredWidth(100); // Period End
            tbl_Payroll.getColumnModel().getColumn(2).setPreferredWidth(90);  // Payroll ID
            tbl_Payroll.getColumnModel().getColumn(3).setPreferredWidth(90);  // Employee ID
            
            // Hours columns
            tbl_Payroll.getColumnModel().getColumn(4).setPreferredWidth(110); // Regular Hours
            tbl_Payroll.getColumnModel().getColumn(5).setPreferredWidth(110); // Overtime Hours
            tbl_Payroll.getColumnModel().getColumn(6).setPreferredWidth(100); // Total Hours
            
            // Financial columns - wider for better number display
            tbl_Payroll.getColumnModel().getColumn(7).setPreferredWidth(120);  // Salary
            tbl_Payroll.getColumnModel().getColumn(8).setPreferredWidth(80);   // Rice
            tbl_Payroll.getColumnModel().getColumn(9).setPreferredWidth(80);   // Phone
            tbl_Payroll.getColumnModel().getColumn(10).setPreferredWidth(90);  // Clothing
            tbl_Payroll.getColumnModel().getColumn(11).setPreferredWidth(130); // Total Allowances
            tbl_Payroll.getColumnModel().getColumn(12).setPreferredWidth(120); // Gross
            tbl_Payroll.getColumnModel().getColumn(13).setPreferredWidth(130); // Taxable Income
            tbl_Payroll.getColumnModel().getColumn(14).setPreferredWidth(80);  // SSS
            tbl_Payroll.getColumnModel().getColumn(15).setPreferredWidth(90);  // PhilHealth
            tbl_Payroll.getColumnModel().getColumn(16).setPreferredWidth(80);  // PagIBIG
            tbl_Payroll.getColumnModel().getColumn(17).setPreferredWidth(80);  // Tax
            tbl_Payroll.getColumnModel().getColumn(18).setPreferredWidth(120); // Deductions
            tbl_Payroll.getColumnModel().getColumn(19).setPreferredWidth(120); // Net Pay
        }
        
        // Professional table styling with enhanced visual hierarchy
        tbl_Payroll.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl_Payroll.setRowHeight(42); // Increased for better readability
        tbl_Payroll.setShowGrid(true);
        tbl_Payroll.setGridColor(new Color(240, 242, 247)); // Softer grid lines
        tbl_Payroll.setBackground(Color.WHITE);
        tbl_Payroll.setForeground(new Color(55, 65, 81)); // Professional dark gray text
        
        // Enhanced selection styling
        tbl_Payroll.setSelectionBackground(new Color(59, 130, 246)); // Professional blue
        tbl_Payroll.setSelectionForeground(Color.WHITE);
        tbl_Payroll.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        // Remove focus border for cleaner look
        tbl_Payroll.setFocusable(true);
        tbl_Payroll.setShowHorizontalLines(true);
        tbl_Payroll.setShowVerticalLines(true);
        
        // Professional table header styling
        if (tbl_Payroll.getTableHeader() != null) {
            tbl_Payroll.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            tbl_Payroll.getTableHeader().setBackground(new Color(248, 250, 252)); // Light blue-gray
            tbl_Payroll.getTableHeader().setForeground(new Color(30, 41, 59)); // Dark professional text
            tbl_Payroll.getTableHeader().setPreferredSize(new java.awt.Dimension(0, 45)); // Taller header
            tbl_Payroll.getTableHeader().setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(203, 213, 225)),
                javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            tbl_Payroll.getTableHeader().setReorderingAllowed(false); // Prevent column reordering
        }
        
        // Enhanced scroll pane styling with horizontal scrolling enabled
        if (jScrollPane1 != null) {
            // Enable horizontal scrolling
            jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            
            jScrollPane1.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createEmptyBorder(2, 2, 4, 4),
                    javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0, 8), 1)
                ),
                javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                    javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0)
                )
            ));
            jScrollPane1.getViewport().setBackground(Color.WHITE);
            jScrollPane1.setBackground(Color.WHITE);
            
            // Style the scrollbars for a modern look
            jScrollPane1.getVerticalScrollBar().setBackground(new Color(248, 250, 252));
            jScrollPane1.getHorizontalScrollBar().setBackground(new Color(248, 250, 252));
            
            // Ensure smooth scrolling
            jScrollPane1.getVerticalScrollBar().setUnitIncrement(16);
            jScrollPane1.getHorizontalScrollBar().setUnitIncrement(16);
        }
        
        // Set optimal column widths to prevent data truncation
        setOptimalColumnWidths();
        
        // Add alternating row colors for better readability
        addTableRowStyling();
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
    
    private void addTableRowStyling() {
        // Custom cell renderer for alternating row colors and better formatting
        tbl_Payroll.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected, 
                boolean hasFocus, int row, int column) {
                
                java.awt.Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    // Alternating row colors for better readability
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(249, 250, 251)); // Very light gray
                    }
                    c.setForeground(new Color(55, 65, 81)); // Professional dark gray
                } else {
                    c.setBackground(new Color(59, 130, 246)); // Professional blue selection
                    c.setForeground(Color.WHITE);
                }
                
                // Add padding for better presentation
                setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 8, 12));
                
                // Format display based on column type
                if (value != null) {
                    String displayValue = value.toString();
                    
                    // Currency columns (7-19) - right align and format
                    if (column >= 7 && column <= 19) {
                        setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                        setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        
                        // Add peso sign if it's a monetary value without one
                        if (displayValue.matches("\\d+(\\.\\d+)?") && !displayValue.contains("₱")) {
                            try {
                                double amount = Double.parseDouble(displayValue);
                                displayValue = String.format("₱%.2f", amount);
                            } catch (NumberFormatException e) {
                                // Keep original value if parsing fails
                            }
                        }
                    } 
                    // Hours columns (4-6) - center align
                    else if (column >= 4 && column <= 6) {
                        setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                        setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        if (displayValue.matches("\\d+(\\.\\d+)?")) {
                            displayValue += " hrs";
                        }
                    }
                    // Date columns (0-1) - left align
                    else if (column <= 1) {
                        setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                        setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    }
                    // ID columns (2-3) - center align
                    else if (column == 2 || column == 3) {
                        setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                    }
                    else {
                        setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                        setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    }
                    
                    // Net Pay column - make it bold and highlighted
                    if (column == 19) {
                        setFont(new Font("Segoe UI", Font.BOLD, 14));
                        if (!isSelected) {
                            c.setForeground(new Color(22, 163, 74)); // Green for net pay
                        }
                    }
                    
                    // Set the formatted text
                    setText(displayValue);
                } else {
                    setText("");
                    setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                }
                
                return c;
            }
        });
        
        // Enable auto-resize mode for better column management
        tbl_Payroll.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
    }
    
    private void styleButtonEnhanced(javax.swing.JButton button, Color backgroundColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        addButtonHoverEnhanced(btn_Profile, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_LeaveMgt, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_SalaryAndStatutory, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_PayrollSummary, new Color(34, 197, 94), new Color(22, 163, 74));
        
        // Add hover effects for sidebar buttons
        addButtonHoverEnhanced(btn_MyRecords, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_EmpRecords, new Color(107, 114, 128), new Color(75, 85, 99));
        addButtonHoverEnhanced(btn_Logout, new Color(239, 68, 68), new Color(220, 38, 38));
        
        // Add hover effect to print button
        addPrintButtonHover(btn_printPayslip, new Color(34, 197, 94), new Color(22, 163, 74));
    }
    
    private void addButtonHoverEnhanced(javax.swing.JButton button, Color normalColor, Color hoverColor) {
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(hoverColor);
                    // Different hover effects for navigation vs sidebar buttons
                    if (button == btn_PayrollSummary) {
                        // Active PayrollSummary button hover
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
                    } else if (button == btn_Profile || button == btn_LeaveMgt || button == btn_SalaryAndStatutory) {
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
                    if (button == btn_PayrollSummary) {
                        // Active PayrollSummary button normal
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
                    } else if (button == btn_Profile || button == btn_LeaveMgt || button == btn_SalaryAndStatutory) {
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
    
    private void addPrintButtonHover(javax.swing.JButton button, Color normalColor, Color hoverColor) {
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
        btn_printPayslip.setEnabled(true); // Still enabled by default
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_printPayslip, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_printPayslip)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

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

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 17, Short.MAX_VALUE)
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

        jPanel5.setBackground(new java.awt.Color(248, 250, 252));

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

        jPanel6.setBackground(new java.awt.Color(248, 250, 252));

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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn_MyRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(jLabel1))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn_EmpRecords, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(jLabel3))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn_Logout, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(jLabel4))))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addGap(0, 0, Short.MAX_VALUE))))
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

        jPanel1.getAccessibleContext().setAccessibleName("Payroll Summary");

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
