package oop_motorph;

import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

/** Persistent native navigation around responsive, independently composed pages. */
public final class WorkspaceShell {
    private WorkspaceShell() { }
    @SuppressWarnings("unchecked")
    private static Map<String,JButton> bindings(JFrame frame) {
        Object existing=frame.getRootPane().getClientProperty("motorph.navigation");
        if(existing instanceof Map){return (Map<String,JButton>)existing;}
        Map<String,JButton> buttons=new LinkedHashMap<>();frame.getRootPane().putClientProperty("motorph.navigation",buttons);return buttons;
    }
    private static String canonical(String section){
        if("my-records".equals(section)||"myRecords".equals(section)){return "profile";}
        if("employee-records".equals(section)){return "employees";}return section;
    }
    public static void bindNavigation(JFrame frame,String section,JButton button){bindings(frame).put(canonical(section),button);}
    public static void mount(JFrame frame,String title,String subtitle,String activeSection,JComponent content,JComponent actions){
        AppTheme.install();
        JPanel root=new JPanel(new BorderLayout());root.setName("workspace");root.setBackground(AppTheme.SURFACE);
        frame.getRootPane().putClientProperty("motorph.workspace",Boolean.TRUE);
        JPanel sidebar=new JPanel(new BorderLayout());sidebar.setName("workspace.navigation");sidebar.setBackground(AppTheme.FOREST);sidebar.setPreferredSize(new Dimension(192,0));sidebar.setBorder(BorderFactory.createEmptyBorder(18,12,12,12));
        JPanel brand=new JPanel(new GridLayout(0,1,0,6));brand.setOpaque(false);brand.setBorder(BorderFactory.createEmptyBorder(0,11,16,0));
        JLabel logo=new JLabel("MotorPH");logo.setForeground(Color.WHITE);logo.setFont(new Font("Segoe UI",Font.BOLD,25));brand.add(logo);
        JLabel service=new JLabel("People and payroll");service.setForeground(new Color(185,207,191));service.setFont(new Font("Segoe UI",Font.PLAIN,12));brand.add(service);sidebar.add(brand,BorderLayout.NORTH);
        JPanel navigation=new JPanel();navigation.setLayout(new BoxLayout(navigation,BoxLayout.Y_AXIS));navigation.setOpaque(false);
        navHeading(navigation,"MY WORKSPACE");
        addNav(frame,navigation,"profile","My profile",activeSection);addNav(frame,navigation,"attendance","Attendance & leave",activeSection);addNav(frame,navigation,"salary","My compensation",activeSection);addNav(frame,navigation,"payroll","My payroll",activeSection);
        if("HR".equalsIgnoreCase(EmpUserSession.getInstance().getRole())){
            navigation.add(Box.createVerticalStrut(13));navHeading(navigation,"PEOPLE OPERATIONS");
            addNav(frame,navigation,"employees","Employee records",activeSection);addNav(frame,navigation,"compensation","Compensation",activeSection);addNav(frame,navigation,"requests","Attendance & leave",activeSection);addNav(frame,navigation,"processing","Payroll processing",activeSection);addNav(frame,navigation,"approvals","Payroll approvals",activeSection);
        }
        JPanel navTop=new JPanel(new BorderLayout());navTop.setOpaque(false);navTop.add(navigation,BorderLayout.NORTH);
        JScrollPane navScroll=new JScrollPane(navTop);navScroll.setBorder(BorderFactory.createEmptyBorder());navScroll.setViewportBorder(BorderFactory.createEmptyBorder());navScroll.setOpaque(false);navScroll.getViewport().setOpaque(false);navScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);navScroll.getVerticalScrollBar().setUnitIncrement(24);sidebar.add(navScroll,BorderLayout.CENTER);
        JPanel footer=new JPanel(new BorderLayout(0,7));footer.setOpaque(false);footer.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        JLabel scope=new JLabel("HR".equalsIgnoreCase(EmpUserSession.getInstance().getRole())?"Human Resources":"Employee workspace");scope.setFont(new Font("Segoe UI",Font.PLAIN,11));scope.setForeground(new Color(185,207,191));scope.setBorder(BorderFactory.createEmptyBorder(0,12,0,0));footer.add(scope,BorderLayout.NORTH);
        JButton logout=bindings(frame).get("logout");if(logout==null){logout=new JButton();logout.addActionListener(e->CSVHandler.handleLogout(frame));}configureNav(logout,"logout","Sign out",false);footer.add(logout,BorderLayout.CENTER);sidebar.add(footer,BorderLayout.SOUTH);
        JPanel main=new JPanel(new BorderLayout(0,18));main.setBackground(AppTheme.SURFACE);main.setBorder(BorderFactory.createEmptyBorder(24,26,24,26));
        JPanel top=new JPanel(new BorderLayout(0,16));top.setOpaque(false);
        JPanel heading=new JPanel(new BorderLayout(0,6));heading.setOpaque(false);JLabel pageTitle=new JLabel(title);pageTitle.setName("workspace.pageTitle");frame.getRootPane().putClientProperty("motorph.pageTitle",pageTitle);pageTitle.setFont(new Font("Segoe UI",Font.BOLD,25));pageTitle.setForeground(AppTheme.INK);javax.swing.JLabel identity=new javax.swing.JLabel();
        EmpDetails signedIn=EmpUserSession.getInstance().getCurrentUser();
        if(signedIn!=null){identity.setText(signedIn.getFirstName()+" "+signedIn.getLastName()+"  |  "+("HR".equalsIgnoreCase(EmpUserSession.getInstance().getRole())?"HR workspace":"Employee workspace"));}
        identity.setName("workspace.identity");identity.putClientProperty("html.disable",true);identity.setFont(new Font("Segoe UI",Font.PLAIN,12));identity.setForeground(AppTheme.MUTED);identity.setBorder(BorderFactory.createEmptyBorder(0,0,6,0));
        JPanel titleRow=new JPanel(new BorderLayout(16,0));titleRow.setOpaque(false);titleRow.add(identity,BorderLayout.NORTH);titleRow.add(pageTitle,BorderLayout.CENTER);heading.add(titleRow,BorderLayout.NORTH);if(subtitle!=null&&!subtitle.isBlank()){heading.add(WorkspaceUI.note(subtitle),BorderLayout.CENTER);}top.add(heading,BorderLayout.NORTH);
        JPanel below=new JPanel();below.setLayout(new BoxLayout(below,BoxLayout.Y_AXIS));below.setOpaque(false);
        if(actions!=null){actions.setAlignmentX(Component.LEFT_ALIGNMENT);below.add(actions);}
        JPanel notices=new JPanel();notices.setLayout(new BoxLayout(notices,BoxLayout.Y_AXIS));notices.setOpaque(false);notices.setAlignmentX(Component.LEFT_ALIGNMENT);below.add(notices);frame.getRootPane().putClientProperty("motorph.noticeSlot",notices);top.add(below,BorderLayout.CENTER);
        main.add(top,BorderLayout.NORTH);main.add(content,BorderLayout.CENTER);root.add(sidebar,BorderLayout.WEST);root.add(main,BorderLayout.CENTER);frame.setContentPane(root);
        Rectangle usable=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();frame.setMinimumSize(new Dimension(Math.min(800,usable.width),Math.min(600,usable.height)));frame.setResizable(true);
        if(!Boolean.TRUE.equals(frame.getRootPane().getClientProperty("motorph.keepWindowBounds"))){frame.setSize(Math.min(1180,usable.width),Math.min(800,usable.height));frame.setLocationRelativeTo(null);}
        AppTheme.apply(root);frame.validate();frame.repaint();
    }
    public static void setPageTitle(JFrame frame,String title){Object label=frame.getRootPane().getClientProperty("motorph.pageTitle");if(label instanceof JLabel)((JLabel)label).setText(title);frame.setTitle(title+" | MotorPH");}
    public static void addNotice(JFrame frame,JComponent notice){Object slot=frame.getRootPane().getClientProperty("motorph.noticeSlot");if(slot instanceof JPanel){notice.setAlignmentX(Component.LEFT_ALIGNMENT);((JPanel)slot).add(notice);((JPanel)slot).revalidate();((JPanel)slot).repaint();}}
    private static void navHeading(JPanel panel,String text){JLabel label=new JLabel(text);label.setFont(new Font("Segoe UI",Font.BOLD,10));label.setForeground(new Color(165,192,174));label.setBorder(BorderFactory.createEmptyBorder(0,12,7,0));label.setAlignmentX(Component.LEFT_ALIGNMENT);panel.add(label);}
    private static void addNav(JFrame frame,JPanel panel,String key,String text,String active){JButton button=bindings(frame).get(key);if(button==null){button=new JButton();button.addActionListener(e->open(frame,key));}configureNav(button,key,text,key.equals(canonical(active)));button.setAlignmentX(Component.LEFT_ALIGNMENT);button.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));panel.add(button);panel.add(Box.createVerticalStrut(1));}
    private static void configureNav(JButton button,String key,String text,boolean active){button.setText(text);button.setName("workspace.nav."+("employees".equals(key)?"employee-records":key));button.setPreferredSize(null);button.setMinimumSize(new Dimension(0,36));button.setMaximumSize(new Dimension(Integer.MAX_VALUE,36));button.setHorizontalAlignment(SwingConstants.LEFT);button.setIcon(new NavIcon(key));button.setIconTextGap(10);button.putClientProperty("motorph.buttonRole",active?"sidebar-active":"sidebar");}
    public static void open(JFrame frame,String key){
        JFrame target;
        switch(canonical(key)){
            case "profile":target=new frm_EmpProfile();break;case "attendance":target=new frm_EmpAttLeave();break;case "salary":target=new frm_EmpSalary();break;case "payroll":target=new frm_EmpPayroll();break;
            case "employees":target=new frm_EmployeesRecords();break;case "compensation":target=new frm_EmployeesSalary();break;case "requests":target=new frm_EmployeesAttLeave();break;case "processing":target=new frm_EmployeesPayrollProcess();break;case "approvals":target=new frm_EmployeesPayrollPaid();break;case "request-review":target=new frm_EmpAttLeaveApproval();break;default:throw new IllegalArgumentException("Unknown workspace page.");
        }
        target.getRootPane().putClientProperty("motorph.keepWindowBounds",true);target.setBounds(frame.getBounds());target.setVisible(true);frame.dispose();
    }
    private static final class NavIcon implements Icon{
        private final String key;NavIcon(String key){this.key=key;}
        public int getIconWidth(){return 16;}public int getIconHeight(){return 16;}
        public void paintIcon(Component c,Graphics g,int x,int y){Graphics2D d=(Graphics2D)g.create();d.translate(x,y);d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);d.setColor(c.getForeground());d.setStroke(new BasicStroke(1.4f));
            if("profile".equals(key)||"employees".equals(key)){d.drawOval(5,1,6,6);d.drawRoundRect(2,9,12,6,5,5);}
            else if("logout".equals(key)){d.drawLine(2,2,8,2);d.drawLine(2,2,2,14);d.drawLine(2,14,8,14);d.drawLine(7,8,15,8);d.drawLine(11,4,15,8);d.drawLine(11,12,15,8);}
            else{d.drawRoundRect(2,2,12,12,2,2);d.drawLine(5,6,11,6);d.drawLine(5,9,11,9);if(key.contains("pay")||key.equals("compensation")){d.drawLine(5,12,9,12);}}d.dispose();}
    }
}
