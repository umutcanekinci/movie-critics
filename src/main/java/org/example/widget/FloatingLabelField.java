package org.example.widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class FloatingLabelField extends JPanel {
    private static final String FONT = "Segoe UI";
    private static final Color ACTIVE_COLOR   = new Color(140, 82, 255);
    private static final Color INACTIVE_COLOR = Color.GRAY;
    private static final Color BG             = new Color(51, 51, 51);
    private static final Rectangle LABEL_RECT = new Rectangle(10, 28, 280, 20);
    private static final Rectangle FIELD_RECT = new Rectangle(10, 18, 280, 40);

    private final JTextField field;
    private final JLabel     label;

    public FloatingLabelField(String placeholder) {
        this(placeholder, false);
    }

    public FloatingLabelField(String placeholder, boolean isPassword) {
        setLayout(null);
        setBackground(BG);
        setPreferredSize(new Dimension(300, 64));

        label = new JLabel(placeholder);
        label.setFont(new Font(FONT, Font.PLAIN, 14));
        label.setForeground(INACTIVE_COLOR);
        label.setBounds(LABEL_RECT);

        field = isPassword ? new JPasswordField() : new JTextField();
        if (isPassword) ((JPasswordField) field).setEchoChar('•');
        field.setBackground(BG);
        field.setFont(new Font(FONT, Font.PLAIN, 14));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(null);
        field.setOpaque(false);
        field.setBounds(FIELD_RECT);

        add(field);
        add(label);

        setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, INACTIVE_COLOR));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                label.setFont(new Font(FONT, Font.PLAIN, 11));
                label.setBounds(10, 4, 280, 16);
                label.setForeground(ACTIVE_COLOR);
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACTIVE_COLOR));
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    label.setFont(new Font(FONT, Font.PLAIN, 14));
                    label.setBounds(10, 28, 280, 20);
                }
                label.setForeground(INACTIVE_COLOR);
                setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, INACTIVE_COLOR));
                repaint();
            }
        });
    }

    public JTextField getTextField() {
        return field;
    }
}
