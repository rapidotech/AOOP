package oop_motorph;

import java.awt.*;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.LongFunction;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Owned, read-only HR view of security and business audit events. */
public final class AuditTrailDialog extends JDialog {
    private final LongFunction<List<AuditEvent>> loader;
    private final Object authorizedSession;
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextArea details;
    private final JLabel status;
    private final JButton newest;
    private final JButton older;
    private List<AuditEvent> page = List.of();
    private long nextBefore;
    private long generation;

    public AuditTrailDialog(Window owner) {
        this(owner, DatabaseHandler::getAuditEvents);
    }

    AuditTrailDialog(Window owner, LongFunction<List<AuditEvent>> loader) {
        super(owner, "Audit trail \u2014 MotorPH", ModalityType.MODELESS);
        AuthorizationService.requireHr();
        authorizedSession = AuthorizationService.requireAuthenticated();
        this.loader = loader;
        model = new DefaultTableModel(new String[]{"Event ID", "Time (UTC)", "Actor", "Action", "Target", "Outcome", "Source machine"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        details = new JTextArea(4, 60);
        status = new JLabel("Loading audit events\u2026");
        newest = new JButton("Newest"); older = new JButton("Older");
        setName("auditTrail");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(AppTheme.SURFACE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);
        JPanel title = new JPanel(new GridLayout(2, 1, 0, 4));
        title.setOpaque(false);
        JLabel heading = new JLabel("Audit trail");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        heading.setForeground(AppTheme.INK);
        JLabel introduction = new JLabel("Read-only security and business events. Times are UTC.");
        introduction.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        introduction.setForeground(AppTheme.MUTED);
        title.add(heading); title.add(introduction);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        newest.setName("newestAuditEvents"); newest.setMnemonic('N');
        newest.setToolTipText("Refresh the latest 100 audit events");
        older.setName("olderAuditEvents"); older.setMnemonic('O');
        older.setToolTipText("Load events before the oldest ID on this page");
        newest.addActionListener(event -> load(0));
        older.addActionListener(event -> { if (nextBefore > 0) load(nextBefore); });
        actions.add(newest); actions.add(older);
        header.add(title, BorderLayout.CENTER); header.add(actions, BorderLayout.EAST);
        table.setName("auditEventsTable");
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.putClientProperty("html.disable", Boolean.TRUE);
        table.setDefaultRenderer(Object.class, renderer);
        int[] widths = {60, 165, 105, 170, 100, 95, 310};
        for (int column = 0; column < widths.length; column++) table.getColumnModel().getColumn(column).setPreferredWidth(widths[column]);
        table.getSelectionModel().addListSelectionListener(event -> { if (!event.getValueIsAdjusting()) showSelection(); });
        details.setName("auditEventDetails");
        details.setEditable(false);
        details.setLineWrap(true); details.setWrapStyleWord(true);
        details.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        details.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        details.getAccessibleContext().setAccessibleName("Selected audit event details");
        JLabel detailLabel = new JLabel("Selected event details");
        detailLabel.setLabelFor(details);
        JPanel selection = new JPanel(new BorderLayout(0, 6));
        selection.setOpaque(false);
        selection.add(detailLabel, BorderLayout.NORTH);
        selection.add(new JScrollPane(details), BorderLayout.CENTER);
        status.setName("auditEventsStatus");
        status.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        status.setForeground(AppTheme.MUTED);
        JPanel footer = new JPanel(new BorderLayout(0, 12));
        footer.setOpaque(false);
        footer.add(selection, BorderLayout.CENTER); footer.add(status, BorderLayout.SOUTH);
        body.add(header, BorderLayout.NORTH); body.add(new JScrollPane(table), BorderLayout.CENTER);
        body.add(footer, BorderLayout.SOUTH);
        setContentPane(body);
        AppTheme.apply(body);
        getRootPane().registerKeyboardAction(event -> dispose(), KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setSize(1120, 680); setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(owner);
        load(0);
    }

    void load(long beforeEventId) {
        AuthorizationService.requireHr();
        if (AuthorizationService.requireAuthenticated() != authorizedSession) {
            clearPage(); dispose();
            throw new AuthorizationException("Sign in again before opening the audit trail.");
        }
        final long request = ++generation;
        clearPage(); older.setEnabled(false);
        status.setText("Loading audit events\u2026");
        setEmptyMessage("Loading audit events\u2026");
        new SwingWorker<List<AuditEvent>, Void>() {
            @Override protected List<AuditEvent> doInBackground() { return loader.apply(beforeEventId); }
            @Override protected void done() {
                if (request != generation || !isDisplayable()) return;
                try {
                    AuthorizationService.requireHr();
                    if (AuthorizationService.requireAuthenticated() != authorizedSession) {
                        clearPage(); dispose(); return;
                    }
                    page = List.copyOf(get());
                    DateTimeFormatter time = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);
                    for (AuditEvent event : page) {
                        model.addRow(new Object[]{event.getEventId(), time.format(event.getOccurredAt()), event.getActor(),
                            event.getAction(), event.getTarget(), event.getOutcome(), event.getSourceMachine()});
                    }
                    nextBefore = page.stream().mapToLong(AuditEvent::getEventId).min().orElse(0);
                    older.setEnabled(page.size() == 100 && nextBefore > 0);
                    String empty = beforeEventId == 0 ? "No audit events." : "No older audit events. Choose Newest to reload.";
                    setEmptyMessage(empty);
                    status.setText(page.isEmpty() ? empty : page.size() + " audit events shown (100 maximum). Select an event for details.");
                    if (!page.isEmpty()) {
                        table.setRowSelectionInterval(0, 0);
                        showSelection();
                    }
                } catch (AuthorizationException ex) {
                    clearPage(); newest.setEnabled(false); older.setEnabled(false);
                    setEmptyMessage("Access denied."); status.setText("Access denied. An authorized HR session is required.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); clearPage();
                    setEmptyMessage("Loading was interrupted."); status.setText("Loading was interrupted. Choose Newest to try again.");
                } catch (ExecutionException ex) {
                    clearPage(); setEmptyMessage("Audit trail unavailable.");
                    status.setText("Audit trail unavailable. Check the connection and choose Newest to retry.");
                }
            }
        }.execute();
    }

    private void showSelection() {
        int selected = table.getSelectedRow();
        if (selected < 0 || selected >= table.getRowCount() || selected >= model.getRowCount()
                || (table.getRowSorter() != null && selected >= table.getRowSorter().getViewRowCount())) {
            details.setText(""); return;
        }
        int row = table.convertRowIndexToModel(selected);
        if (row >= page.size()) { details.setText(""); return; }
        AuditEvent event = page.get(row);
        details.setText("Event " + event.getEventId() + " | " + event.getAction() + " | " + event.getOutcome()
                + "\nActor: " + event.getActor() + " | Target: " + event.getTarget()
                + " | Source: " + event.getSourceMachine() + "\nDetails: " + event.getDetails());
        details.setCaretPosition(0);
    }

    private void clearPage() {
        table.clearSelection();
        table.getSelectionModel().setAnchorSelectionIndex(-1);
        table.getSelectionModel().setLeadSelectionIndex(-1);
        model.setRowCount(0); page = List.of(); nextBefore = 0; details.setText("");
    }

    private void setEmptyMessage(String message) { table.putClientProperty("motorph.emptyMessage", message); table.repaint(); }

    @Override public void dispose() {
        generation++;
        if (table != null) clearPage();
        super.dispose();
    }
}
