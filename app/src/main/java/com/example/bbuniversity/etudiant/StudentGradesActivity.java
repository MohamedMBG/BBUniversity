package com.example.bbuniversity.etudiant;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.StudentNoteAdapter;
import com.example.bbuniversity.models.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activité affichant toutes les notes de l'étudiant.
 */
public class StudentGradesActivity extends AppCompatActivity {

    private RecyclerView recyclerGrades;                      // liste des notes
    private StudentNoteAdapter adapter;                       // adaptateur des notes
    private final List<Note> notes = new ArrayList<>();       // données
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_grades);     // affichage du layout
        EdgeToEdge.enable(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        // initialisation de la liste
        recyclerGrades = findViewById(R.id.recyclerGrades);
        adapter = new StudentNoteAdapter(notes, this::showComplaintDialog);
        recyclerGrades.setLayoutManager(new LinearLayoutManager(this));
        recyclerGrades.setAdapter(adapter);

        // chargement des notes si l'utilisateur est connecté
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadNotes(user.getUid());
        } else {
            Toast.makeText(this, "Utilisateur non connecté." , Toast.LENGTH_SHORT).show();
        }
    }

    /** Charge toutes les notes de l'étudiant */
    private void loadNotes(String uid) {
        db.collection("users").document(uid).collection("notes").get()
                .addOnSuccessListener(query -> {
                    notes.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Note n = doc.toObject(Note.class);           // conversion
                        if (n != null) {
                            n.setDocumentPath(doc.getReference().getPath());
                            notes.add(n);                            // ajout à la liste
                        }
                    }
                    adapter.updateNotes(notes);                      // rafraîchissement
                })
                .addOnFailureListener(e -> Toast.makeText(this , "Erreur de chargement des notes : " + e.getMessage() , Toast.LENGTH_SHORT).show());
    }

    /** Affiche une boîte de dialogue pour envoyer une plainte */
    private void showComplaintDialog(Note note) {
        final android.widget.EditText input = new android.widget.EditText(this);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Plainte")
                .setMessage("Expliquez votre reclamation")
                .setView(input)
                .setPositiveButton("Envoyer", (d, w) -> {
                    String message = input.getText().toString().trim();
                    sendComplaint(note, message);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Envoie la plainte dans Firestore */
    private void sendComplaint(Note note, String message) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("notePath", note.getDocumentPath());
        data.put("professeurId", note.getProfesseurId());
        data.put("message", message);
        data.put("timestamp", com.google.firebase.Timestamp.now());
        data.put("status", "pending");

        db.collection("users").document(user.getUid())
                .collection("plaintes")
                .add(data)
                .addOnSuccessListener(r -> Toast.makeText(this , "Plainte envoyée" , Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this , "Erreur : " + e.getMessage() , Toast.LENGTH_SHORT).show());

        notifyTeacher(note.getProfesseurId(), message);
    }

    private void notifyTeacher(String profId, String message) {
        FirebaseFirestore.getInstance().collection("users").document(profId)
                .get()
                .addOnSuccessListener(doc -> {
                    String email = doc.getString("email");
                    if (email != null) {
                        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
                        intent.setData(android.net.Uri.parse("mailto:" + email));
                        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "New grade complaint");
                        intent.putExtra(android.content.Intent.EXTRA_TEXT, message);
                        try {
                            startActivity(intent);
                        } catch (android.content.ActivityNotFoundException ignored) {
                        }
                    }
                });
    }
}