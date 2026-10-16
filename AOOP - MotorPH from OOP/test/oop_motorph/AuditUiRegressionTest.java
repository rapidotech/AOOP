package oop_motorph;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.*;
import javax.swing.*;

/** Audit viewer tests use genuine issued sessions over a test-only JDBC double. */
public final class AuditUiRegressionTest {
    private static int assertions;
    private static UiAuthFixture auth;

    public static void main(String[] args) throws Exception {
        try {
            auth = new UiAuthFixture();
            for (UIManager.LookAndFeelInfo look : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(look.getName())) { UIManager.setLookAndFeel(look.getClassName()); break; }
            }
            deniedBeforeQuery(); entryGeometry(); paginationAndReadOnlyDetails();
            staleWorkerAndRoleLoss(); unavailableAndEmpty();
            System.out.println("Audit UI regression checks passed: " + assertions + " assertions; fake JDBC and issued HR sessions only.");
        } finally {
            SwingUtilities.invokeAndWait(() -> { for (Window window : Window.getWindows()) window.dispose(); });
            if (auth != null) auth.close();
        }
    }

    private static void deniedBeforeQuery() throws Exception {
        AtomicInteger queries = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            EmpUserSession.getInstance().clearSession();
            expectDenied(() -> new AuditTrailDialog(null, cursor -> { queries.incrementAndGet(); return List.of(); }));
        });
        auth.establish("EMPLOYEE");
        SwingUtilities.invokeAndWait(() -> {
            expectDenied(() -> new AuditTrailDialog(null, cursor -> { queries.incrementAndGet(); return List.of(); }));
            check(queries.get() == 0, "Unauthenticated and employee viewers must deny before querying audit data");
            EmpUserSession.getInstance().clearSession();
        });
    }

    private static void entryGeometry() throws Exception {
        auth.establish("HR");
        SwingUtilities.invokeAndWait(() -> {
            SecurityActivityPanel panel = new SecurityActivityPanel(List::of);
            panel.setSize(1050, 76); panel.doLayout();
            for (Component child : panel.getComponents()) if (child instanceof Container) ((Container) child).doLayout();
            JButton audit = find(panel, "reviewAuditTrail", JButton.class);
            JButton alerts = find(panel, "reviewSecurityActivity", JButton.class);
            Rectangle bounds = SwingUtilities.convertRectangle(audit.getParent(), audit.getBounds(), panel);
            check(bounds.x >= 0 && bounds.x + bounds.width <= panel.getWidth(), "Audit trail entry must fit the existing HR toolbar");
            check(audit.getWidth() >= audit.getFontMetrics(audit.getFont()).stringWidth(audit.getText())
                    + audit.getInsets().left + audit.getInsets().right, "Audit entry label must remain fully readable");
            check(audit.getX() > alerts.getX(), "Audit entry must sit beside the existing security activity entry");
            check(audit.getMnemonic() == java.awt.event.KeyEvent.VK_A, "Audit trail entry must have a keyboard mnemonic");
        });
    }

    private static void paginationAndReadOnlyDetails() throws Exception {
        auth.establish("HR");
        AtomicReference<JFrame> owner = new AtomicReference<>();
        AtomicReference<AuditTrailDialog> dialog = new AtomicReference<>();
        AtomicLong cursor = new AtomicLong(-1);
        AtomicBoolean background = new AtomicBoolean();
        List<AuditEvent> first = page(500, 100);
        SwingUtilities.invokeAndWait(() -> {
            owner.set(new JFrame("Offline audit fixture")); owner.get().pack();
            dialog.set(new AuditTrailDialog(owner.get(), before -> {
                background.set(!SwingUtilities.isEventDispatchThread()); cursor.set(before);
                return before == 0 ? first : List.of(event(400));
            }));
            dialog.get().addNotify();
        });
        awaitStatus(dialog.get(), "100 audit");
        SwingUtilities.invokeAndWait(() -> {
            JTable table = find(dialog.get(), "auditEventsTable", JTable.class);
            JTextArea details = find(dialog.get(), "auditEventDetails", JTextArea.class);
            check(background.get(), "Audit loading must run off the Swing event thread");
            check(table.getRowCount() == 100 && cursor.get() == 0, "Newest must load the bounded newest page");
            check(!table.isCellEditable(0, 3) && !details.isEditable(), "Audit events and details must be read-only");
            check("2026-10-17 01:23:45".equals(table.getValueAt(0, 1)), "Audit timestamps must display UTC");
            check(details.getText().contains("status_before=Pending;status_after=Verified"), "Selection must show allowlisted transition details");
            check(details.getText().contains("<html>source</html>"), "Selected source metadata must remain literal plain text");
            Component rendered = table.prepareRenderer(table.getCellRenderer(0, 2), 0, 2);
            check(Boolean.TRUE.equals(((JComponent) rendered).getClientProperty("html.disable")), "Audit actors must not invoke Swing HTML rendering");
            check(find(dialog.get(), "olderAuditEvents", JButton.class).isEnabled(), "A full page must enable Older paging");
            find(dialog.get(), "olderAuditEvents", JButton.class).doClick();
        });
        awaitStatus(dialog.get(), "1 audit");
        SwingUtilities.invokeAndWait(() -> {
            check(cursor.get() == 401, "Older must request event IDs before the minimum ID on the current page");
            check(find(dialog.get(), "auditEventDetails", JTextArea.class).getText().startsWith("Event 400 |"),
                    "Older-page selection details must match the newly displayed record");
            check(!find(dialog.get(), "olderAuditEvents", JButton.class).isEnabled(), "A partial final page must disable Older");
            find(dialog.get(), "newestAuditEvents", JButton.class).doClick();
        });
        awaitStatus(dialog.get(), "100 audit");
        SwingUtilities.invokeAndWait(() -> {
            check(cursor.get() == 0, "Newest must refresh from the newest cursor");
            EmpUserSession.getInstance().clearSession(); owner.get().dispose();
            check(!dialog.get().isDisplayable(), "Parent logout must close the owned audit viewer");
            check(find(dialog.get(), "auditEventDetails", JTextArea.class).getText().isEmpty(), "Closing the viewer must clear cached details");
        });
    }

    private static void staleWorkerAndRoleLoss() throws Exception {
        auth.establish("HR");
        AtomicReference<AuditTrailDialog> dialog = new AtomicReference<>();
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch started = new CountDownLatch(1), release = new CountDownLatch(1), finished = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            dialog.set(new AuditTrailDialog(null, before -> {
                if (calls.incrementAndGet() == 1) {
                    started.countDown();
                    try { release.await(5, TimeUnit.SECONDS); }
                    catch (InterruptedException failure) { Thread.currentThread().interrupt(); }
                    finished.countDown(); return List.of(event(100));
                }
                return List.of(event(999));
            }));
            dialog.get().addNotify();
        });
        check(started.await(3, TimeUnit.SECONDS), "The first audit request must start asynchronously");
        SwingUtilities.invokeAndWait(() -> {
            check(find(dialog.get(), "auditEventsTable", JTable.class).getRowCount() == 0, "Pending audit loading must show no cached rows");
            check("Loading audit events\u2026".equals(find(dialog.get(), "auditEventsTable", JTable.class).getClientProperty("motorph.emptyMessage")),
                    "Pending audit state must not claim no audit events exist");
            dialog.get().load(0);
        });
        awaitStatus(dialog.get(), "1 audit");
        release.countDown(); check(finished.await(3, TimeUnit.SECONDS), "The superseded request must finish");
        Thread.sleep(150);
        SwingUtilities.invokeAndWait(() -> {
            check(Long.valueOf(999).equals(find(dialog.get(), "auditEventsTable", JTable.class).getValueAt(0, 0)), "A superseded worker must never overwrite the newest result");
            dialog.get().dispose();
        });
        CountDownLatch blocked = new CountDownLatch(1), resume = new CountDownLatch(1);
        SwingUtilities.invokeAndWait(() -> {
            dialog.set(new AuditTrailDialog(null, before -> {
                blocked.countDown();
                try { resume.await(5, TimeUnit.SECONDS); }
                catch (InterruptedException failure) { Thread.currentThread().interrupt(); }
                return List.of(event(100));
            }));
            dialog.get().addNotify();
        });
        check(blocked.await(3, TimeUnit.SECONDS), "Role-loss fixture must reach the async query");
        EmpUserSession.getInstance().clearSession(); resume.countDown();
        awaitStatus(dialog.get(), "Access denied");
        SwingUtilities.invokeAndWait(() -> {
            check(find(dialog.get(), "auditEventsTable", JTable.class).getRowCount() == 0, "A lost session must prevent late audit data from becoming visible");
            check(find(dialog.get(), "auditEventDetails", JTextArea.class).getText().isEmpty(), "Role loss must clear audit details");
            dialog.get().dispose();
        });
    }

    private static void unavailableAndEmpty() throws Exception {
        auth.establish("HR");
        AtomicBoolean fail = new AtomicBoolean(true);
        AtomicReference<AuditTrailDialog> dialog = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            dialog.set(new AuditTrailDialog(null, before -> { if (fail.get()) throw new PersistenceException("Offline fixture"); return List.of(); }));
            dialog.get().addNotify();
        });
        awaitStatus(dialog.get(), "unavailable");
        SwingUtilities.invokeAndWait(() -> {
            check(find(dialog.get(), "newestAuditEvents", JButton.class).isEnabled(), "Failed audit loading must allow Newest retry");
            check("Audit trail unavailable.".equals(find(dialog.get(), "auditEventsTable", JTable.class).getClientProperty("motorph.emptyMessage")), "A failed query must not claim the audit trail is empty");
            fail.set(false); find(dialog.get(), "newestAuditEvents", JButton.class).doClick();
        });
        awaitStatus(dialog.get(), "No audit");
        SwingUtilities.invokeAndWait(() -> {
            check(!find(dialog.get(), "olderAuditEvents", JButton.class).isEnabled(), "An empty newest page must disable Older");
            dialog.get().dispose(); EmpUserSession.getInstance().clearSession();
        });
    }

    private static AuditEvent event(long id) {
        return new AuditEvent(id, Instant.parse("2026-10-17T01:23:45Z"), "<html>actor</html>", "PAYROLL_STATUS_CHANGED", "TX-FIXTURE", "SUCCESS",
                "<html>source</html>", "status_before=Pending;status_after=Verified");
    }
    private static List<AuditEvent> page(long newest, int count) {
        List<AuditEvent> page = new ArrayList<>();
        for (int index = 0; index < count; index++) page.add(event(newest - index));
        return page;
    }
    private static void expectDenied(Runnable action) {
        boolean denied = false;
        try { action.run(); } catch (AuthorizationException expected) { denied = true; }
        check(denied, "Audit viewer must require HR before initialization or querying");
    }
    private static void awaitStatus(AuditTrailDialog dialog, String text) throws Exception {
        AtomicBoolean found = new AtomicBoolean(); long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            SwingUtilities.invokeAndWait(() -> found.set(find(dialog, "auditEventsStatus", JLabel.class).getText().contains(text)));
            if (found.get()) return;
            Thread.sleep(20);
        }
        throw new AssertionError("Expected audit status: " + text);
    }
    private static <T extends Component> T find(Container parent, String name, Class<T> type) {
        T result = findOrNull(parent, name, type);
        if (result == null) throw new AssertionError("Missing audit UI control: " + name);
        return result;
    }
    private static <T extends Component> T findOrNull(Container parent, String name, Class<T> type) {
        for (Component component : parent.getComponents()) {
            if (name.equals(component.getName()) && type.isInstance(component)) return type.cast(component);
            if (component instanceof Container) { T found = findOrNull((Container) component, name, type); if (found != null) return found; }
        }
        return null;
    }
    private static void check(boolean value, String message) { assertions++; if (!value) throw new AssertionError(message); }
}
