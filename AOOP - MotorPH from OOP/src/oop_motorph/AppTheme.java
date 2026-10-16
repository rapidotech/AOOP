package oop_motorph;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.LayerUI;
import javax.swing.text.JTextComponent;

/** Shared, native Swing presentation and recoverable persistence errors. */
public final class AppTheme {
    public static final Color INK = new Color(31, 41, 55);
    public static final Color MUTED = new Color(75, 85, 99);
    public static final Color GREEN = new Color(21, 128, 61);
    public static final Color GREEN_HOVER = new Color(22, 101, 52);
    public static final Color LINE = new Color(209, 213, 219);
    public static final Color SURFACE = new Color(249, 250, 251);
    private static boolean installed;

    private AppTheme() { }

    public static synchronized void install() {
        if (installed) return;
        installed = true;
        Font body = new Font("Segoe UI", Font.PLAIN, 13);
        for (String key : new String[]{"Button.font", "Label.font", "TextField.font",
                "PasswordField.font", "ComboBox.font", "CheckBox.font", "Table.font",
                "TableHeader.font", "OptionPane.messageFont", "OptionPane.buttonFont"}) {
            UIManager.put(key, body);
        }
        UIManager.put("nimbusFocus", GREEN);
        UIManager.put("TextField.selectionBackground", GREEN);
        UIManager.put("PasswordField.selectionBackground", GREEN);
        UIManager.put("Table.selectionBackground", new Color(220, 252, 231));
        UIManager.put("Table.selectionForeground", INK);
        UIManager.put("ComboBox.disabledForeground", INK);
        UIManager.put("ComboBox.disabledText", INK);
        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventQueue() {
            @Override protected void dispatchEvent(AWTEvent event) {
                try {
                    super.dispatchEvent(event);
                } catch (AuthorizationException ex) {
                    showAccessDenied();
                } catch (PersistenceException ex) {
                    showDataError(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow(), ex);
                } catch (UnsupportedOperationException ex) {
                    JOptionPane.showMessageDialog(null, "This action is not available in this version. No changes were saved.",
                            "Action unavailable", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
    }

    public static void showDataError(Component parent, PersistenceException ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage()
                + "\nCheck the connection, then try again.",
                "Data unavailable", JOptionPane.ERROR_MESSAGE);
    }

    private static void showAccessDenied() {
        Window owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (EmpUserSession.getInstance().getCurrentUser() == null) {
            frm_Login login = null;
            for (Window window : Window.getWindows()) {
                if (window instanceof frm_Login && window.isDisplayable()) login = (frm_Login) window;
            }
            if (login == null) { login = new frm_Login(); login.setVisible(true); }
            for (Window window : Window.getWindows()) {
                if (window != login && window.isDisplayable()) window.dispose();
            }
            owner = login;
        }
        JOptionPane.showMessageDialog(owner,
                "You do not have access to this page or action. Sign in with an authorized account.",
                "Access denied", JOptionPane.WARNING_MESSAGE);
    }

    /** Runs after each form's existing initialization and styling. */
    public static void finish(JFrame frame) {
        install();
        SwingUtilities.invokeLater(() -> {
            if (!frame.isDisplayable()) return;
            apply(frame.getContentPane());
            if (!(frame instanceof frm_Login)) makeContentReachable(frame);
        });
    }

    public static void apply(Component component) {
        if (component instanceof JComponent) {
            JComponent control = (JComponent) component;
            Font font = control.getFont();
            if (font != null && font.getSize() < 14 && !(control instanceof JLabel)) {
                control.setFont(new Font("Segoe UI", font.getStyle(), 13));
            }
        }
        if (component instanceof JButton) {
            styleButton((JButton) component);
        } else if (component instanceof JTextComponent) {
            JTextComponent input = (JTextComponent) component;
            input.setForeground(INK);
            input.setCaretColor(GREEN);
            input.setSelectionColor(GREEN);
            input.setSelectedTextColor(Color.WHITE);
            input.setDisabledTextColor(MUTED);
            input.setBackground(input.isEditable() ? Color.WHITE : SURFACE);
            installFocus(input);
        } else if (component instanceof JComboBox) {
            component.setForeground(INK);
            JComboBox<?> combo = (JComboBox<?>) component;
            combo.setRenderer(new ReadableComboRenderer(combo));
            installFocus((JComponent) component);
        } else if (component instanceof JLabel || component instanceof JCheckBox) {
            if (component instanceof JLabel && "Phone Number:".equals(((JLabel) component).getText())) {
                ((JLabel) component).setText("Phone:");
            }
            Color fg = component.getForeground();
            if (fg != null && fg.getRed() > 85 && fg.getRed() < 200
                    && Math.abs(fg.getRed() - fg.getGreen()) < 35) component.setForeground(MUTED);
        } else if (component instanceof JTable) {
            JTable table = (JTable) component;
            table.setRowHeight(30);
            table.setForeground(INK);
            table.setBackground(Color.WHITE);
            table.setSelectionBackground(new Color(220, 252, 231));
            table.setSelectionForeground(INK);
            table.setGridColor(new Color(229, 231, 235));
            table.setShowVerticalLines(false);
            table.setFillsViewportHeight(true);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            table.getTableHeader().setForeground(INK);
            table.getTableHeader().setPreferredSize(new Dimension(0, 36));
            table.getTableHeader().setToolTipText("Scroll horizontally to see all columns.");
            table.getAccessibleContext().setAccessibleDescription("Use arrow keys to select a record. Column headers identify each value.");
            if (table.getParent() instanceof JViewport) {
                JViewport viewport = (JViewport) table.getParent();
                JLayer<JComponent> layer = new JLayer<>(table, new LayerUI<JComponent>() {
                    @Override public void paint(Graphics graphics, JComponent layerComponent) {
                        super.paint(graphics, layerComponent);
                        if (table.getRowCount() == 0) {
                            graphics.setColor(MUTED);
                            graphics.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                            Object emptyMessage = table.getClientProperty("motorph.emptyMessage");
                            String message = emptyMessage == null ? "No records to display." : String.valueOf(emptyMessage);
                            int width = graphics.getFontMetrics().stringWidth(message);
                            graphics.drawString(message, Math.max(16, (viewport.getWidth() - width) / 2), 70);
                        }
                    }
                });
                viewport.setView(layer);
            }
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) apply(child);
        }
    }

    private static void styleButton(JButton button) {
        if (Boolean.TRUE.equals(button.getClientProperty("motorph.themed"))) return;
        button.putClientProperty("motorph.themed", Boolean.TRUE);
        Color old = button.getBackground();
        String text = button.getText();
        if (text != null) {
            if (text.startsWith("Employees Attendance")) button.setText("Attendance & Leave");
            else if (text.startsWith("Employees Salary")) button.setText("Salary & Statutory");
            else if ("ADD".equals(text)) button.setText("Add");
            else if ("DEL".equals(text)) button.setText("Delete");
            else if ("REF".equals(text)) button.setText("Refresh");
            button.setToolTipText(button.getText());
        }
        Color base = old;
        if (old.getGreen() > old.getRed() + 35 && old.getGreen() > old.getBlue() + 25) base = GREEN;
        else if (old.getRed() > 180 && old.getGreen() < 110) base = new Color(185, 28, 28);
        else if (old.getBlue() > old.getRed() + 50 && old.getRed() < 120) base = new Color(29, 78, 216);
        button.setBackground(base);
        button.setFocusPainted(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final Color normal = base;
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (button.isEnabled()) button.setBackground(normal.darker()); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(normal); }
        });
        installFocus(button);
    }

    private static void installFocus(JComponent control) {
        if (Boolean.TRUE.equals(control.getClientProperty("motorph.focus"))) return;
        control.putClientProperty("motorph.focus", Boolean.TRUE);
        control.addFocusListener(new FocusAdapter() {
            private Border original;
            @Override public void focusGained(FocusEvent e) {
                original = control.getBorder();
                Insets insets = original == null ? new Insets(4, 6, 4, 6) : original.getBorderInsets(control);
                control.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(GREEN, 2),
                        BorderFactory.createEmptyBorder(Math.max(0, insets.top - 2), Math.max(0, insets.left - 2),
                                Math.max(0, insets.bottom - 2), Math.max(0, insets.right - 2))));
                if (control instanceof JTextComponent) {
                    ((JTextComponent) control).scrollRectToVisible(new Rectangle(0, 0, control.getWidth(), control.getHeight()));
                }
            }
            @Override public void focusLost(FocusEvent e) { control.setBorder(original); }
        });
    }

    private static void makeContentReachable(JFrame frame) {
        Rectangle usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        Container content = frame.getContentPane();
        Dimension preferred = content.getPreferredSize();
        int contentWidth = preferred.width;
        int contentHeight = preferred.height;
        String form = frame.getClass().getSimpleName();
        String sourceMessage = form.contains("AttLeave")
                ? "Attendance and requests: database  |  Changes recorded in the audit trail"
                : form.equals("frm_EmployeesPayrollPaid") ? "Payroll approvals: database  |  Changes recorded in the audit trail"
                : form.contains("Payroll") ? "Calculated payroll and approvals: database" : null;
        if (contentWidth > 950 || contentHeight > 650 || sourceMessage != null) {
            FormCanvas canvas = new FormCanvas(content);
            if (sourceMessage != null) {
                JLabel source = new JLabel(sourceMessage);
                source.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                source.setForeground(MUTED);
                source.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
                canvas.add(source, BorderLayout.NORTH);
            }
            JScrollPane scroll = new JScrollPane(canvas);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getVerticalScrollBar().setUnitIncrement(24);
            scroll.getHorizontalScrollBar().setUnitIncrement(24);
            frame.setContentPane(scroll);
        }
        frame.setResizable(true);
        frame.setMinimumSize(new Dimension(Math.min(800, usable.width), Math.min(540, usable.height)));
        frame.setSize(Math.min(frame.getWidth(), usable.width), Math.min(frame.getHeight(), usable.height));
        frame.setLocationRelativeTo(null);
        frame.validate();
    }

    private static final class FormCanvas extends JPanel implements Scrollable {
        FormCanvas(Container content) {
            super(new BorderLayout());
            setBackground(SURFACE);
            add(content, BorderLayout.CENTER);
        }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? Math.max(24, visible.height - 24) : Math.max(24, visible.width - 24);
        }
        @Override public boolean getScrollableTracksViewportWidth() {
            return getParent() instanceof JViewport && getParent().getWidth() >= getMinimumSize().width;
        }
        @Override public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport && getParent().getHeight() >= getMinimumSize().height;
        }
    }

    /** ComboBox UI delegates can reset renderer colors after configuration. */
    private static final class ReadableComboRenderer extends DefaultListCellRenderer {
        private final JComboBox<?> owner;
        ReadableComboRenderer(JComboBox<?> owner) { this.owner = owner; }
        @Override public void setEnabled(boolean enabled) { super.setEnabled(true); }
        @Override public void setForeground(Color color) {
            super.setForeground(owner != null && !owner.isEnabled() ? INK : color);
        }
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean selected, boolean focus) {
            super.getListCellRendererComponent(list, value, index, selected, focus);
            if (!owner.isEnabled()) { setForeground(INK); setBackground(SURFACE); }
            return this;
        }
    }
}
