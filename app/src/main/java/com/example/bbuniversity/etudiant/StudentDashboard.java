package com.example.bbuniversity.etudiant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.EmailSender;
import com.example.bbuniversity.R;
import com.example.bbuniversity.TimetableViewActivity;
import com.example.bbuniversity.adapters.AbsenceAdapter;
import com.example.bbuniversity.adapters.StudentNoteAdapter;
import com.example.bbuniversity.models.Abscence;
import com.example.bbuniversity.models.Complaint;
import com.example.bbuniversity.models.Note;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;

public class StudentDashboard extends AppCompatActivity {

    private TextView tvName, tvFiliere, tvMatricule, tvClasse,
            absencesCountText, tvOverallGrade, tvViewAllGrades;
    private RecyclerView absencesRecyclerView, notesRecyclerView;
    private String studentClassName = "";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<Abscence> absenceList = new ArrayList<>();
    private final List<Note> noteList     = new ArrayList<>();
    private AbsenceAdapter adapter;
    private StudentNoteAdapter noteAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
        EdgeToEdge.enable(this);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // UI bindings
        tvName            = findViewById(R.id.FullStdName);
        tvFiliere         = findViewById(R.id.stdFiliere);
        tvMatricule       = findViewById(R.id.stdMatricule);
        tvClasse          = findViewById(R.id.stdClasse);
        absencesCountText = findViewById(R.id.absences_count_text);
        absencesRecyclerView = findViewById(R.id.absences_recycler_view);
        notesRecyclerView    = findViewById(R.id.notes_recycler_view);
        tvOverallGrade       = findViewById(R.id.tvOverallGrade);
        tvViewAllGrades      = findViewById(R.id.tvViewAllGrades);
        Button btnViewGrades = findViewById(R.id.btnViewGrades);
        Button btnLogout     = findViewById(R.id.btnLogout);
        Button btnViewAbsences = findViewById(R.id.btnViewAbsences);
        Button btnTimetable  = findViewById(R.id.btnTimetable);

        btnTimetable.setOnClickListener(v -> {
            Intent i = new Intent(this, TimetableViewActivity.class);
//            i.putExtra("class", tvClasse.getText().toString());
            i.putExtra("class", studentClassName);
            startActivity(i);
        });

        //absences
        btnViewAbsences.setOnClickListener(v -> startActivity(new Intent(this, StudentAbsencesActivity.class)));

        // Logout
        btnLogout.setOnClickListener(v -> logout());

        // Absences list
        adapter = new AbsenceAdapter(absenceList);
        absencesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        absencesRecyclerView.setAdapter(adapter);

        // Notes list with complaint dialog
        noteAdapter = new StudentNoteAdapter(noteList, this::showComplaintDialog);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(noteAdapter);

        // Navigate to full grades
        View.OnClickListener openAll = v ->
                startActivity(new Intent(this, StudentGradesActivity.class));
        tvViewAllGrades.setOnClickListener(openAll);
        btnViewGrades.setOnClickListener(openAll);

        // Load data
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            loadUserData(uid);
            loadAbsences(uid);
            loadAverageGrade(uid);
            loadNotes(uid);
        } else {
            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
        }
    }

    // … loadNotes, loadAbsences, loadAverageGrade, loadUserData, updateUI, logout() stay unchanged …

    /** Show dialog to enter complaint message */
    private void showComplaintDialog(Note note) {
        final android.widget.EditText input = new android.widget.EditText(this);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Réclamation")
                .setMessage("Expliquez votre réclamation")
                .setView(input)
                .setPositiveButton("Envoyer", (d, w) -> {
                    String message = input.getText().toString().trim();
                    sendComplaint(note, message);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Build a Complaint object and save it in the root-level 'complaints' collection */
    private void sendComplaint(Note note, String description) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Complaint complaint = new Complaint(
                user.getUid(),              // studentId
                note.getProfesseurId(),     // teacherId
                note.getMatiere(),        // subjectId
                note.getDocumentPath(),     // noteId
                note.getNoteGenerale(),            // initialGrade (double)
                note.getNoteGenerale(),            // modifiedGrade (double)
                "Réclamation sur la note",  // title
                description,                // description
                "",                         // response (empty until teacher acts)
                "pending",                  // status
                Timestamp.now(),            // dateFiled
                null                        // dateProcessed
        );

        db.collection("complaints")
                .add(complaint)
                .addOnSuccessListener(r ->
                        Toast.makeText(this, "Réclamation envoyée", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show()
                );

        // Optional: still notifying teacher by email
        notifyTeacher(note.getProfesseurId(), description);
    }

    /** Email-notify the teacher off the main thread */
    private void notifyTeacher(String profId, String message) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String studentName = currentUser != null ? currentUser.getDisplayName() : "Étudiant";
        db.collection("users").document(profId)
                .get()
                .addOnSuccessListener(doc -> {
                    String email = doc.getString("email");
                    if (email == null || email.isEmpty()) return;

                    // Send email in a background thread
                    new Thread(() -> {
                        try {
                            EmailSender.sendEmail(
                                    email,
                                    "Nouvelle réclamation reçue",
                                    "Bonjour,\nL'étudiant " + studentName +
                                            " a envoyé une réclamation :\n\n" + message
                            );
                        } catch (MessagingException e) {
                            Log.e("Email", "failed to send", e);
                        }
                    }).start();
                })
                .addOnFailureListener(e ->
                        Log.e("notifyTeacher", "Impossible de récupérer l'email du prof", e)
                );
    }

    // … other existing methods (loadAverageGrade, loadUserData, etc.) …
    /** Charge toutes les notes de l'étudiant */
    private void loadNotes(String uid) {
        db.collection("users").document(uid).collection("notes")
                .get()
                .addOnSuccessListener(query -> {
                    noteList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Note n = doc.toObject(Note.class);
                        if (n != null) {
                            n.setDocumentPath(doc.getReference().getPath());
                            noteList.add(n);
                        }
                    }
                    noteAdapter.updateNotes(noteList);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Erreur chargement notes : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }
    /** Calcule et affiche la moyenne générale */
    private void loadAverageGrade(String uid) {
        db.collection("users").document(uid).collection("notes")
                .get()
                .addOnSuccessListener(query -> {
                    double total = 0;
                    int count = 0;
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Double grade = doc.getDouble("noteGenerale");
                        if (grade != null) {
                            total += grade;
                            count++;
                        }
                    }
                    if (count > 0) {
                        double avg = total / count;
                        tvOverallGrade.setText(String.format(Locale.FRANCE, "%.2f/20", avg));
                    } else {
                        tvOverallGrade.setText("--/20");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Erreur chargement moyenne : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    /** Charge les données de l'utilisateur et met à jour l'UI */
    private void loadUserData(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(this::updateUI)
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Erreur chargement utilisateur : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    /** Charge et compte les absences */
    private void loadAbsences(String uid) {
        db.collection("users").document(uid).collection("abscence")
                .get()
                .addOnSuccessListener(query -> {
                    absencesCountText.setText("Nombre d'absences : " + query.size());
                    absenceList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Abscence a = doc.toObject(Abscence.class);
                        if (a != null) {
                            a.setDocumentId(doc.getId());
                            absenceList.add(a);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Erreur chargement absences : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    /** Met à jour les TextViews avec les données user */
    private void updateUI(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this,
                    "Données utilisateur non trouvées.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String nom      = getSafe(doc, "nom");
        String prenom   = getSafe(doc, "prenom");
        String filiere  = getSafe(doc, "filiere");
        String matricule= getSafe(doc, "matricule");
        String niveau   = getSafe(doc, "niveau");
        String classe   = getSafe(doc, "classe");

        // Retenir le nom réel de la classe pour charger l'emploi du temps
        studentClassName = classe;

        tvName    .setText(nom + " " + prenom);
        tvFiliere .setText(filiere);
        tvMatricule.setText(matricule);
        tvClasse  .setText(niveau + " " + filiere + " " + classe);
    }

    /** Récupère un champ de DocumentSnapshot en sûr */
    private String getSafe(DocumentSnapshot doc, String key) {
        Object val = doc.get(key);
        return (val != null) ? val.toString() : "";
    }

    /** Déconnecte l'utilisateur et retourne à l'écran de login */
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, StudentActivity.class));
        finish();
    }

}
