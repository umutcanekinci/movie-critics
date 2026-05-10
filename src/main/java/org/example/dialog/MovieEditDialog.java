package org.example.dialog;

import org.example.DatabaseManager;
import org.example.data.Movie;

import org.example.widget.WidgetFactory;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

import org.example.widget.UIHelper;

public class MovieEditDialog extends BaseEditDialog {

    private final transient Movie movie;
    private final transient DatabaseManager dbManager;

    private JTextField titleField;
    private JTextField yearField;
    private JTextField languageField;
    private JTextField countryField;
    private JTextField genreField;
    private JTextField directorField;
    private JTextField leadingActorField;
    private JTextField supportingActorField;
    private JTextField ratingField;
    private JTextField posterField;
    private JTextArea  aboutArea;
    private JTextArea  commentsArea;
    private JCheckBox  watchedCheck;
    private JCheckBox  restrictionCheck;

    public MovieEditDialog(Frame parent, Movie movie, DatabaseManager dbManager, Runnable onSave) {
        super(parent, onSave);
        this.movie     = movie;
        this.dbManager = dbManager;
        setup();
    }

    @Override
    public String getDialogTitle() {
        return movie == null ? "Add Movie" : "Edit Movie";
    }

    @Override
    public boolean isEditMode() {
        return movie != null;
    }

    @Override
    public JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WidgetFactory.DARK);
        form.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        String title    = movie != null ? movie.getTitle()                       : "";
        String year     = movie != null ? String.valueOf(movie.getReleaseYear()) : "";
        String lang     = movie != null ? movie.getLanguage()                    : "";
        String country  = movie != null ? movie.getOriginCountry()               : "";
        String genre    = movie != null ? movie.getGenre()                       : "";
        String director = movie != null ? movie.getDirectorId()                  : "";
        String lead     = movie != null ? movie.getLeadingActorId()              : "";
        String support  = movie != null ? movie.getSupportingActorId()           : "";
        String rating   = movie != null ? String.valueOf(movie.getRating())      : "";
        String poster   = movie != null ? movie.getPoster()                      : null;

        int r = 0;
        titleField           = textRow(form, gbc, r++, "Title",            title);
        yearField            = textRow(form, gbc, r++, "Release Year",     year);
        languageField        = textRow(form, gbc, r++, "Language",         lang);
        countryField         = textRow(form, gbc, r++, "Country",          country);
        genreField           = textRow(form, gbc, r++, "Genre",            genre);
        directorField        = textRow(form, gbc, r++, "Director",         director);
        leadingActorField    = textRow(form, gbc, r++, "Leading Actor",    lead);
        supportingActorField = textRow(form, gbc, r++, "Supporting Actor", support);
        ratingField          = textRow(form, gbc, r++, "Rating (1–10)",    rating);
        posterField          = posterRow(form, gbc, r++, poster);
        aboutArea            = areaRow(form, gbc, r++, "About",    movie != null ? movie.getAbout()    : "");
        commentsArea         = areaRow(form, gbc, r++, "Comments", movie != null ? movie.getComments() : "");

        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 2;
        JPanel checks = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        checks.setBackground(WidgetFactory.DARK);
        watchedCheck     = WidgetFactory.createStyledCheck("Watched",              movie != null && movie.isWatched());
        restrictionCheck = WidgetFactory.createStyledCheck("Parental Restriction", movie != null && movie.isParentalRestriction());
        checks.add(watchedCheck);
        checks.add(restrictionCheck);
        form.add(checks, gbc);

        return form;
    }

    @Override
    public void doSave() {
        try {
            int year   = Integer.parseInt(yearField.getText().trim());
            int rating = Integer.parseInt(ratingField.getText().trim());
            if (rating < 1 || rating > 10) throw new NumberFormatException();

            String poster = posterField.getText().trim();
            Movie m = new Movie(
                movie != null ? movie.getId() : 0,
                titleField.getText().trim(), year,
                languageField.getText().trim(), countryField.getText().trim(),
                genreField.getText().trim(), directorField.getText().trim(),
                watchedCheck.isSelected(), leadingActorField.getText().trim(),
                supportingActorField.getText().trim(), aboutArea.getText().trim(),
                rating, commentsArea.getText().trim(),
                poster.isEmpty() ? null : poster, restrictionCheck.isSelected());

            UIHelper.runWithDbHandler(this, () -> {
                if (movie == null) dbManager.addMovie(m);
                else               dbManager.updateMovie(m);
                onSave.run();
                dispose();
            });
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Release year must be a number and rating must be 1–10.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void doDelete() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete \"" + movie.getTitle() + "\"?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            UIHelper.runWithDbHandler(this, () -> {
                dbManager.deleteMovie(movie.getId());
                onSave.run();
                dispose();
            });
        }
    }

    @Override
    protected JComponent buildFormWrapper() {
        JScrollPane scroll = new JScrollPane(buildForm());
        scroll.setBackground(WidgetFactory.DARK);
        scroll.getViewport().setBackground(WidgetFactory.DARK);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(520, 520));
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        return scroll;
    }

    @Override
    protected int getLabelWidth() {
        return 150;
    }

    private JTextField posterRow(JPanel form, GridBagConstraints gbc, int r, String value) {
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; gbc.weightx = 0;
        form.add(rowLabel("Poster"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;

        JPanel wrap = new JPanel(new BorderLayout(6, 0));
        wrap.setBackground(WidgetFactory.DARK);
        JTextField f = WidgetFactory.createStyledField(value);
        JButton browse = WidgetFactory.createFlatButton("…", WidgetFactory.PURPLE);
        browse.setPreferredSize(new Dimension(32, 28));
        browse.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                f.setText(fc.getSelectedFile().getAbsolutePath());
        });
        wrap.add(f, BorderLayout.CENTER);
        wrap.add(browse, BorderLayout.EAST);
        form.add(wrap, gbc);
        return f;
    }

    private JTextArea areaRow(JPanel form, GridBagConstraints gbc, int r, String label, String value) {
        gbc.gridx = 0; gbc.gridy = r; gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(rowLabel(label), gbc);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1; gbc.weightx = 1;

        JTextArea area = new JTextArea(value != null ? value : "", 3, 20);
        area.setBackground(WidgetFactory.CARD_BG);
        area.setForeground(Color.WHITE);
        area.setCaretColor(Color.WHITE);
        area.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 13));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(null);
        form.add(sp, gbc);
        return area;
    }

}
