package org.example.widget;

import org.example.data.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;


public class UserCard extends JPanel {
    private final User user;
    private boolean hovered;

    public UserCard(User user, String role, Runnable onEdit) {
        this.user = user;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) { onEdit.run(); }
        });
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int titleH = 36;
        int imgH   = getHeight() - titleH - 8;

        // Card background
        g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), imgH, 10, 10));
        g2.setColor(hovered ? new Color(50, 35, 75) : WidgetFactory.CARD_BG);
        g2.fillRect(0, 0, getWidth(), imgH);

        // Avatar circle with first initial
        int avR = Math.min(getWidth(), imgH) / 4;
        int avX = getWidth() / 2 - avR;
        int avY = imgH / 2 - avR;
        g2.setColor(WidgetFactory.PURPLE);
        g2.fillOval(avX, avY, avR * 2, avR * 2);
        g2.setFont(new Font(WidgetFactory.FONT, Font.BOLD, avR));
        g2.setColor(Color.WHITE);
        FontMetrics fmA = g2.getFontMetrics();
        String init = user.getUsername().substring(0, 1).toUpperCase();
        g2.drawString(init,
                getWidth() / 2 - fmA.stringWidth(init) / 2,
                imgH / 2 + (fmA.getAscent() - fmA.getDescent()) / 2);

        g2.setClip(null);

        // Username below
        g2.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 15));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        String name = user.getUsername();
        g2.drawString(name, (getWidth() - fm.stringWidth(name)) / 2, imgH + 8 + fm.getAscent());
        g2.dispose();
    }
}
