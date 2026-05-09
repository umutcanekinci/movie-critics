# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Compile
mvn compile

# Package into fat jar (if exec plugin added)
mvn package

# Run from IDE: use the "Main" launch config in .vscode/launch.json
# Entry point: org.example.Main  →  Main.main()
```

Java 26 is required (`maven.compiler.source=26`). The only dependency is `sqlite-jdbc 3.47.1.0`.

There are no tests in this project.

The app is launched from `LoginFrame` (not `Main.main()` directly — `main()` has package-private visibility, so IntelliJ/VS Code run `LoginFrame` directly via launch configs).

## Architecture

### Application flow

```
LoginFrame  →  (user_type == 1)  →  Type1MainFrame  (Parent / Admin)
            →  (user_type == 0)  →  Type2MainFrame  (Child)
```

`LoginFrame` checks whether any users exist: if the DB is empty it shows "Register", otherwise "Sign In". A logo slide-in animation runs on startup; on confirm it runs the reverse animation before opening the main frame.

### User types

| `user_type` | Role | Frame | Capabilities |
|---|---|---|---|
| `1` | Parent/Admin | `Type1MainFrame` | Full CRUD on movies/users, Analytics tab |
| `0` | Child | `Type2MainFrame` | Browse unrestricted movies, watchlist, rate/comment, progress stats |

Registration always creates `user_type=2` (but `Type2MainFrame` handles both 0 and 2 — the "else" branch in `openMainFrame`).

### Database

- SQLite file `db.db` written to the **working directory** at runtime (not inside `src/`).
- Schema is loaded from classpath `/schema.sql` inside `DatabaseManager()` constructor using a **two-pass** approach: all `CREATE TABLE IF NOT EXISTS` statements are batched first, then each `INSERT` runs only if `COUNT(*) == 0` for that table. This prevents re-seeding on every startup.
- `director_id`, `leading_actor_id`, `supporting_actor_id` on `movie` are stored as `VARCHAR` containing the `person_id` value as a string — they are not enforced FK relationships.
- Upsert for user ratings uses SQLite `ON CONFLICT DO UPDATE`.

### Package structure

| Package | Contents |
|---|---|
| `org.example` | `Main`, `DatabaseManager` |
| `org.example.frame` | `LoginFrame`, `Type1MainFrame`, `Type2MainFrame` |
| `org.example.dialog` | `MovieEditDialog`, `UserEditDialog` (null entity = add mode) |
| `org.example.widget` | `MovieCard`, `UserCard`, `FloatingLabelField`, `LogoAnimator` |
| `org.example.data` | `Movie`, `User`, `Person`, `UserRating` |

### UI conventions

- All frames are `setUndecorated(true)` + `MAXIMIZED_BOTH`. ESC closes/logs out.
- Dark theme throughout: background `new Color(20, 20, 20)`, cards `new Color(25, 25, 25)`, purple accent `new Color(140, 82, 255)`, font `"Segoe UI"`.
- `Type1MainFrame` and `Type2MainFrame` follow the same layout pattern: fixed header with logo + nav tabs, left/right arrow buttons for pagination, a central `contentArea` panel that is cleared and rebuilt on each tab switch.
- `MovieCard` and `UserCard` are custom `JPanel` subclasses that use `paintComponent` directly (no child components).
- `FloatingLabelField` is a composite `JPanel` (label + `JTextField`) that animates the label to a smaller floating position on focus.

### Edit/Add dialogs

`MovieEditDialog` and `UserEditDialog` both accept a nullable entity:
- `null` → "Add" mode (empty form, calls `dbManager.addX()`)
- non-null → "Edit" mode (pre-filled form, calls `dbManager.updateX()`, shows red Delete button on the left)

### Seed credentials

| Username | Password | Type |
|---|---|---|
| `admin` | `admin123` | Parent (1) |
| `parent1` | `pass123` | Parent (1) |
| `child1` | `pass123` | Child (0) |
