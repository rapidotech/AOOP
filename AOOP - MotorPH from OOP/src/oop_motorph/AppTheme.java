package oop_motorph;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.LayerUI;
import javax.swing.text.JTextComponent;

/** Shared, native Swing presentation and recoverable persistence errors. */
public final class AppTheme {
    public static final Color INK = new Color(27, 45, 38);
    public static final Color MUTED = new Color(89, 106, 97);
    public static final Color GREEN = new Color(22, 112, 76);
    public static final Color GREEN_HOVER = new Color(16, 89, 59);
    public static final Color LINE = new Color(214, 224, 215);
    public static final Color SURFACE = new Color(244, 246, 240);
    public static final Color FOREST = new Color(20, 57, 43);
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
            if (frame instanceof WorkspaceScreen && !Boolean.TRUE.equals(frame.getRootPane().getClientProperty("motorph.workspace"))) {
                ((WorkspaceScreen) frame).composeWorkspace();
            }
            apply(frame.getContentPane());
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
        } else if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            if ("Phone Number:".equals(label.getText())) {
                label.setText("Phone:");
            }
        } else if (component instanceof JTable) {
            JTable table = (JTable) component;
            table.setRowHeight(34);
            table.setForeground(INK);
            table.setBackground(Color.WHITE);
            table.setSelectionBackground(new Color(220, 252, 231));
            table.setSelectionForeground(INK);
            table.setGridColor(new Color(229, 231, 235));
            table.setShowVerticalLines(false);
            table.setFillsViewportHeight(true);
            table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
            table.getTableHeader().setForeground(INK);
            table.getTableHeader().setPreferredSize(new Dimension(0, 38));
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
        String role = String.valueOf(button.getClientProperty("motorph.buttonRole"));
        boolean sidebar = role.startsWith("sidebar");
        boolean solid = "primary".equals(role) || "danger".equals(role) || sidebar;
        Color base = sidebar ? ("sidebar-active".equals(role) ? new Color(39, 86, 63) : FOREST)
                : "danger".equals(role) ? new Color(165, 46, 43) : "primary".equals(role) ? GREEN : Color.WHITE;
        Color foreground = solid ? Color.WHITE : INK;
        button.setFont(new Font("Segoe UI", sidebar ? Font.PLAIN : Font.BOLD, 13));
        button.setBackground(base); button.setForeground(foreground);
        button.setPreferredSize(null); button.setMinimumSize(new Dimension(32, 36));
        button.setMargin(new Insets(9, 13, 9, 13));
        button.setBorder(BorderFactory.createEmptyBorder(sidebar ? 11 : 9, 13, sidebar ? 11 : 9, 13));
        button.setFocusPainted(true); button.setContentAreaFilled(true); button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText(button.getText());
        UIDefaults overrides = new UIDefaults();
        javax.swing.Painter<JComponent> painter = (g, c, w, h) -> {
            Graphics2D graphics = (Graphics2D) g.create();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ButtonModel model = ((JButton)c).getModel();
            Color fill = !c.isEnabled() ? (sidebar ? FOREST : new Color(232, 236, 230))
                    : model.isPressed() ? base.darker() : model.isRollover() ? (solid ? base.brighter() : SURFACE) : base;
            graphics.setColor(fill); graphics.fillRoundRect(1, 1, w-2, h-2, 8, 8);
            if (!sidebar) { graphics.setColor(solid ? base : LINE); graphics.drawRoundRect(1, 1, w-3, h-3, 8, 8); }
            if (c.isFocusOwner()) { graphics.setColor(sidebar ? new Color(174, 216, 189) : GREEN); graphics.setStroke(new BasicStroke(2)); graphics.drawRoundRect(2, 2, w-5, h-5, 8, 8); }
            graphics.dispose();
        };
        for (String state : new String[]{"Enabled", "Focused", "MouseOver", "Pressed", "Default", "Default+Focused", "Default+MouseOver", "Default+Pressed", "Disabled", "Focused+MouseOver", "Focused+Pressed"}) {
            overrides.put("Button["+state+"].backgroundPainter", painter);
            overrides.put("Button["+state+"].textForeground", "Disabled".equals(state) ? MUTED : foreground);
        }
        button.putClientProperty("Nimbus.Overrides", overrides);
        button.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
        button.putClientProperty("motorph.themed", Boolean.TRUE);
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
