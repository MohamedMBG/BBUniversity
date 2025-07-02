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
import com.example.bbuniversity.models.Complaint;
import com.example.bbuniversity.models.Note;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class StudentGradesActivity extends AppCompatActivity {

    // recycler showing all grade items
    private RecyclerView recyclerGrades;
    // adapter used by the recycler
    private StudentNoteAdapter adapter;
    // list containing the student's notes
    private final List<Note> notes = new ArrayList<>();
    // Firestore access point
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // call super implementation
        super.onCreate(savedInstanceState);
        // inflate the layout containing the list
        setContentView(R.layout.activity_student_grades);
        // allow drawing behind system bars
        EdgeToEdge.enable(this);
        // hide navigation bar
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // locate recycler view in layout
        recyclerGrades = findViewById(R.id.recyclerGrades);
        // create adapter handling complaints
        adapter = new StudentNoteAdapter(notes, this::showComplaintDialog);
        // vertical list configuration
        recyclerGrades.setLayoutManager(new LinearLayoutManager(this));
        // attach adapter
        recyclerGrades.setAdapter(adapter);

        // retrieve current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // load notes when a user exists otherwise show a toast
        if (user != null) loadNotes(user.getUid());
        else Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
    }

    /** Charge toutes les notes de l'étudiant */
    private void loadNotes(String uid) {
        // query user's notes
        db.collection("users").document(uid).collection("notes").get()
                .addOnSuccessListener(query -> {
                    // clear previous list
                    notes.clear();
                    // convert each document
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Note n = doc.toObject(Note.class);
                        if (n != null) {
                            n.setDocumentPath(doc.getReference().getPath());
                            notes.add(n);
                        }
                    }
                    // refresh recycler
                    adapter.updateNotes(notes);
                })
                // display error message on failure
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur de chargement des notes : " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    /** Affiche une boîte de dialogue pour envoyer une plainte */
    private void showComplaintDialog(Note note) {
        final android.widget.EditText input = new android.widget.EditText(this);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Réclamation")
                .setMessage("Expliquez votre réclamation")
                .setView(input)
                .setPositiveButton("Envoyer", (dialog, which) -> {
                    String message = input.getText().toString().trim();
                    sendComplaint(note, message);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
    private void sendComplaint(Note note, String description) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Complaint complaint = new Complaint(
                user.getUid(),                      // studentId
                note.getProfesseurId(),            // teacherId
                note.getMatiere(),                 // subjectId
                note.getDocumentPath(),            // noteId
                note.getNoteGenerale(),            // initialGrade
                note.getNoteGenerale(),            // modifiedGrade
                "Réclamation sur la note",         // title
                description,                       // description
                "",                                // response
                "pending",                         // status
                Timestamp.now(),                   // dateFiled
                null                               // dateProcessed
        );

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("complaints")
                .add(complaint)
                .addOnSuccessListener(r ->
                        Toast.makeText(this, "Réclamation envoyée", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }


}