package oop_motorph;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

/** Native, responsive work surfaces built from the original controls and listeners. */
public final class WorkspaceUI {
    private WorkspaceUI() { }

    public static JPanel actions(Component... controls) {
        JPanel panel=new JPanel(new WrapLayout(FlowLayout.LEADING,8,6));
        panel.setOpaque(false);
        for(Component control:controls){if(control!=null){panel.add(control);}}
        return panel;
    }
    public static JButton primary(JButton button,String text){return button(button,text,"primary");}
    public static JButton secondary(JButton button,String text){return button(button,text,"secondary");}
    public static JButton danger(JButton button,String text){return button(button,text,"danger");}
    private static JButton button(JButton button,String text,String role){button.setText(text);button.putClientProperty("motorph.buttonRole",role);return button;}

    public static JPanel field(String label,JComponent control){return field(new JLabel(label),control);}
    public static JPanel field(JLabel label,JComponent control){
        JPanel panel=new JPanel(new BorderLayout(0,7));panel.setOpaque(false);
        label.setLabelFor(control);label.setFont(new Font("Segoe UI",Font.PLAIN,12));label.setForeground(AppTheme.MUTED);
        label.setBorder(null);label.setIcon(null);label.setPreferredSize(null);label.setMinimumSize(new Dimension(0,18));
        label.putClientProperty("html.disable",Boolean.TRUE);
        control.setFont(new Font("Segoe UI",Font.PLAIN,13));control.setPreferredSize(null);
        control.setMinimumSize(new Dimension(120,36));
        if(!(control instanceof JScrollPane)){control.setPreferredSize(new Dimension(220,36));}
        panel.add(label,BorderLayout.NORTH);panel.add(control,BorderLayout.CENTER);
        panel.setMinimumSize(new Dimension(150,64));
        return panel;
    }
    public static JPanel fields(Object... pairs){
        if(pairs.length%2!=0){throw new IllegalArgumentException("Fields require label/control pairs.");}
        JPanel[] fields=new JPanel[pairs.length/2];
        for(int i=0;i<pairs.length;i+=2){
            if(!(pairs[i+1] instanceof JComponent)){throw new IllegalArgumentException("A field must contain a Swing control.");}
            fields[i/2]=pairs[i] instanceof JLabel?field((JLabel)pairs[i],(JComponent)pairs[i+1]):field(String.valueOf(pairs[i]),(JComponent)pairs[i+1]);
        }
        return new FieldsPanel(fields);
    }
    public static JPanel section(String title,String help,JComponent content){
        JPanel panel=new JPanel(new BorderLayout(0,16));panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,AppTheme.LINE),BorderFactory.createEmptyBorder(20,22,20,22)));
        JPanel heading=new JPanel(new BorderLayout(0,6));heading.setOpaque(false);
        JLabel label=new JLabel(title);label.setFont(new Font("Segoe UI",Font.BOLD,16));label.setForeground(AppTheme.INK);heading.add(label,BorderLayout.NORTH);
        if(help!=null&&!help.isBlank()){heading.add(note(help),BorderLayout.CENTER);}
        panel.add(heading,BorderLayout.NORTH);panel.add(content,BorderLayout.CENTER);return panel;
    }
    public static JPanel column(Component... children){
        JPanel panel=new JPanel(new GridBagLayout());panel.setOpaque(false);int row=0;
        for(Component child:children){if(child==null){continue;}GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.gridy=row++;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;c.anchor=GridBagConstraints.NORTHWEST;c.insets=new Insets(0,0,16,0);panel.add(child,c);}
        GridBagConstraints glue=new GridBagConstraints();glue.gridx=0;glue.gridy=row;glue.weightx=1;glue.weighty=1;glue.fill=GridBagConstraints.BOTH;JPanel filler=new JPanel();filler.setOpaque(false);panel.add(filler,glue);return panel;
    }
    public static JLabel note(String text){JLabel note=new JLabel(text);note.setFont(new Font("Segoe UI",Font.PLAIN,12));note.setForeground(AppTheme.MUTED);note.putClientProperty("html.disable",Boolean.TRUE);note.setBorder(BorderFactory.createEmptyBorder(0,0,2,0));return note;}
    public static JScrollPane scroll(JComponent content){
        JPanel canvas=new ScrollCanvas(content);JScrollPane scroll=new JScrollPane(canvas,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());scroll.setBackground(AppTheme.SURFACE);scroll.getViewport().setBackground(AppTheme.SURFACE);scroll.getVerticalScrollBar().setUnitIncrement(24);return scroll;
    }
    public static JScrollPane table(JTable table){
        table.setPreferredScrollableViewportSize(new Dimension(760,420));table.setAutoResizeMode(table.getColumnCount()>7?JTable.AUTO_RESIZE_OFF:JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        for(int i=0;i<table.getColumnCount();i++){table.getColumnModel().getColumn(i).setPreferredWidth(i==0?100:Math.max(125,Math.min(185,String.valueOf(table.getColumnName(i)).length()*8+26)));table.getColumnModel().getColumn(i).setMinWidth(90);}
        JScrollPane pane=new JScrollPane(table);pane.setBorder(BorderFactory.createLineBorder(AppTheme.LINE));pane.getViewport().setBackground(Color.WHITE);pane.setMinimumSize(new Dimension(0,180));return pane;
    }

    private static final class FieldsPanel extends JPanel{
        private final JPanel[] fields;private int columns=0;
        FieldsPanel(JPanel[] fields){super(new GridBagLayout());this.fields=fields;setOpaque(false);configure(2);addComponentListener(new ComponentAdapter(){@Override public void componentResized(ComponentEvent event){configure(getWidth()<600?1:2);}});}
        private void configure(int count){if(count==columns){return;}columns=count;removeAll();for(int i=0;i<fields.length;i++){GridBagConstraints c=new GridBagConstraints();c.gridx=i%count;c.gridy=i/count;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;c.anchor=GridBagConstraints.NORTHWEST;c.insets=new Insets(0,0,14,c.gridx==0&&count==2?20:0);add(fields[i],c);}revalidate();repaint();}
    }
    private static final class ScrollCanvas extends JPanel implements Scrollable{
        ScrollCanvas(JComponent content){super(new BorderLayout());setOpaque(false);add(content,BorderLayout.NORTH);}
        public Dimension getPreferredScrollableViewportSize(){return new Dimension(760,500);}public int getScrollableUnitIncrement(Rectangle visible,int orientation,int direction){return 24;}public int getScrollableBlockIncrement(Rectangle visible,int orientation,int direction){return Math.max(24,visible.height-24);}public boolean getScrollableTracksViewportWidth(){return true;}public boolean getScrollableTracksViewportHeight(){return false;}
    }
    private static final class WrapLayout extends FlowLayout{
        WrapLayout(int alignment,int horizontal,int vertical){super(alignment,horizontal,vertical);}
        @Override public Dimension preferredLayoutSize(Container target){synchronized(target.getTreeLock()){int width=target.getWidth();if(width<=0){width=760;}Insets insets=target.getInsets();int available=Math.max(100,width-insets.left-insets.right-getHgap()*2),x=0,rowHeight=0,height=getVgap()*2,max=0;for(Component c:target.getComponents()){if(!c.isVisible()){continue;}Dimension d=c.getPreferredSize();if(x>0&&x+d.width>available){height+=rowHeight+getVgap();max=Math.max(max,x);x=0;rowHeight=0;}x+=d.width+getHgap();rowHeight=Math.max(rowHeight,d.height);}height+=rowHeight;max=Math.max(max,x);return new Dimension(max+insets.left+insets.right,height+insets.top+insets.bottom);}}
        @Override public Dimension minimumLayoutSize(Container target){Dimension d=preferredLayoutSize(target);return new Dimension(100,d.height);}
    }
}
