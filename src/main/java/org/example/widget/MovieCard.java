package org.example.widget;

import org.example.data.Movie;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MovieCard extends JPanel {
    private final Movie movie;
    private BufferedImage posterImg;
    private boolean hovered;

    private Timer animTimer;
    private float currentScale = 1.0f;
    private float targetScale = 1.0f;

    public MovieCard(Movie movie, Runnable onEdit) {
        this.movie = movie;
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        String path = movie.getPoster();
        if (path != null && !path.isBlank()) {
            try {
                posterImg = ImageIO.read(new File(path));
            } catch (IOException ex) {
                posterImg = null;
            }
        }

        animTimer = new Timer(16, e -> {
            currentScale += (targetScale - currentScale) * 0.15f; 
            
            if (Math.abs(currentScale - targetScale) < 0.002f) {
                currentScale = targetScale;
                animTimer.stop();
            }
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { 
                hovered = true; 
                targetScale = 1.04f; 
                animTimer.start(); 
            }
            @Override public void mouseExited(MouseEvent e)  { 
                hovered = false; 
                targetScale = 1.0f; 
                animTimer.start(); 
            }
            @Override public void mouseClicked(MouseEvent e) { 
                onEdit.run(); 
            }
        });
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;
        g2.translate(centerX, centerY);
        g2.scale(currentScale, currentScale);
        g2.translate(-centerX, -centerY);

        int titleH = 36;
        int imgH   = getHeight() - titleH - 8;

        g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), imgH, 10, 10));
        if (posterImg != null) {
            g2.drawImage(posterImg, 0, 0, getWidth(), imgH, this);
        } else {
            g2.setColor(WidgetFactory.CARD_BG);
            g2.fillRoundRect(0, 0, getWidth(), imgH, 10, 10);
        }
        
        if (hovered) {
            g2.setColor(new Color(140, 82, 255, 80));
            g2.fillRect(0, 0, getWidth(), imgH);
        }
        g2.setClip(null);

        g2.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 15));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        String title = ellipsize(g2, movie.getTitle(), getWidth());
        g2.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, imgH + 8 + fm.getAscent());
        
        g2.dispose();
    }

    private String ellipsize(Graphics2D g2, String text, int maxW) {
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(text) <= maxW) return text;
        while (!text.isEmpty() && fm.stringWidth(text + "…") > maxW)
            text = text.substring(0, text.length() - 1);
        return text + "…";
    }
}