# Architecture

## Package overview

### `admin_panel`
Contains activities used by administrators for university management tasks:
- `AdminActivity` – authentication for administrators.
- `AdminDashboard` – displays counts of students, teachers and absences and links to create or manage entities.
- `CreateStudentActivity`, `CreateProfessorActivity`, `CreateClassActivity`, `CreateSubjectActivity` – forms for adding data.
- `ManageStudentsActivity`, `ManageProfessorsActivity`, `ManageAbsencesActivity` – lists for updating or removing records.
- `AddNoteActivity`, `AddAbsenceActivity`, `TimetableAdminActivity`, `EditStudentDetailsActivity` – specialized management screens.

### `etudiant`
Student-facing screens:
- `StudentActivity` – student authentication.
- `StudentDashboard` – shows profile details, absences, grades, timetables and allows filing complaints.
- `StudentGradesActivity` and `StudentAbsencesActivity` – detailed lists of grades and absences.

### `teacher`
Teacher-facing screens:
- `TeacherActivity` – teacher authentication.
- `TeacherDashboard` – upcoming classes with navigation to grading, complaints and timetables.
- `TeacherClassesActivity`, `ClassStudentsActivity` – class and student lists.
- `TeacherComplaintsActivity`, `TeacherComplaintDetailActivity` – complaint management.

### `adapters`
RecyclerView adapters shared across the app, including `StudentAdapter`, `TeacherAdapter`, `ClassAdapter`, `NoteAdapter`, `AbsenceAdapter`, `ComplaintAdapter` and more.

### `models`
Firestore data models:
- Base `User` plus `Etudiant` and `Professeur` subclasses.
- Academic models such as `Matiere`, `Classe`, `TimetableEntry`, `Note`, `Abscence`, `ClassInfo` and `Complaint`.

### Utilities
- `MainActivity` – entry point routing users to admin, teacher or student flows.
- `TimetableViewActivity` – displays the timetable for a class or teacher.
- `EmailSender` – helper for sending SMTP emails.
