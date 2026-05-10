package org.example;

import org.example.data.Movie;
import org.example.data.Person;
import org.example.data.User;
import org.example.data.UserRating;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL         = "jdbc:sqlite:db.db";
    private static final String COL_TITLE   = "title";
    private static final String COL_GENRE   = "genre";
    private static final String COL_DIR     = "director_id";

    public void checkDatabaseExists() throws SQLException {
        File dbFile = new File("db.db");
        if (!dbFile.exists()) {
            throw new SQLException("Connection failed! Database file 'db.db' not found. Please ensure the database is set up correctly using the provided schema.sql. See: README.md for setup instructions.");
        }
        
        String checkTableSql = "SELECT name FROM sqlite_master WHERE type='table' AND name='user';";
        try (Connection con = connect(); 
            java.sql.PreparedStatement ps = con.prepareStatement(checkTableSql);
            ResultSet rs = ps.executeQuery()) {
            
            if (!rs.next()) { 
                throw new SQLException("Database file 'db.db' exists but is not properly initialized. The required tables are missing. Please set up the database using the provided schema.sql.");
            }
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    
    public List<Movie> getAllMovies() {
        return queryMovies("SELECT * FROM movie", ps -> {});
    }

    public List<Movie> getUnrestrictedMovies() {
        return queryMovies("SELECT * FROM movie WHERE parental_restriction = 0", ps -> {});
    }

    public List<Movie> filterMovies(String title, String genre, String director, String yearStr, boolean unrestrictedOnly) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (unrestrictedOnly) conditions.add("parental_restriction = 0");
        addLikeFilter(conditions, params, COL_TITLE, title);
        addLikeFilter(conditions, params, COL_GENRE, genre);
        addLikeFilter(conditions, params, COL_DIR,   director);
        addYearFilter(conditions, params, yearStr);
        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        return queryMovies("SELECT * FROM movie" + where, ps -> bindParams(ps, params));
    }

    private static void addLikeFilter(List<String> conditions, List<Object> params, String column, String value) {
        if (value != null && !value.isBlank()) {
            conditions.add("LOWER(" + column + ") LIKE ?");
            params.add("%" + value.toLowerCase() + "%");
        }
    }

    private static void addYearFilter(List<String> conditions, List<Object> params, String yearStr) {
        if (yearStr == null || yearStr.isBlank()) return;
        try {
            conditions.add("release_year = ?");
            params.add(Integer.parseInt(yearStr.trim()));
        } catch (NumberFormatException _) {
            // non-numeric year input Ã¢â‚¬â€ skip filter
        }
    }

    private static void bindParams(PreparedStatement ps, List<Object> params) {
        try {
            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                if (v instanceof Integer iv) ps.setInt(i + 1, iv);
                else ps.setString(i + 1, (String) v);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Movie> getWatchlistMovies(int userId) {
        String sql = "SELECT m.* FROM movie m JOIN watchlist w ON m.movie_id = w.movie_id WHERE w.user_id = ?";
        return queryMovies(sql, ps -> {
            try { ps.setInt(1, userId); } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    private interface PsSetter { void set(PreparedStatement ps); }

    private List<Movie> queryMovies(String sql, PsSetter setter) {
        List<Movie> list = new ArrayList<>();
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            setter.set(ps);
            ResultSet r = ps.executeQuery();
            while (r.next()) list.add(mapMovie(r));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private Movie mapMovie(ResultSet r) throws SQLException {
        return new Movie(
            r.getInt("movie_id"), r.getString(COL_TITLE), r.getInt("release_year"),
            r.getString("language"), r.getString("country_of_origin"), r.getString(COL_GENRE),
            r.getString(COL_DIR), r.getBoolean("is_watched"),
            r.getString("leading_actor_id"), r.getString("supporting_actor_id"),
            r.getString("about"), r.getInt("rating"), r.getString("comments"),
            r.getString("poster"), r.getBoolean("parental_restriction"));
    }

    public void addMovie(Movie movie) throws SQLException {
        String sql = "INSERT INTO movie(title,release_year,language,country_of_origin,genre,director_id,is_watched,leading_actor_id,supporting_actor_id,about,rating,comments,poster,parental_restriction) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            setMovieParams(ps, movie); ps.executeUpdate();
        }
    }

    public void updateMovie(Movie movie) throws SQLException {
        String sql = "UPDATE movie SET title=?,release_year=?,language=?,country_of_origin=?,genre=?,director_id=?,is_watched=?,leading_actor_id=?,supporting_actor_id=?,about=?,rating=?,comments=?,poster=?,parental_restriction=? WHERE movie_id=?";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            setMovieParams(ps, movie); ps.setInt(15, movie.getId()); ps.executeUpdate();
        }
    }

    public void deleteMovie(int id) throws SQLException {
        execute("DELETE FROM movie WHERE movie_id = ?", ps -> ps.setInt(1, id));
    }

    private void setMovieParams(PreparedStatement ps, Movie m) throws SQLException {
        ps.setString(1, m.getTitle());      ps.setInt(2, m.getReleaseYear());
        ps.setString(3, m.getLanguage());   ps.setString(4, m.getOriginCountry());
        ps.setString(5, m.getGenre());      ps.setString(6, m.getDirectorId());
        ps.setBoolean(7, m.isWatched());    ps.setString(8, m.getLeadingActorId());
        ps.setString(9, m.getSupportingActorId()); ps.setString(10, m.getAbout());
        ps.setInt(11, m.getRating());       ps.setString(12, m.getComments());
        ps.setString(13, m.getPoster());    ps.setBoolean(14, m.isParentalRestriction());
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try (Connection con = connect(); Statement stmt = con.createStatement();
             ResultSet r = stmt.executeQuery("SELECT * FROM user")) {
            while (r.next()) list.add(mapUser(r));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public User getUserByUsernameOrEmail(String value) {
        String sql = "SELECT * FROM user WHERE username = ? OR email = ?";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, value); ps.setString(2, value);
            ResultSet r = ps.executeQuery();
            if (r.next()) return mapUser(r);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO user(username,password,user_type,email) VALUES(?,?,?,?)";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getUsername()); ps.setString(2, user.getPassword());
            ps.setInt(3, user.getUserType());    ps.setString(4, user.getEmail());
            ps.executeUpdate();
        }
    }

    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE user SET username=?,password=?,user_type=?,email=? WHERE user_id=?";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getUsername()); ps.setString(2, user.getPassword());
            ps.setInt(3, user.getUserType());    ps.setString(4, user.getEmail());
            ps.setInt(5, user.getId());          ps.executeUpdate();
        }
    }

    public void deleteUser(int id) throws SQLException {
        execute("DELETE FROM user WHERE user_id = ?", ps -> ps.setInt(1, id));
    }

    private User mapUser(ResultSet r) throws SQLException {
        return new User(r.getInt("user_id"), r.getString("username"),
            r.getString("password"), r.getInt("user_type"), r.getString("email"));
    }

    public List<Person> getAllPersons() {
        List<Person> list = new ArrayList<>();
        try (Connection con = connect(); Statement stmt = con.createStatement();
             ResultSet r = stmt.executeQuery("SELECT * FROM person")) {
            while (r.next()) list.add(new Person(r.getInt("person_id"),
                r.getString("first_name"), r.getString("last_name"),
                r.getDate("date_of_birth").toString(), r.getString("nationality")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void addPerson(Person p) {
        String sql = "INSERT INTO person(first_name,last_name,date_of_birth,nationality) VALUES(?,?,?,?)";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getFirstName()); ps.setString(2, p.getLastName());
            ps.setDate(3, Date.valueOf(p.getDateOfBirth())); ps.setString(4, p.getNationality());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void deletePerson(int id) throws SQLException {
        execute("DELETE FROM person WHERE person_id = ?", ps -> ps.setInt(1, id));
    }

    public void addToWatchlist(int userId, int movieId) throws SQLException {
        String sql = "INSERT INTO watchlist(user_id,movie_id) VALUES(?,?)";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            ps.executeUpdate();
        }
    }

    public void removeFromWatchlist(int userId, int movieId) throws SQLException {
        execute("DELETE FROM watchlist WHERE user_id=? AND movie_id=?",
            ps -> { ps.setInt(1, userId); ps.setInt(2, movieId); });
    }

    public boolean isInWatchlist(int userId, int movieId) {
        String sql = "SELECT COUNT(*) FROM watchlist WHERE user_id=? AND movie_id=?";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, movieId);
            ResultSet r = ps.executeQuery();
            return r.next() && r.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public void addOrUpdateRating(int userId, int movieId, int rating, String comment) throws SQLException {
        String sql = "INSERT INTO user_rating(user_id,movie_id,rating,comment) VALUES(?,?,?,?) ON CONFLICT(user_id,movie_id) DO UPDATE SET rating=excluded.rating, comment=excluded.comment";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, movieId);
            ps.setInt(3, rating); ps.setString(4, comment);
            ps.executeUpdate();
        }
    }

    public UserRating getUserRating(int userId, int movieId) {
        String sql = "SELECT ur.*, u.username FROM user_rating ur JOIN user u ON ur.user_id=u.user_id WHERE ur.user_id=? AND ur.movie_id=?";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId); ps.setInt(2, movieId);
            ResultSet r = ps.executeQuery();
            if (r.next()) return mapRating(r);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<UserRating> getRatingsForMovie(int movieId) {
        List<UserRating> list = new ArrayList<>();
        String sql = "SELECT ur.*, u.username FROM user_rating ur JOIN user u ON ur.user_id=u.user_id WHERE ur.movie_id=?";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ResultSet r = ps.executeQuery();
            while (r.next()) list.add(mapRating(r));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void deleteRating(int ratingId) throws SQLException {
        execute("DELETE FROM user_rating WHERE id = ?", ps -> ps.setInt(1, ratingId));
    }

    private UserRating mapRating(ResultSet r) throws SQLException {
        return new UserRating(r.getInt("id"), r.getInt("user_id"), r.getInt("movie_id"),
            r.getInt("rating"), r.getString("comment"), r.getString("username"));
    }

    public int countWatched() {
        return queryInt("SELECT COUNT(*) FROM movie WHERE is_watched = 1");
    }

    public double averageRating() {
        return queryDouble("SELECT AVG(rating) FROM user_rating");
    }

    public String topGenre() {
        String sql = "SELECT genre, COUNT(*) c FROM movie GROUP BY genre ORDER BY c DESC LIMIT 1";
        try (Connection con = connect(); Statement stmt = con.createStatement();
             ResultSet r = stmt.executeQuery(sql)) {
            if (r.next()) return r.getString(COL_GENRE);
        } catch (SQLException e) { e.printStackTrace(); }
        return "Ã¢â‚¬â€";
    }

    public int countRatingsByUser(int userId) {
        return queryInt("SELECT COUNT(*) FROM user_rating WHERE user_id = " + userId);
    }

    private interface ThrowingPsSetter { void set(PreparedStatement ps) throws SQLException; }

    private void execute(String sql, ThrowingPsSetter setter) throws SQLException {
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            setter.set(ps); ps.executeUpdate();
        }
    }

    private int queryInt(String sql) {
        try (Connection con = connect(); Statement stmt = con.createStatement();
             ResultSet r = stmt.executeQuery(sql)) {
            if (r.next()) return r.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private double queryDouble(String sql) {
        try (Connection con = connect(); Statement stmt = con.createStatement();
             ResultSet r = stmt.executeQuery(sql)) {
            if (r.next()) return r.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }
}
