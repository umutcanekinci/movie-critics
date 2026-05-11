package org.example.dialog;

import org.example.DatabaseManager;
import org.example.data.Movie;
import org.example.data.User;
import org.example.data.UserRating;
import org.example.widget.UIHelper;
import org.example.widget.WidgetFactory;

import javax.swing.*;
import java.awt.*;

public class RateDialog extends JDialog {

    private final Movie           movie;
    private final User            user;
    private final DatabaseManager dbManager;
    private final Runnable        onSave;

    private JSpinner  ratingSpinner;
    private JTextArea commentArea;

    public RateDialog(Frame parent, Movie movie, User user, DatabaseManager dbManager, Runnable onSave) {
        super(parent, true);
        this.movie     = movie;
        this.user      = user;
        this.dbManager = dbManager;
        this.onSave    = onSave;
        setUndecorated(true);
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WidgetFactory.DARK);
        root.setBorder(BorderFactory.createLineBorder(WidgetFactory.PURPLE, 2));

        root.add(buildTitleBar(),  BorderLayout.NORTH);
        root.add(buildForm(),      BorderLayout.CENTER);
        root.add(buildButtonRow(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(30, 30, 30));
        bar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 12));
        JLabel lbl = new JLabel("Rate: " + movie.getTitle());
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 15));
        bar.add(lbl, BorderLayout.WEST);
        JButton x = WidgetFactory.createFlatButton("✕", new Color(30, 30, 30));
        x.addActionListener(e -> dispose());
        bar.add(x, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WidgetFactory.DARK);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        UserRating existing = dbManager.getUserRating(user.getId(), movie.getId());

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(WidgetFactory.createDialogLabel("Rating (1-5 stars)", 130), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        SpinnerNumberModel spinModel = new SpinnerNumberModel(
            existing != null ? existing.getRating() : 3, 1, 5, 1);
        ratingSpinner = new JSpinner(spinModel);
        ratingSpinner.setBackground(new Color(35, 35, 35));
        ratingSpinner.setForeground(Color.WHITE);
        ratingSpinner.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 14));
        form.add(ratingSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(WidgetFactory.createDialogLabel("Comment:", 130), gbc);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1; gbc.weightx = 1;
        commentArea = new JTextArea(existing != null ? existing.getComment() : "", 4, 22);
        commentArea.setBackground(new Color(35, 35, 35));
        commentArea.setForeground(Color.WHITE);
        commentArea.setCaretColor(Color.WHITE);
        commentArea.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 13));
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        JScrollPane commentScroll = new JScrollPane(commentArea);
        commentScroll.setBorder(null);
        form.add(commentScroll, gbc);
        return form;
    }

    private JPanel buildButtonRow() {
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btns.setBackground(WidgetFactory.DARK);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(45, 45, 45)));
        JButton cancel = WidgetFactory.createFlatButton("Cancel", new Color(60, 60, 60));
        cancel.setPreferredSize(new Dimension(90, 34));
        cancel.addActionListener(e -> dispose());
        JButton save = WidgetFactory.createFlatButton("Save", WidgetFactory.PURPLE);
        save.setPreferredSize(new Dimension(90, 34));
        save.addActionListener(e -> trySave());
        btns.add(cancel);
        btns.add(save);
        return btns;
    }

    private void trySave() {
        int rating     = (int) ratingSpinner.getValue();
        String comment = commentArea.getText().trim();
        boolean ok = UIHelper.runWithDbHandler(this, () -> {
            dbManager.addOrUpdateRating(user.getId(), movie.getId(), rating, comment);
        });
        if (ok) {
            if (onSave != null) onSave.run();
            dispose();
        }
    }
}
