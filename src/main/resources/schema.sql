CREATE TABLE IF NOT EXISTS movie (
    movie_id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(255) NOT NULL,
    release_year INTEGER NOT NULL,
    language VARCHAR(100) NOT NULL,
    country_of_origin VARCHAR(100),
    genre VARCHAR(100),
    director_id VARCHAR(255),
    is_watched BOOLEAN,
    leading_actor_id VARCHAR(255),
    supporting_actor_id VARCHAR(255),
    about TEXT,
    rating INT,
    comments TEXT,
    poster VARCHAR(255),
    parental_restriction BOOLEAN
);

CREATE TABLE IF NOT EXISTS person (
    person_id INTEGER PRIMARY KEY AUTOINCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    nationality VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS user (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    user_type INT NOT NULL,
    email VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS watchlist (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    UNIQUE(user_id, movie_id)
);

CREATE TABLE IF NOT EXISTS user_rating (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    UNIQUE(user_id, movie_id)
);

CREATE TRIGGER IF NOT EXISTS trg_prevent_restricted_movie_watchlist
    BEFORE INSERT ON watchlist
    WHEN (SELECT user_type FROM user WHERE user_id = NEW.user_id) = 0
        AND (SELECT parental_restriction FROM movie WHERE movie_id = NEW.movie_id) = 1
BEGIN
    SELECT RAISE(ABORT, 'Parental restriction: this movie cannot be added to a child account watchlist.');
END;

CREATE TRIGGER IF NOT EXISTS trg_remove_from_watchlist_after_rating
    AFTER INSERT ON user_rating
BEGIN
    DELETE FROM watchlist
    WHERE user_id = NEW.user_id AND movie_id = NEW.movie_id;
END;

CREATE TRIGGER IF NOT EXISTS trg_update_movie_rating
    AFTER INSERT ON user_rating
BEGIN
    UPDATE movie
    SET rating = (
        SELECT CAST(ROUND(AVG(rating)) AS INT)
        FROM user_rating
        WHERE movie_id = NEW.movie_id
    )
    WHERE movie_id = NEW.movie_id;
END;

CREATE TRIGGER IF NOT EXISTS trg_update_movie_rating_after_update
    AFTER UPDATE OF rating ON user_rating
BEGIN
    UPDATE movie
    SET rating = (
        SELECT CAST(ROUND(AVG(rating)) AS INT)
        FROM user_rating
        WHERE movie_id = NEW.movie_id
    )
    WHERE movie_id = NEW.movie_id;
END;

CREATE TRIGGER IF NOT EXISTS trg_update_movie_rating_after_delete
    AFTER DELETE ON user_rating
BEGIN
    UPDATE movie
    SET rating = (
        SELECT COALESCE(CAST(ROUND(AVG(rating)) AS INT), 0)
        FROM user_rating
        WHERE movie_id = OLD.movie_id
    )
    WHERE movie_id = OLD.movie_id;
END;


-- SAMPLE DATA
INSERT INTO person (first_name, last_name, date_of_birth, nationality) VALUES
('Christopher', 'Nolan', '1970-07-30', 'British-American'),
('Leonardo', 'DiCaprio', '1974-11-11', 'American'),
('Joseph', 'Gordon-Levitt', '1981-02-17', 'American'),
('Heath', 'Ledger', '1979-04-04', 'Australian'),
('Christian', 'Bale', '1974-01-30', 'British-American');

INSERT INTO movie (title, release_year, language, country_of_origin, genre, director_id, is_watched, leading_actor_id, supporting_actor_id, about, rating, comments, poster, parental_restriction) VALUES
('Inception', 2010, 'English', 'USA', 'Sci-Fi', '1', 1, '2', '3', 'A thief who steals corporate secrets through dream-sharing technology is given the task of planting an idea.', 9, 'Brilliant and complex.', 'C:\Users\user\Desktop\projects\movie-critics\src\main\resources\inception.png', 0),
('The Dark Knight', 2008, 'English', 'USA', 'Action', '1', 1, '5', '4', 'When the Joker emerges to wreak havoc on Gotham, Batman must confront his greatest challenge.', 10, 'Greatest superhero film ever made.', 'C:\Users\user\Desktop\projects\movie-critics\src\main\resources\the_dark_knight.png', 0),
('The Prestige', 2006, 'English', 'USA', 'Drama', '1', 0, '5', '2', 'Two rival magicians engage in a dangerous battle to create the ultimate illusion.', 9, 'Captivating from start to finish.', 'C:\Users\user\Desktop\projects\movie-critics\src\main\resources\the_prestige.png', 0),
('Interstellar', 2014, 'English', 'USA', 'Sci-Fi', '1', 1, '2', '3', 'A team of explorers travel through a wormhole in search of a new home for humanity.', 9, 'Emotionally stunning and scientifically fascinating.', 'C:\Users\user\Desktop\projects\movie-critics\src\main\resources\interstellar.png', 0),
('Toy Story', 1995, 'English', 'USA', 'Animation', '1', 1, '2', '3', 'A cowboy toy is threatened by the arrival of a new spaceman toy.', 8, 'A timeless classic for all ages.', 'C:\Users\user\Desktop\projects\movie-critics\src\main\resources\toy_story.png', 0),
('The Wolf of Wall Street', 2013, 'English', 'USA', 'Biography', '1', 0, '2', '3', 'The rise and fall of stockbroker Jordan Belfort.', 7, 'Intense but very adult.', 'C:\Users\user\Desktop\projects\movie-critics\src\main\resources\the_wolf_of_wall_street.png', 1),
('Pulp Fiction', 1994, 'English', 'USA', 'Crime', '1', 1, '2', '3', 'Interconnected stories of crime in Los Angeles.', 9, 'Iconic dialogue and storytelling.', 'C:\Users\user\Desktop\projects\movie-critics\src\main\resources\pulp_fiction.png', 1);

INSERT OR IGNORE INTO user (username, password, user_type, email) VALUES
('a', 'a', 1, 'admin@moviecritics.com'),
('admin', 'admin123', 1, 'admin@moviecritics.com'),
('parent1', 'pass123', 1, 'parent1@example.com'),
('child1', 'pass123', 0, 'child1@example.com'),
('child2', 'pass123', 0, 'child2@example.com'),
('child3', 'pass123', 0, 'child3@example.com');

INSERT OR IGNORE INTO watchlist (user_id, movie_id) VALUES
(4, 2),
(4, 4),
(5, 3),
(5, 5),
(6, 1),
(6, 2);

INSERT OR IGNORE INTO user_rating (user_id, movie_id, rating, comment) VALUES
(4, 1, 5, 'Mind-blowing, especially the ending!'),
(5, 1, 4, 'Confusing at first but amazing.'),
(5, 2, 5, 'Heath Ledger was incredible.'),
(6, 5, 5, 'My favorite movie of all time!'),
(3, 1, 5, 'Cinematic masterpiece by Nolan.'),
(3, 3, 4, 'Brilliantly twisted plot.');
