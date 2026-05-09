package org.example.widget;

import javax.swing.*;
import java.awt.*;

public class WidgetFactory {

    public static final Color PURPLE  = new Color(140, 82, 255);
    public static final Color DARK    = new Color(20, 20, 20);
    public static final Color CARD_BG = new Color(25, 25, 25);
    public static final String FONT   = "Segoe UI";

    private WidgetFactory() {} // Prevent instantiation

    public static JButton createFlatButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font(FONT, Font.BOLD, 13));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createToolbarButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(50, 50, 50));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font(FONT, Font.BOLD, 13));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    public static JButton createArrowButton(String symbol) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(25, 25, 25, isEnabled() ? 210 : 80));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setFont(new Font(FONT, Font.BOLD, 22));
                g2.setColor(isEnabled() ? Color.WHITE : new Color(180, 180, 180));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(symbol, (getWidth() - fm.stringWidth(symbol)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(54, 54));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JTextField createFilterField(int width, String placeholder) {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(width, 32));
        f.setBackground(new Color(35, 35, 35));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font(FONT, Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 70)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    public static JTextField createStyledField(String value) {
        JTextField f = new JTextField(value != null ? value : "");
        f.setBackground(CARD_BG);
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font(FONT, Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    public static JLabel createDialogLabel(String text, int width) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(160, 160, 160));
        lbl.setFont(new Font(FONT, Font.PLAIN, 13));
        lbl.setPreferredSize(new Dimension(width, 28));
        return lbl;
    }
    
    public static JCheckBox createStyledCheck(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setBackground(DARK);
        cb.setForeground(Color.WHITE);
        cb.setFont(new Font(FONT, Font.PLAIN, 13));
        cb.setFocusPainted(false);
        return cb;
    }

    public static JPanel createPosterPanel(String path, int w, int h) {
        java.awt.image.BufferedImage img = null;
        if (path != null && !path.isBlank()) {
            try {
                img = javax.imageio.ImageIO.read(new java.io.File(path));
            } catch (Exception ignored) {}
        }
        final java.awt.image.BufferedImage poster = img;

        JPanel p = new JPanel() {
            { setPreferredSize(new Dimension(w, h)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, w, h, 10, 10));
                if (poster != null) {
                    g2.drawImage(poster, 0, 0, w, h, this);
                } else {
                    g2.setColor(CARD_BG);
                    g2.fillRoundRect(0, 0, w, h, 10, 10);
                    g2.setFont(new Font(FONT, Font.BOLD, 14));
                    g2.setColor(new Color(100, 100, 100));
                    FontMetrics fm = g2.getFontMetrics();
                    String txt = "No Image";
                    g2.drawString(txt, (w - fm.stringWidth(txt)) / 2, h / 2);
                }
                g2.dispose();
            }
        };
        return p;
    }

    public static JPanel createStatCard(String label, String value) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font(FONT, Font.BOLD, 28));
        valLbl.setForeground(PURPLE);
        valLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLbl = new JLabel(label, SwingConstants.CENTER);
        nameLbl.setFont(new Font(FONT, Font.PLAIN, 14));
        nameLbl.setForeground(new Color(180, 180, 180));
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(valLbl);
        inner.add(Box.createVerticalStrut(6));
        inner.add(nameLbl);
        card.add(inner);
        return card;
    }

    public static Component createInfoLine(String key, String value) {
        JLabel lbl = new JLabel(key + ": " + (value != null ? value : "—"));
        lbl.setForeground(new Color(200, 200, 200));
        lbl.setFont(new Font(FONT, Font.PLAIN, 13));
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return lbl;
    }
}
