package org.example.data;

public class Movie {
    private int id;
    private String title;
    private int releaseYear;
    private String language;
    private String originCountry;
    private String genre;
    private String directorId;
    private boolean isWatched;
    private String leadingActorId;
    private String supportingActorId;
    private String about;
    private int rating;
    private String comments;
    private String poster;
    private boolean parentalRestriction;
    
    public Movie(int id, String title, int releaseYear, String language, String originCountry, String genre,
            String directorId, boolean isWatched, String leadingActorId, String supportingActorId, String about,
            int rating, String comments, String poster, boolean parentalRestriction) {
        this.id                  = id;
        this.title               = title;
        this.releaseYear         = releaseYear;
        this.language            = language;
        this.originCountry       = originCountry;
        this.genre               = genre;
        this.directorId          = directorId;
        this.isWatched           = isWatched;
        this.leadingActorId      = leadingActorId;
        this.supportingActorId   = supportingActorId;
        this.about               = about;
        this.rating              = rating;
        this.comments            = comments;
        this.poster              = poster;
        this.parentalRestriction = parentalRestriction;
    }

    public int     getId()                  { return id; }
    public String  getTitle()               { return title; }
    public int     getReleaseYear()         { return releaseYear; }
    public String  getLanguage()            { return language; }
    public String  getOriginCountry()       { return originCountry; }
    public String  getGenre()               { return genre; }
    public String  getDirectorId()          { return directorId; }
    public boolean isWatched()              { return isWatched; }
    public String  getLeadingActorId()      { return leadingActorId; }
    public String  getSupportingActorId()   { return supportingActorId; }
    public String  getAbout()               { return about; }
    public int     getRating()              { return rating; }
    public String  getComments()            { return comments; }
    public String  getPoster()              { return poster; }
    public boolean isParentalRestriction()  { return parentalRestriction; }
}
