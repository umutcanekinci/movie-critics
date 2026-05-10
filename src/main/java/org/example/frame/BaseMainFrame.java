package org.example.frame;

import org.example.DatabaseManager;
import org.example.data.User;
import org.example.widget.WidgetFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseMainFrame extends javax.swing.JFrame {

    protected final DatabaseManager dbManager;
    protected final User user;

    protected String activeTab;
    protected final Map<String, JLabel> tabLabels = new LinkedHashMap<>();

    protected JPanel contentArea;
    protected JButton prevBtn;
    protected JButton nextBtn;

    public BaseMainFrame(DatabaseManager dbManager, User user, String initialTab) {
        this.dbManager = dbManager;
        this.user = user;
        this.activeTab = initialTab;

        setUndecorated(true);
        
        rootPane.registerKeyboardAction(e -> { new LoginFrame().setVisible(true); dispose(); },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    protected void initSharedComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildHeader(), BorderLayout.NORTH);
        getContentPane().add(buildWrapper(), BorderLayout.CENTER);
        
        JPanel footer = new JPanel();
        footer.setBackground(WidgetFactory.DARK);
        footer.setPreferredSize(new Dimension(0, 50));
        getContentPane().add(footer, BorderLayout.SOUTH);
        pack();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(WidgetFactory.DARK);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(45, 45, 45)),
            BorderFactory.createEmptyBorder(0, 40, 0, 40)));
        header.add(buildLogo(), BorderLayout.WEST);
        header.add(buildNavBar(), BorderLayout.CENTER);
        header.add(buildUserTag(), BorderLayout.EAST);
        return header;
    }

    private JPanel buildLogo() {
        JLabel logo = new JLabel(new ImageIcon(getClass().getResource("/logo.png")));
        logo.setOpaque(false);
        JPanel w = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        w.setOpaque(false);
        w.add(logo);
        return w;
    }

    private JPanel buildUserTag() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel("Hi, " + user.getUsername());
        lbl.setForeground(new Color(180, 180, 180));
        lbl.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 14));
        p.add(lbl);
        return p;
    }

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        nav.setOpaque(false);
        for (String tab : getTabNames()) {
            JLabel lbl = new JLabel(tab) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (activeTab.equals(tab)) {
                        g.setColor(Color.WHITE);
                        g.fillRect(0, getHeight() - 3, getWidth(), 3);
                    }
                }
            };
            lbl.setForeground(Color.WHITE);
            lbl.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 18));
            lbl.setVerticalAlignment(SwingConstants.CENTER);
            lbl.setPreferredSize(new Dimension(lbl.getPreferredSize().width, 78));
            lbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lbl.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { 
                    activeTab = tab;
                    tabLabels.values().forEach(Component::repaint);
                    onTabClicked(tab); 
                }
            });
            tabLabels.put(tab, lbl);
            nav.add(lbl);
        }
        return nav;
    }

    private JPanel buildWrapper() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(WidgetFactory.PURPLE);

        prevBtn = WidgetFactory.createArrowButton("←"); prevBtn.addActionListener(e -> navigate(-1));
        nextBtn = WidgetFactory.createArrowButton("→"); nextBtn.addActionListener(e -> navigate(1));
        wrapper.add(centeredArrowPane(prevBtn), BorderLayout.WEST);
        wrapper.add(centeredArrowPane(nextBtn), BorderLayout.EAST);

        JPanel padded = new JPanel(new BorderLayout());
        padded.setBackground(WidgetFactory.PURPLE);
        padded.setBorder(BorderFactory.createEmptyBorder(20, 20, 40, 20));

        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(WidgetFactory.PURPLE);
        contentArea.setOpaque(false);
        padded.add(contentArea, BorderLayout.CENTER);
        wrapper.add(padded, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel centeredArrowPane(JButton btn) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(WidgetFactory.PURPLE);
        p.setPreferredSize(new Dimension(90, 0));
        p.add(btn);
        return p;
    }

    protected abstract String[] getTabNames();
    protected abstract void onTabClicked(String tab);
    protected abstract void navigate(int dir);
}
