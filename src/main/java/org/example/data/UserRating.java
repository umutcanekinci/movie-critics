package org.example.data;

public class UserRating {
    private final int id;
    private final int userId;
    private final int movieId;
    private final int rating;
    private final String comment;
    private final String username;

    public UserRating(int id, int userId, int movieId, int rating, String comment, String username) {
        this.id       = id;
        this.userId   = userId;
        this.movieId  = movieId;
        this.rating   = rating;
        this.comment  = comment;
        this.username = username;
    }

    public int    getId()       { return id; }
    public int    getUserId()   { return userId; }
    public int    getMovieId()  { return movieId; }
    public int    getRating()   { return rating; }
    public String getComment()  { return comment; }
    public String getUsername() { return username; }
}
