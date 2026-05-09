package org.example.widget;

import javax.swing.*;

public class LogoAnimator {

    private static final int DURATION_IN  = 900;
    private static final int DURATION_OUT = 700;

    private final JLabel iconLabel;
    private final JPanel panel;

    private int x;
    private int startY;
    private int finalY;

    public LogoAnimator(JLabel iconLabel, JPanel panel) {
        this.iconLabel = iconLabel;
        this.panel     = panel;
    }

    public void animateIn(int x, int fromY, int toY, Runnable onComplete) {
        this.x      = x;
        this.startY = fromY;
        this.finalY = toY;
        animate(fromY, toY, DURATION_IN, false, onComplete);
    }

    public void animateOut(Runnable onComplete) {
        animate(finalY, startY, DURATION_OUT, true, onComplete);
    }

    private void animate(int fromY, int toY, int duration, boolean easeIn, Runnable onComplete) {
        long startTime = System.currentTimeMillis();
        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float t      = Math.min(1f, (float) elapsed / duration);
            float eased  = easeIn ? t * t * t : 1f - (1f - t) * (1f - t) * (1f - t);

            iconLabel.setLocation(x, (int) (fromY + (toY - fromY) * eased));
            panel.repaint();

            if (t >= 1f) {
                ((Timer) e.getSource()).stop();
                if (onComplete != null) onComplete.run();
            }
        });
        timer.start();
    }
}
