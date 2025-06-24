package com.example.bbuniversity.etudiant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.AbsenceAdapter;
import com.example.bbuniversity.adapters.StudentNoteAdapter;
import com.example.bbuniversity.models.Abscence;
import com.example.bbuniversity.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentDashboard extends AppCompatActivity {

    private TextView tvName, tvFiliere, tvMatricule, tvClasse, absencesCountText, tvOverallGrade, tvViewAllGrades;
    private RecyclerView absencesRecyclerView, notesRecyclerView;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<Abscence> absenceList = new ArrayList<>();
    private final List<Note> noteList = new ArrayList<>();
    private AbsenceAdapter adapter;
    private StudentNoteAdapter noteAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
        EdgeToEdge.enable(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        //declaration des elements de ui
        tvName = findViewById(R.id.FullStdName);
        tvFiliere = findViewById(R.id.stdFiliere);
        tvMatricule = findViewById(R.id.stdMatricule);
        tvClasse = findViewById(R.id.stdClasse);
        absencesCountText = findViewById(R.id.absences_count_text);
        absencesRecyclerView = findViewById(R.id.absences_recycler_view);
        tvOverallGrade = findViewById(R.id.tvOverallGrade);
        absencesRecyclerView = findViewById(R.id.absences_recycler_view); // liste des absences
        notesRecyclerView = findViewById(R.id.notes_recycler_view); // liste des notes
        tvOverallGrade = findViewById(R.id.tvOverallGrade); // moyenne generale
        tvViewAllGrades = findViewById(R.id.tvViewAllGrades);
        Button btnViewGrades = findViewById(R.id.btnViewGrades);



        adapter = new AbsenceAdapter(absenceList); // adaptateur pour les absences
        absencesRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // disposition verticale
        absencesRecyclerView.setAdapter(adapter); // lien avec la vue

        // adaptateur pour afficher les notes et gerer la reclamation
        noteAdapter = new StudentNoteAdapter(noteList, this::showComplaintDialog);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // orientation verticale
        notesRecyclerView.setAdapter(noteAdapter); // liaison de l'adaptateur

        // navigation vers l'activité listant toutes les notes
        View.OnClickListener openAll = v ->
                startActivity(new Intent(this, StudentGradesActivity.class));
        tvViewAllGrades.setOnClickListener(openAll);
        btnViewGrades.setOnClickListener(openAll);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            loadUserData(uid);
            loadAbsences(uid);
            loadAverageGrade(uid); // calcul de la moyenne
            loadNotes(uid); // chargement des notes detaillees
        } else {
            Toast.makeText(this, "Utilisateur non connecté." , Toast.LENGTH_SHORT).show();
        }
    }

    // Chargement de toutes les notes de l'etudiant
    private void loadNotes(String uid) {
        db.collection("users").document(uid).collection("notes").get()
                .addOnSuccessListener(q -> {
                    noteList.clear(); // on vide la liste courante
                    for (DocumentSnapshot doc : q.getDocuments()) {
                        Note n = doc.toObject(Note.class); // conversion du document
                        if (n != null) {
                            n.setDocumentPath(doc.getReference().getPath()); // garder le chemin pour la plainte
                            noteList.add(n); // ajout a la liste
                        }
                    }
                    noteAdapter.updateNotes(noteList); // mise a jour de l'affichage
                })
                .addOnFailureListener(e -> Toast.makeText(this , "Erreur de chargement des notes : " + e.getMessage() , Toast.LENGTH_SHORT).show());
    }

    // Affichage d'une boite de dialogue pour envoyer une plainte
    private void showComplaintDialog(Note note) {
        // zone de texte pour saisir le message
        final android.widget.EditText input = new android.widget.EditText(this);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Plainte")
                .setMessage("Expliquez votre reclamation")
                .setView(input)
                .setPositiveButton("Envoyer", (d, w) -> {
                    String message = input.getText().toString().trim();
                    sendComplaint(note, message); // envoi vers Firestore
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // Envoi de la plainte dans Firestore
    private void sendComplaint(Note note, String message) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return; // securite

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("notePath", note.getDocumentPath()); // identifiant de la note
        data.put("professeurId", note.getProfesseurId()); // prof concerne
        data.put("message", message); // contenu de la plainte
        data.put("timestamp", com.google.firebase.Timestamp.now()); // date

        db.collection("users").document(user.getUid())
                .collection("plaintes")
                .add(data)
                .addOnSuccessListener(r -> Toast.makeText(this , "Plainte envoyee" , Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this , "Erreur : " + e.getMessage() , Toast.LENGTH_SHORT).show());
    }


    private void loadAverageGrade(String uid) {
        db.collection("users").document(uid).collection("notes").get()
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
                        double average = total / count;
                        tvOverallGrade.setText(String.format(Locale.FRANCE, "%.2f/20", average));
                    } else {
                        tvOverallGrade.setText("--/20");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this , "Erreur de chargement des notes : " + e.getMessage() , Toast.LENGTH_SHORT).show());
    }

    private void loadUserData(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(this::updateUI)
                .addOnFailureListener(e -> Toast.makeText(this , "Erreur de chargement utilisateur : " + e.getMessage() , Toast.LENGTH_SHORT).show());
    }

    private void loadAbsences(String uid) {
        db.collection("users").document(uid).collection("abscence").get()
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
                .addOnFailureListener(e -> Toast.makeText(this , "Erreur de chargement des absences : " + e.getMessage() , Toast.LENGTH_SHORT).show());
    }

    private void updateUI(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Données utilisateur non trouvées." , Toast.LENGTH_SHORT).show();
            return;
        }

        String nom = getSafe(doc, "nom");
        String prenom = getSafe(doc, "prenom");
        String filiere = getSafe(doc, "filiere");
        String matricule = getSafe(doc, "matricule");
        String niveau = getSafe(doc, "niveau");
        String classe = getSafe(doc, "classe");

        tvName.setText(nom + " " + prenom);
        tvFiliere.setText(filiere);
        tvMatricule.setText(matricule);
        tvClasse.setText(niveau + " " + filiere + " " + classe);
    }

    private String getSafe(DocumentSnapshot doc, String key) {
        Object val = doc.get(key);
        return val != null ? val.toString() : "";
    }

}
