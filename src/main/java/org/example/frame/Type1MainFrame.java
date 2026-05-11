package org.example.frame;

import org.example.DatabaseManager;
import org.example.data.Movie;
import org.example.data.User;
import org.example.data.UserRating;
import org.example.dialog.MovieEditDialog;
import org.example.dialog.UserEditDialog;
import org.example.widget.MovieCard;
import org.example.widget.UserCard;
import org.example.widget.UIHelper;

import org.example.widget.WidgetFactory;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Type1MainFrame extends BaseMainFrame {

    private static final String TAB_ACCOUNTS  = "Accounts";
    private static final String TAB_MOVIES    = "Movies";
    private static final String TAB_ANALYTICS = "Analytics";

    private static final int MOVIE_COLS      = 4;
    private static final int MOVIES_PER_PAGE = MOVIE_COLS;

    private transient List<User>  allUsers       = new ArrayList<>();

    public Type1MainFrame(DatabaseManager dbManager, User user) {
        super(dbManager, user, TAB_ACCOUNTS);

        initSharedComponents();
        reload();
        showTab(TAB_ACCOUNTS);
        setExtendedState(MAXIMIZED_BOTH);
    }

    @Override
    protected String[] getTabNames() {
        return new String[]{TAB_ACCOUNTS, TAB_MOVIES, TAB_ANALYTICS};
    }

    @Override
    protected void onTabClicked(String tab) {
        showTab(tab); // Artık tab repainting yapmana gerek yok, Base class hallediyor
    }

    @Override
    protected void navigate(int dir) {
        int maxPage = Math.max(0, (int) Math.ceil((double) filteredMovies.size() / MOVIES_PER_PAGE) - 1);
        int next = moviePage + dir;
        if (next < 0 || next > maxPage) return;
        moviePage = next;
        refreshMovieGrid();
    }

    private void reload() {
        allMovies = dbManager.getAllMovies();
        allUsers  = dbManager.getAllUsers();
        filteredMovies = new ArrayList<>(allMovies);
    }

    private void showTab(String tab) {
        activeTab = tab;
        tabLabels.values().forEach(Component::repaint);
        switch (tab) {
            case TAB_MOVIES    -> { prevBtn.setVisible(true);  nextBtn.setVisible(true);  refreshMovieGrid();   }
            case TAB_ACCOUNTS  -> { prevBtn.setVisible(false); nextBtn.setVisible(false); refreshAccountGrid(); }
            case TAB_ANALYTICS -> { prevBtn.setVisible(false); nextBtn.setVisible(false); refreshAnalytics();   }
            default            -> { prevBtn.setVisible(false); nextBtn.setVisible(false); }
        }
    }

    private void refreshMovieGrid() {
        contentArea.removeAll();
        contentArea.setLayout(new BorderLayout());

        JPanel toolbar = buildMovieToolbar();
        contentArea.add(toolbar, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, MOVIE_COLS, 24, 24));
        grid.setOpaque(false);

        if (filteredMovies.isEmpty()) {
            grid.setLayout(new BorderLayout());
            JLabel msg = new JLabel("No movies found.", SwingConstants.CENTER);
            msg.setForeground(Color.WHITE);
            msg.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 18));
            grid.add(msg, BorderLayout.CENTER);
        } else {
            int start = moviePage * MOVIES_PER_PAGE;
            int end   = Math.min(start + MOVIES_PER_PAGE, filteredMovies.size());
            for (int i = start; i < end; i++) {
                Movie m = filteredMovies.get(i);
                grid.add(new MovieCard(m, () -> openMovieDetail(m)));
            }
            for (int i = end - start; i < MOVIE_COLS; i++) {
                JPanel b = new JPanel(); b.setOpaque(false); grid.add(b);
            }
        }
        contentArea.add(grid, BorderLayout.CENTER);

        int totalPages = Math.max(1, (int) Math.ceil((double) filteredMovies.size() / MOVIES_PER_PAGE));
        prevBtn.setEnabled(moviePage > 0);
        nextBtn.setEnabled(moviePage < totalPages - 1);

        contentArea.revalidate();
        contentArea.repaint();
    }

    private JPanel buildMovieToolbar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        filterTitle = WidgetFactory.createFilterField(200, "Search by title…");

        List<String> genres    = allMovies.stream().map(Movie::getGenre).filter(s -> s != null && !s.isBlank()).distinct().sorted().toList();
        List<String> directors = allMovies.stream().map(Movie::getDirectorId).filter(s -> s != null && !s.isBlank()).distinct().sorted().toList();
        List<String> years     = allMovies.stream().map(m -> String.valueOf(m.getReleaseYear())).distinct().sorted().toList();

        filterGenre    = filterCombo("All Genres",    genres);
        filterDirector = filterCombo("All Directors", directors);
        filterYear     = filterCombo("All Years",     years);

        JButton filterBtn = WidgetFactory.createToolbarButton("Filter");
        filterBtn.addActionListener(e -> applyFilter());
        JButton clearBtn = WidgetFactory.createToolbarButton("Clear");
        clearBtn.addActionListener(e -> clearFilter());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filters.setOpaque(false);
        filters.add(filterTitle);
        filters.add(filterGenre); filters.add(filterDirector); filters.add(filterYear);
        filters.add(filterBtn); filters.add(clearBtn);

        JButton addBtn = WidgetFactory.createToolbarButton("+ Add Movie");
        addBtn.setBackground(WidgetFactory.PURPLE);
        addBtn.addActionListener(e -> openMovieEditDialog(null));

        bar.add(filters, BorderLayout.CENTER);
        bar.add(addBtn,  BorderLayout.EAST);
        return bar;
    }

    @Override
    protected void applyFilter() {
        String genre    = filterGenre    != null && filterGenre.getSelectedIndex()    > 0 ? (String) filterGenre.getSelectedItem()    : null;
        String director = filterDirector != null && filterDirector.getSelectedIndex() > 0 ? (String) filterDirector.getSelectedItem() : null;
        String year     = filterYear     != null && filterYear.getSelectedIndex()     > 0 ? (String) filterYear.getSelectedItem()     : null;
        filteredMovies = dbManager.filterMovies(
            filterTitle != null ? filterTitle.getText().trim() : null,
            genre, director, year, false);
        moviePage = 0;
        refreshMovieGrid();
    }
    
    @Override
    protected void onFilterCleared() {
        refreshMovieGrid();
    }

    private void openMovieEditDialog(Movie movie) {
        new MovieEditDialog(this, movie, dbManager, () -> {
            reload();
            filteredMovies = new ArrayList<>(allMovies);
            moviePage = 0;
            refreshMovieGrid();
        }).setVisible(true);
    }

    private void openMovieDetail(Movie movie) {
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
        JButton closeTb = WidgetFactory.createToolbarButton("✕");
        closeTb.setBackground(new Color(30, 30, 30));
        closeTb.addActionListener(e -> dlg.dispose());
        titleBar.add(closeTb, BorderLayout.EAST);
        root.add(titleBar, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(WidgetFactory.DARK);
        body.setBorder(BorderFactory.createEmptyBorder(18, 24, 10, 24));

        for (String[] row : new String[][]{
            {"Genre",    movie.getGenre()},
            {"Year",     String.valueOf(movie.getReleaseYear())},
            {"Language", movie.getLanguage()},
            {"Director", movie.getDirectorId()},
            {"Rating",   movie.getRating() + " / 10"}
        }) {
            JLabel l = new JLabel(row[0] + ": " + (row[1] != null ? row[1] : "—"));
            l.setForeground(new Color(200, 200, 200));
            l.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 13));
            l.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            body.add(l);
        }

        body.add(Box.createVerticalStrut(12));
        JLabel ratingsHdr = new JLabel("Family Ratings:");
        ratingsHdr.setForeground(new Color(160, 160, 160));
        ratingsHdr.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 13));
        body.add(ratingsHdr);
        body.add(Box.createVerticalStrut(6));

        List<UserRating> ratings = dbManager.getRatingsForMovie(movie.getId());
        if (ratings.isEmpty()) {
            JLabel none = new JLabel("No ratings yet.");
            none.setForeground(new Color(120, 120, 120));
            none.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 13));
            body.add(none);
        } else {
            for (UserRating ur : ratings) {
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setOpaque(false);
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
                String txt = ur.getUsername() + ": " + ur.getRating() + "/5"
                    + (ur.getComment() != null && !ur.getComment().isBlank() ? " — " + ur.getComment() : "");
                JLabel rl = new JLabel(txt);
                rl.setForeground(new Color(200, 200, 200));
                rl.setFont(new Font(WidgetFactory.FONT, Font.PLAIN, 13));
                JButton del = WidgetFactory.createToolbarButton("Delete");
                del.setBackground(new Color(160, 40, 40));
                del.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 11));
                del.setPreferredSize(new Dimension(70, 24));
                del.addActionListener(e -> {
                    UIHelper.runWithDbHandler(Type1MainFrame.this, () -> {
                        dbManager.deleteRating(ur.getId());
                        reload();
                        dlg.dispose();
                        openMovieDetail(allMovies.stream()
                            .filter(m -> m.getId() == movie.getId())
                            .findFirst().orElse(movie));
                    });
                });
                row.add(rl,  BorderLayout.CENTER);
                row.add(del, BorderLayout.EAST);
                body.add(row);
            }
        }

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBackground(WidgetFactory.DARK);
        scroll.getViewport().setBackground(WidgetFactory.DARK);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(520, 360));
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        root.add(scroll, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btns.setBackground(WidgetFactory.DARK);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(45, 45, 45)));
        JButton editBtn = WidgetFactory.createToolbarButton("Edit Movie");
        editBtn.setBackground(WidgetFactory.PURPLE);
        editBtn.addActionListener(e -> { dlg.dispose(); openMovieEditDialog(movie); });
        JButton closeBtn = WidgetFactory.createToolbarButton("Close");
        closeBtn.addActionListener(e -> dlg.dispose());
        btns.add(editBtn);
        btns.add(closeBtn);
        root.add(btns, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void refreshAccountGrid() {
        contentArea.removeAll();
        contentArea.setLayout(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        JButton addBtn = WidgetFactory.createToolbarButton("+ Add User");
        addBtn.setBackground(WidgetFactory.PURPLE);
        addBtn.addActionListener(e -> openUserEditDialog(null));
        toolbar.add(addBtn);
        contentArea.add(toolbar, BorderLayout.NORTH);
        
        JPanel grid = new JPanel(new GridLayout(0, 4, 24, 24));
        grid.setOpaque(false);

        List<User> children = allUsers.stream().filter(u -> u.getUserType() == 0).toList();
        List<User> parents  = allUsers.stream().filter(u -> u.getUserType() == 1).toList();

        for (User u : parents) {
            grid.add(new UserCard(u, "Parent", () -> openUserEditDialog(u)));
        }
        for (User u : children) {
            grid.add(new UserCard(u, "Child", () -> openUserEditDialog(u)));
        }
        
        int remainder = allUsers.size() % 4;
        if (remainder != 0) {
            for (int i = 0; i < (4 - remainder); i++) {
                JPanel b = new JPanel(); b.setOpaque(false); grid.add(b);
            }
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder()); 
        scroll.getVerticalScrollBar().setUnitIncrement(16);  

        contentArea.add(scroll, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void openUserEditDialog(User u) {
        new UserEditDialog(this, u, dbManager, () -> {
            allUsers = dbManager.getAllUsers();
            refreshAccountGrid();
        }).setVisible(true);
    }

    private void refreshAnalytics() {
        contentArea.removeAll();
        contentArea.setLayout(new BorderLayout(0, 24));
        contentArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Family Analytics", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font(WidgetFactory.FONT, Font.BOLD, 22));
        contentArea.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, 3, 20, 20));
        cards.setOpaque(false);

        int total   = allMovies.size();
        int watched = dbManager.countWatched();
        double avg  = dbManager.averageRating();
        String topG = dbManager.topGenre();

        long restricted = allMovies.stream().filter(Movie::isParentalRestriction).count();
        long unwatched  = (long) total - watched;

        cards.add(WidgetFactory.createStatCard("Total Movies",      String.valueOf(total)));
        cards.add(WidgetFactory.createStatCard("Watched",           watched + " / " + total));
        cards.add(WidgetFactory.createStatCard("Not Watched",       String.valueOf(unwatched)));
        cards.add(WidgetFactory.createStatCard("Avg User Rating",   avg > 0 ? String.format("%.1f / 5", avg) : "—"));
        cards.add(WidgetFactory.createStatCard("Top Genre",         topG));
        cards.add(WidgetFactory.createStatCard("Restricted Movies", String.valueOf(restricted)));

        contentArea.add(cards, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

}
