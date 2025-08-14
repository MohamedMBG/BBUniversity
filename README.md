# BBUniversity

BBUniversity is an Android application that provides role-based tools for managing university data. Users can log in as administrators, teachers, or students.

## Features
- **Administrator**: manage students, professors, classes, subjects, timetables, absences and grades. The dashboard aggregates counts and shows recent notes.
- **Teacher**: view teaching assignments, record grades, review student complaints and access timetables.
- **Student**: view personal information, absences, grades and timetables, and file complaints about grades.

## Architecture
- `app/src/main/java/com/example/bbuniversity`
  - `admin_panel` – administrative activities for CRUD operations.
  - `etudiant` – student authentication and dashboard screens.
  - `teacher` – teacher dashboard and related screens.
  - `models` – Firestore data models such as students, professors, notes and timetable entries.
  - `adapters` – RecyclerView adapters used across the app.
  - Utility classes like `EmailSender` and `TimetableViewActivity`.
- Layouts, drawables and other Android resources reside under `app/src/main/res`.

More detailed package information is available in [`docs/architecture.md`](docs/architecture.md).

## Build & Run
1. Install Android Studio or use the included Gradle wrapper.
2. Provide a valid `google-services.json` for Firebase services.
3. Build the debug variant:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run unit tests:
   ```bash
   ./gradlew test
   ```

## Testing
Sample unit tests live under `app/src/test`. Execute the full test suite with `./gradlew test`.
