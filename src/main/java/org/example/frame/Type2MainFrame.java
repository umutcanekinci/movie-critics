package org.example.frame;

import org.example.DatabaseManager;
import org.example.data.Movie;
import org.example.data.User;
import org.example.data.UserRating;
import org.example.widget.MovieCard;
import org.example.widget.UIHelper;

import javax.imageio.ImageIO;
import org.example.widget.WidgetFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Type2MainFrame extends javax.swing.JFrame {
    private static final int    MOVIE_COLS      = 4;
    private static final int    MOVIES_PER_PAGE = MOVIE_COLS;
    private static final String TAB_MOVIES      = "Movies";
    private static final String TAB_WATCHLIST   = "Watchlist";
    private static final String TAB_PROGRESS    = "Progress";

    private final transient DatabaseManager dbManager;
    private final transient User user;

    private transient List<Movie> allMovies       = new ArrayList<>();
    private transient List<Movie> filteredMovies  = new ArrayList<>();
    private transient List<Movie> watchlistMovies = new ArrayList<>();
    private int moviePage     = 0;
    private int watchlistPage = 0;

    private String activeTab = TAB_MOVIES;
    private final Map<String, JLabel> tabLabels = new LinkedHashMap<>();

    private JPanel  contentArea;
    private JButton prevBtn;
    private JButton nextBtn;
    private JTextField filterTitle;
    private JTextField filterGenre;
    private JTextField filterDirector;
    private JTextField filterYear;

    public Type2MainFrame(DatabaseManager dbManager, User user) {
        this.dbManager = dbManager;
        this.user      = user;
        setUndecorated(true);
        initComponents();
        setExtendedState(MAXIMIZED_BOTH);
        reload();
        showTab(TAB_MOVIES);
        rootPane.registerKeyboardAction(e -> { new LoginFrame().setVisible(true); dispose(); },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void reload() {
        allMovies      = dbManager.getUnrestrictedMovies();
        filteredMovies = new ArrayList<>(allMovies);
        watchlistMovies = dbManager.getWatchlistMovies(user.getId());
    }

    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildHeader(),  BorderLayout.NORTH);
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
        header.add(buildLogo(),    BorderLayout.WEST);
        header.add(buildNavBar(),  BorderLayout.CENTER);
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

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        nav.setOpaque(false);
        for (String tab : new String[]{TAB_MOVIES, TAB_WATCHLIST, TAB_PROGRESS}) {
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
                @Override public void mouseClicked(MouseEvent e) { showTab(tab); }
            });
            tabLabels.put(tab, lbl);
            nav.add(lbl);
        }
        return nav;
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


    private void showTab(String tab) {
        activeTab = tab;
        tabLabels.values().forEach(Component::repaint);
        switch (tab) {
            case TAB_MOVIES    -> { prevBtn.setVisible(true);  nextBtn.setVisible(true);  refreshMovieGrid();     }
            case TAB_WATCHLIST -> { prevBtn.setVisible(true);  nextBtn.setVisible(true);  refreshWatchlistGrid(); }
            case TAB_PROGRESS  -> { prevBtn.setVisible(false); nextBtn.setVisible(false); refreshProgress();      }
            default            -> { prevBtn.setVisible(false); nextBtn.setVisible(false); }
        }
    }


    private void navigate(int dir) {
        if (TAB_WATCHLIST.equals(activeTab)) {
            int maxPage = Math.max(0, (int) Math.ceil((double) watchlistMovies.size() / MOVIES_PER_PAGE) - 1);
            int next = watchlistPage + dir;
            if (next < 0 || next > maxPage) return;
            watchlistPage = next;
            refreshWatchlistGrid();
        } else {
            int maxPage = Math.max(0, (int) Math.ceil((double) filteredMovies.size() / MOVIES_PER_PAGE) - 1);
            int next = moviePage + dir;
            if (next < 0 || next > maxPage) return;
            moviePage = next;
            refreshMovieGrid();
        }
    }

    private void refreshMovieGrid() {
        contentArea.removeAll();
        contentArea.setLayout(new BorderLayout());
        contentArea.add(buildMovieToolbar(), BorderLayout.NORTH);
        contentArea.add(buildMoviePageGrid(filteredMovies, moviePage,
            this::openMovieDetailDialog), BorderLayout.CENTER);
        updateArrows(filteredMovies.size(), moviePage);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private JPanel buildMovieToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        filterTitle    = WidgetFactory.createFilterField(180, "Title…");
        filterGenre    = WidgetFactory.createFilterField(110, "Genre…");
        filterDirector = WidgetFactory.createFilterField(130, "Director…");
        filterYear     = WidgetFactory.createFilterField(65,  "Year");

        JButton filterBtn = WidgetFactory.createToolbarButton("Filter");
        filterBtn.addActionListener(e -> applyFilter());
        JButton clearBtn = WidgetFactory.createToolbarButton("Clear");
        clearBtn.addActionListener(e -> clearFilter());

        bar.add(filterTitle); bar.add(filterGenre);
        bar.add(filterDirector); bar.add(filterYear);
        bar.add(filterBtn); bar.add(clearBtn);
        return bar;
    }

    private void applyFilter() {
        filteredMovies = dbManager.filterMovies(
            filterTitle    != null ? filterTitle.getText().trim()    : null,
            filterGenre    != null ? filterGenre.getText().trim()    : null,
            filterDirector != null ? filterDirector.getText().trim() : null,
            filterYear     != null ? filterYear.getText().trim()     : null,
            true);
        moviePage = 0;
        refreshMovieGrid();
    }

    private void clearFilter() {
        if (filterTitle    != null) filterTitle.setText("");
        if (filterGenre    != null) filterGenre.setText("");
        if (filterDirector != null) filterDirector.setText("");
        if (filterYear     != null) filterYear.setText("");
        filteredMovies = new ArrayList<>(allMovies);
        moviePage = 0;
        refreshMovieGrid();
    }


    private void refreshWatchlistGrid() {
        contentArea.removeAll();
        contentArea.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        JLabel title = new JLabel("My Watchlist");
        title.setForeground(Color.WHITE);
        title.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 18));
        header.add(title, BorderLayout.WEST);
        contentArea.add(header, BorderLayout.NORTH);

        contentArea.add(buildMoviePageGrid(watchlistMovies, watchlistPage,
            this::openMovieDetailDialog), BorderLayout.CENTER);
        updateArrows(watchlistMovies.size(), watchlistPage);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private JPanel buildMoviePageGrid(List<Movie> movies, int page, java.util.function.Consumer<Movie> onClick) {
        JPanel grid = new JPanel(new GridLayout(1, MOVIE_COLS, 24, 24));
        grid.setOpaque(false);

        if (movies.isEmpty()) {
            grid.setLayout(new BorderLayout());
            JLabel msg = new JLabel("No movies here yet.", SwingConstants.CENTER);
            msg.setForeground(Color.WHITE);
            msg.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 18));
            grid.add(msg, BorderLayout.CENTER);
        } else {
            int start = page * MOVIES_PER_PAGE;
            int end   = Math.min(start + MOVIES_PER_PAGE, movies.size());
            for (int i = start; i < end; i++) {
                Movie m = movies.get(i);
                grid.add(new MovieCard(m, () -> onClick.accept(m)));
            }
            for (int i = end - start; i < MOVIE_COLS; i++) {
                JPanel b = new JPanel(); b.setOpaque(false); grid.add(b);
            }
        }
        return grid;
    }

    private void updateArrows(int total, int page) {
        int totalPages = Math.max(1, (int) Math.ceil((double) total / MOVIES_PER_PAGE));
        prevBtn.setEnabled(page > 0);
        nextBtn.setEnabled(page < totalPages - 1);
    }


    private void refreshProgress() {
        contentArea.removeAll();
        contentArea.setLayout(new BorderLayout(0, 24));
        contentArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("My Progress", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 22));
        contentArea.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 3, 20, 20));
        cards.setOpaque(false);

        int totalMovies   = allMovies.size();
        long watchedCount = allMovies.stream().filter(Movie::isWatched).count();
        int ratingsGiven  = dbManager.countRatingsByUser(user.getId());
        int watchlistSize = watchlistMovies.size();
        long notWatched   = totalMovies - watchedCount;
        UserRating best   = getBestRating();

        cards.add(WidgetFactory.createStatCard("Total Movies",    String.valueOf(totalMovies)));
        cards.add(WidgetFactory.createStatCard("Watched",         watchedCount + " / " + totalMovies));
        cards.add(WidgetFactory.createStatCard("Not Watched",     String.valueOf(notWatched)));
        cards.add(WidgetFactory.createStatCard("My Ratings Given", String.valueOf(ratingsGiven)));
        cards.add(WidgetFactory.createStatCard("My Watchlist",    String.valueOf(watchlistSize)));
        cards.add(WidgetFactory.createStatCard("Highest Rated",   best != null ? best.getRating() + "/5" : "—"));

        contentArea.add(cards, BorderLayout.CENTER);

        if (best != null) {
            JLabel hint = new JLabel("Your top-rated movie: " + getMovieTitle(best.getMovieId()),
                SwingConstants.CENTER);
            hint.setForeground(new Color(160, 160, 160));
            hint.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 14));
            contentArea.add(hint, BorderLayout.SOUTH);
        }

        contentArea.revalidate();
        contentArea.repaint();
    }

    private UserRating getBestRating() {
        List<UserRating> ratings = new ArrayList<>();
        for (Movie m : allMovies) {
            UserRating ur = dbManager.getUserRating(user.getId(), m.getId());
            if (ur != null) ratings.add(ur);
        }
        return ratings.stream().max(java.util.Comparator.comparingInt(UserRating::getRating)).orElse(null);
    }

    private String getMovieTitle(int movieId) {
        return allMovies.stream().filter(m -> m.getId() == movieId)
            .map(Movie::getTitle).findFirst().orElse("Unknown");
    }


    private void openMovieDetailDialog(Movie movie) {
        JDialog dlg = new JDialog(this, true);
        dlg.setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WidgetFactory.DARK);
        root.setBorder(BorderFactory.createLineBorder(WidgetFactory.PURPLE, 2));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(30, 30, 30));
        titleBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 12));
        JLabel titleLbl = new JLabel(movie.getTitle());
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 16));
        titleBar.add(titleLbl, BorderLayout.WEST);
        JButton closeBt = WidgetFactory.createFlatButton("✕", new Color(30, 30, 30));
        closeBt.addActionListener(e -> dlg.dispose());
        titleBar.add(closeBt, BorderLayout.EAST);
        root.add(titleBar, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(20, 0));
        body.setBackground(WidgetFactory.DARK);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel posterPanel = WidgetFactory.createPosterPanel(movie.getPoster(), 160, 220);
        body.add(posterPanel, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(WidgetFactory.DARK);

        info.add(WidgetFactory.createInfoLine("Genre",    movie.getGenre()));
        info.add(WidgetFactory.createInfoLine("Year",     String.valueOf(movie.getReleaseYear())));
        info.add(WidgetFactory.createInfoLine("Language", movie.getLanguage()));
        info.add(WidgetFactory.createInfoLine("Director", movie.getDirectorId()));
        info.add(WidgetFactory.createInfoLine("Rating",   movie.getRating() + " / 10"));
        info.add(Box.createVerticalStrut(10));

        if (movie.getAbout() != null && !movie.getAbout().isBlank()) {
            JTextArea aboutLbl = new JTextArea(movie.getAbout());
            aboutLbl.setForeground(new Color(200, 200, 200));
            aboutLbl.setBackground(WidgetFactory.DARK);
            aboutLbl.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 13));
            aboutLbl.setLineWrap(true);
            aboutLbl.setWrapStyleWord(true);
            aboutLbl.setEditable(false);
            aboutLbl.setMaximumSize(new Dimension(320, 80));
            info.add(aboutLbl);
            info.add(Box.createVerticalStrut(10));
        }

        List<UserRating> ratings = dbManager.getRatingsForMovie(movie.getId());
        if (!ratings.isEmpty()) {
            JLabel ratingHdr = new JLabel("Family ratings:");
            ratingHdr.setForeground(new Color(160, 160, 160));
            ratingHdr.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 13));
            info.add(ratingHdr);
            info.add(Box.createVerticalStrut(4));
            for (UserRating ur : ratings) {
                JLabel rl = new JLabel("• " + ur.getUsername() + ": " + ur.getRating() + "/5"
                    + (ur.getComment() != null && !ur.getComment().isBlank() ? " — " + ur.getComment() : ""));
                rl.setForeground(new Color(200, 200, 200));
                rl.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 12));
                info.add(rl);
            }
            info.add(Box.createVerticalStrut(10));
        }

        body.add(info, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(body);
        scroll.setBackground(WidgetFactory.DARK);
        scroll.getViewport().setBackground(WidgetFactory.DARK);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(560, 320));
        root.add(scroll, BorderLayout.CENTER);

        root.add(buildDetailButtons(movie, dlg), BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel buildDetailButtons(Movie movie, JDialog dlg) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(WidgetFactory.DARK);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(45, 45, 45)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        left.setBackground(WidgetFactory.DARK);

        boolean watched = movie.isWatched();
        JButton watchedBtn = WidgetFactory.createFlatButton(watched ? "✓ Watched" : "Mark Watched",
            watched ? new Color(40, 120, 60) : new Color(60, 60, 60));
        watchedBtn.setPreferredSize(new Dimension(130, 34));
        watchedBtn.addActionListener(e -> {
            toggleWatched(movie);
            dlg.dispose();
            showTab(activeTab);
        });
        left.add(watchedBtn);

        boolean inWatchlist = dbManager.isInWatchlist(user.getId(), movie.getId());
        JButton wlBtn = WidgetFactory.createFlatButton(inWatchlist ? "– Watchlist" : "+ Watchlist",
            inWatchlist ? new Color(80, 50, 120) : WidgetFactory.PURPLE);
        wlBtn.setPreferredSize(new Dimension(120, 34));
        wlBtn.addActionListener(e -> {
            UIHelper.runWithDbHandler(this, () -> {
                if (inWatchlist) dbManager.removeFromWatchlist(user.getId(), movie.getId());
                else             dbManager.addToWatchlist(user.getId(), movie.getId());
                reload();
                dlg.dispose();
                showTab(activeTab);
            });
        });
        left.add(wlBtn);

        panel.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        right.setBackground(WidgetFactory.DARK);

        JButton rateBtn = WidgetFactory.createFlatButton("Rate / Comment", WidgetFactory.PURPLE);
        rateBtn.setPreferredSize(new Dimension(140, 34));
        rateBtn.addActionListener(e -> {
            dlg.dispose();
            openRateDialog(movie);
        });
        right.add(rateBtn);

        JButton closeBtn = WidgetFactory.createFlatButton("Close", new Color(60, 60, 60));
        closeBtn.setPreferredSize(new Dimension(90, 34));
        closeBtn.addActionListener(e -> dlg.dispose());
        right.add(closeBtn);

        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private void toggleWatched(Movie movie) {
        Movie updated = new Movie(
            movie.getId(), movie.getTitle(), movie.getReleaseYear(),
            movie.getLanguage(), movie.getOriginCountry(), movie.getGenre(),
            movie.getDirectorId(), !movie.isWatched(),
            movie.getLeadingActorId(), movie.getSupportingActorId(),
            movie.getAbout(), movie.getRating(), movie.getComments(),
            movie.getPoster(), movie.isParentalRestriction());
        UIHelper.runWithDbHandler(this, () -> {
            dbManager.updateMovie(updated);
            reload();
        });
    }


    private void openRateDialog(Movie movie) {
        JDialog dlg = new JDialog(this, true);
        dlg.setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(WidgetFactory.DARK);
        root.setBorder(BorderFactory.createLineBorder(WidgetFactory.PURPLE, 2));

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(30, 30, 30));
        bar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 12));
        JLabel lbl = new JLabel("Rate: " + movie.getTitle());
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 15));
        bar.add(lbl, BorderLayout.WEST);
        JButton x = WidgetFactory.createFlatButton("✕", new Color(30, 30, 30));
        x.addActionListener(e -> dlg.dispose());
        bar.add(x, BorderLayout.EAST);
        root.add(bar, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(WidgetFactory.DARK);
        form.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill   = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);

        UserRating existing = dbManager.getUserRating(user.getId(), movie.getId());

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        form.add(WidgetFactory.createDialogLabel("Rating (1-5 stars)", 130), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        SpinnerNumberModel spinModel = new SpinnerNumberModel(
            existing != null ? existing.getRating() : 3, 1, 5, 1);
        JSpinner ratingSpinner = new JSpinner(spinModel);
        ratingSpinner.setBackground(new Color(35, 35, 35));
        ratingSpinner.setForeground(Color.WHITE);
        ratingSpinner.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 14));
        form.add(ratingSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        form.add(WidgetFactory.createDialogLabel("Comment:", 130), gbc);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1; gbc.weightx = 1;
        JTextArea commentArea = new JTextArea(existing != null ? existing.getComment() : "", 4, 22);
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
        root.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btns.setBackground(WidgetFactory.DARK);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(45, 45, 45)));
        JButton cancel = WidgetFactory.createFlatButton("Cancel", new Color(60, 60, 60));
        cancel.setPreferredSize(new Dimension(90, 34));
        cancel.addActionListener(e -> dlg.dispose());
        JButton save = WidgetFactory.createFlatButton("Save", WidgetFactory.PURPLE);
        save.setPreferredSize(new Dimension(90, 34));
        save.addActionListener(e -> {
            int rating = (int) ratingSpinner.getValue();
            String comment = commentArea.getText().trim();
            UIHelper.runWithDbHandler(this, () -> {
                dbManager.addOrUpdateRating(user.getId(), movie.getId(), rating, comment);
                reload();
                dlg.dispose();
            });
        });
        btns.add(cancel);
        btns.add(save);
        root.add(btns, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

}
