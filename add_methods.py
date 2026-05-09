import os

with open('src/main/java/org/example/widget/WidgetFactory.java', 'r', encoding='utf-8') as f:
    content = f.read()

snippet = """
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
"""

content = content.rstrip().rsplit('}', 1)[0] + snippet
with open('src/main/java/org/example/widget/WidgetFactory.java', 'w', encoding='utf-8') as f:
    f.write(content)
