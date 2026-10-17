package oop_motorph;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.swing.*;

/** A nonblocking HR alert on the existing profile, backed by the protected audit query. */
final class SecurityActivityPanel extends JPanel {
    private final JLabel summary;
    private final JButton refresh;
    private final Supplier<List<SecurityAlert>> alerts;
    private boolean loading;

    static void attach(JFrame frame) {
        AuthorizationService.requireHr();
        WorkspaceShell.addNotice(frame, new SecurityActivityPanel(() -> DatabaseHandler.getSecurityAlerts(0)));
    }

    SecurityActivityPanel(Supplier<List<SecurityAlert>> alerts) {
        AuthorizationService.requireHr();
        this.alerts = alerts;
        summary = new JLabel("Loading security activity\u2026");
        refresh = new JButton("Refresh alerts");
        setName("securityActivity");
        setLayout(new BorderLayout(0, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.LINE),
                BorderFactory.createEmptyBorder(14, 18, 12, 18)));
        summary.setName("securitySummary");
        summary.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        summary.setForeground(AppTheme.INK);
        summary.setToolTipText("Counts cover the latest 100 sign-in failures and account lockouts.");
        JPanel actions = WorkspaceUI.actions();
        actions.setOpaque(false);
        JButton review = new JButton("Review security activity");
        review.setName("reviewSecurityActivity");
        review.setMnemonic('K');
        review.addActionListener(event -> new frm_SecurityEvents(SwingUtilities.getWindowAncestor(this)).setVisible(true));
        JButton audit = new JButton("Audit trail");
        audit.setName("reviewAuditTrail");
        audit.setMnemonic('A');
        audit.addActionListener(event -> new AuditTrailDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true));
        refresh.setName("refreshSecurityAlerts");
        refresh.addActionListener(event -> refresh());
        actions.add(refresh); actions.add(review); actions.add(audit);
        add(summary, BorderLayout.NORTH); add(actions, BorderLayout.CENTER);
        AppTheme.apply(this);
        refresh();
    }

    void refresh() {
        AuthorizationService.requireHr();
        if (loading) return;
        loading = true;
        refresh.setEnabled(false);
        summary.setText("Loading security activity\u2026");
        new SwingWorker<List<SecurityAlert>, Void>() {
            @Override protected List<SecurityAlert> doInBackground() { return alerts.get(); }
            @Override protected void done() {
                loading = false;
                refresh.setEnabled(true);
                try {
                    AuthorizationService.requireHr();
                    List<SecurityAlert> events = get();
                    long locked = events.stream().filter(event -> "AUTH_LOCKED".equals(event.getAction())).count();
                    summary.setForeground(locked > 0 ? new Color(153, 27, 27) : AppTheme.INK);
                    summary.setText(events.isEmpty() ? "No recent sign-in alerts."
                            : "Recent sign-in alerts: " + events.size() + ". Account lockouts: " + locked + ".");
                    summary.getAccessibleContext().setAccessibleDescription(summary.getText());
                } catch (AuthorizationException ex) {
                    summary.setText("Security activity requires an authorized HR session.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    summary.setText("Security activity was interrupted. Refresh to try again.");
                } catch (ExecutionException ex) {
                    summary.setText("Security activity unavailable. Refresh to try again.");
                }
            }
        }.execute();
    }
}
