# MovieCritics - Software System Analysis Project

This project is a desktop Java application designed to help family members keep track of movies they have watched or wish to watch, either individually or together.

## 🛠 Getting Started (Database Setup)

For the application to run successfully, the `db.db` SQLite database file must be located in the root directory of the project. If the database file is missing or you need to initialize the tables from scratch, please follow the instructions below.

### Prerequisites
- **SQLite3** must be installed and accessible via your system's command line.

### Step 1: Initialize the Database
Open your terminal (or PowerShell), navigate to the project directory, and execute the following command:

```bash
sqlite3 db.db < schema.sql
New-Item db.db -ItemType File
& "C:\Program Files\DB Browser for SQLite\DB Browser for SQLite.exe" -q -s .\src\main\resources\schema.sql db.db
```

This single command will automatically:

- Create a new db.db file.

- Build all the required tables (movie, person, user, watchlist, and user_rating).

- Seed the database with the mandatory sample data (at least 5 entries per table) for testing purposes.


**Step 2: Verify the Installation (Optional)**

To ensure the tables and data were created successfully, you can inspect the database manually:

```bash
sqlite3 db.db
sqlite> .tables
sqlite> SELECT * FROM movie;
sqlite> .exit
```

## 📂 Project Structure & Guidelines
- **Database File:** The application relies on the db.db file at runtime. As per the submission guidelines, this file must be placed directly in the main folder alongside the Java source codes (no subfolders).

- **User Types & Roles:**

  - Type-1 (Parents/Adults): Have admin-level privileges. They can add/remove movies, edit movie details, manage user accounts, and view family - - analytics.

  - Type-2 (Children): Have member-level privileges. They can log watched movies, rate and comment on movies, and manage personal watchlists.

- **Parental Controls:** Child accounts are strictly restricted from viewing or interacting with any movie where the ParentalRestriction attribute is set to True (1).

**Developer:** Umutcan Ekinci

**University:** Yaşar University

**Course:** SE 2232 - Software System Analysis

**Instructor:** Asst. Prof. Dr. Deniz Özsoyeller