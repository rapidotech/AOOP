package oop_motorph;

import java.awt.*;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** HR-only read view; events come from the database audit trail. */
public final class frm_SecurityEvents extends JDialog {
    private final Supplier<List<SecurityAlert>> alerts;
    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel status;
    private final JButton refresh;
    private boolean loading;

    public frm_SecurityEvents() {
        this(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow(), () -> DatabaseHandler.getSecurityAlerts(0));
    }

    public frm_SecurityEvents(Window owner) {
        this(owner, () -> DatabaseHandler.getSecurityAlerts(0));
    }

    frm_SecurityEvents(Supplier<List<SecurityAlert>> alerts) {
        this(null, alerts);
    }

    frm_SecurityEvents(Window owner, Supplier<List<SecurityAlert>> alerts) {
        super(owner, "Security activity \u2014 MotorPH", ModalityType.MODELESS);
        AuthorizationService.requireHr();
        this.alerts = alerts;
        model = new DefaultTableModel(new String[]{"Event ID", "Time (UTC)", "Account", "Activity", "Source machine"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        status = new JLabel("Loading security activity\u2026");
        refresh = new JButton("Refresh");
        setName("securityEvents");
        setTitle("Security activity \u2014 MotorPH");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(AppTheme.SURFACE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JLabel heading = new JLabel("Security activity");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(AppTheme.INK);
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        refresh.setName("refreshSecurityEvents");
        refresh.setMnemonic('R');
        refresh.addActionListener(event -> refresh());
        header.add(heading, BorderLayout.CENTER); header.add(refresh, BorderLayout.EAST);
        table.setName("securityEventsTable");
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.putClientProperty("html.disable", Boolean.TRUE);
        table.setDefaultRenderer(Object.class, renderer);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] widths = {60, 165, 120, 140, 310};
        for (int column = 0; column < widths.length; column++) table.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
        status.setName("securityEventsStatus");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        status.setForeground(AppTheme.MUTED);
        body.add(header, BorderLayout.NORTH); body.add(new JScrollPane(table), BorderLayout.CENTER);
        body.add(status, BorderLayout.SOUTH);
        setContentPane(body);
        AppTheme.apply(body);
        setSize(900, 560);
        setMinimumSize(new Dimension(660, 400));
        setLocationRelativeTo(owner);
        refresh();
    }

    void refresh() {
        AuthorizationService.requireHr();
        if (loading) return;
        loading = true;
        refresh.setEnabled(false);
        status.setText("Loading security activity\u2026");
        setEmptyMessage("Loading security activity\u2026");
        new SwingWorker<List<SecurityAlert>, Void>() {
            @Override protected List<SecurityAlert> doInBackground() { return alerts.get(); }
            @Override protected void done() {
                loading = false;
                refresh.setEnabled(true);
                if (!isDisplayable()) return;
                try {
                    AuthorizationService.requireHr();
                    List<SecurityAlert> events = get();
                    model.setRowCount(0);
                    DateTimeFormatter time = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
                    for (SecurityAlert event : events) {
                        model.addRow(new Object[]{event.getEventId(), time.format(event.getOccurredAt()),
                            event.getUsername(), activityName(event.getAction()), event.getSourceMachine()});
                    }
                    setEmptyMessage("No recent sign-in alerts.");
                    status.setText(events.isEmpty() ? "No recent sign-in alerts."
                            : events.size() + " recent sign-in alerts shown (latest 100 maximum). Times are UTC.");
                } catch (AuthorizationException ex) {
                    model.setRowCount(0);
                    setEmptyMessage("Access denied.");
                    status.setText("Access denied. An authorized HR session is required.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    setEmptyMessage("Loading was interrupted.");
                    status.setText("Loading was interrupted. Refresh to try again.");
                } catch (ExecutionException ex) {
                    model.setRowCount(0);
                    setEmptyMessage("Security activity unavailable.");
                    status.setText("Security activity unavailable. Check the connection and refresh.");
                }
            }
        }.execute();
    }

    private static String activityName(String action) {
        return "AUTH_LOCKED".equals(action) ? "Account lockout" : "AUTH_FAILURE".equals(action) ? "Failed sign in" : action;
    }

    private void setEmptyMessage(String message) {
        table.putClientProperty("motorph.emptyMessage", message);
        table.repaint();
    }
}
