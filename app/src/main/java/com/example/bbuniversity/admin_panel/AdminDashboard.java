package com.example.bbuniversity.admin_panel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.NoteAdapter;
import com.example.bbuniversity.models.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboard extends AppCompatActivity {

    private FirebaseFirestore db;
    private NoteAdapter noteAdapter;
    private TextView textTotalStudents, textTotalTeachers, textTotalAbsences;
    private CardView manageTeachers,manageStudents;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashboard);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        db = FirebaseFirestore.getInstance();

        setupUI();
        setupButtons();
        loadDashboardData();
    }
    private void loadTotalAbsences() {
        db.collectionGroup("abscence").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        textTotalAbsences.setText(String.valueOf(task.getResult().size()));
                    } else {
                        textTotalAbsences.setText("Error");
                        Log.e("AbsenceCount", "Error", task.getException());
                    }
                });
    }
    private void setupUI() {
        RecyclerView recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
        recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(new ArrayList<>(), db);
        recyclerViewNotes.setAdapter(noteAdapter);

        textTotalStudents = findViewById(R.id.textTotalStudents);
        textTotalTeachers = findViewById(R.id.textTotalTeachers);

        manageTeachers = findViewById(R.id.ManageTeahcers);
        manageStudents = findViewById(R.id.ManageStudents);
        textTotalAbsences = findViewById(R.id.textTotalAbsences);

    }

    private void setupButtons() {
        findViewById(R.id.fabAddStudent).setOnClickListener(v -> launch(CreateStudentActivity.class));
        findViewById(R.id.fabAddTeacher).setOnClickListener(v -> launch(CreateProfessorActivity.class));

        manageStudents.setOnClickListener(view -> launch(ManageStudentsActivity.class));
        manageTeachers.setOnClickListener(view -> launch(ManageProfessorsActivity.class));
        findViewById(R.id.fabAddAbsence).setOnClickListener(v -> launch(AddAbsenceActivity.class));
        findViewById(R.id.btnAdminTimetable).setOnClickListener(v -> launch(TimetableAdminActivity.class));
        findViewById(R.id.btnAddClass).setOnClickListener(v -> launch(CreateClassActivity.class));
        findViewById(R.id.btnAddSubject).setOnClickListener(v -> launch(CreateSubjectActivity.class));

    }

    private void launch(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    private void loadDashboardData() {
        loadNotes();
        loadAbsenceStats();
        loadCount("student", R.id.textTotalStudents);
        loadCount("professor", R.id.textTotalTeachers);
        loadTotalAbsences();

    }

    private void loadCount(String role, int textViewId) {
        db.collection("users").whereEqualTo("role", role).get()
                .addOnCompleteListener(task -> {
                    TextView textView = findViewById(textViewId);
                    if (task.isSuccessful()) {
                        textView.setText(String.valueOf(task.getResult().size()));
                    } else {
                        textView.setText("Error");
                        Log.e(role + "Count", "Error", task.getException());
                    }
                });
    }

    private void loadNotes() {
        db.collectionGroup("notes")
                .orderBy("derniereMiseAJour", Query.Direction.DESCENDING)
                .limit(3).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Note> notes = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                notes.add(doc.toObject(Note.class));
                            } catch (Exception e) {
                                Log.e("NoteParseError", "Error", e);
                            }
                        }
                        noteAdapter.mettreAJourListe(notes);
                    } else {
                        handleFirestoreError(task.getException(), "loading notes");
                    }
                });
    }

    private void loadAbsenceStats() {
        db.collectionGroup("notes")
                .orderBy("derniereMiseAJour", Query.Direction.DESCENDING)
                .limit(50).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Note> notes = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Note note = doc.toObject(Note.class);
                            note.setDocumentPath(doc.getReference().getPath());
                            notes.add(note);
                        }
                        noteAdapter.mettreAJourListe(notes);
                    }
                });
    }

    private void handleFirestoreError(Exception e, String action) {
        Log.e("Firestore", "Error " + action, e);
        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException fse = (FirebaseFirestoreException) e;
            if (fse.getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                Toast.makeText(this, "Permissions or indexes missing.", Toast.LENGTH_LONG).show();
            }
        }
        Toast.makeText(this, "Error " + action + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        loadAbsenceStats();
    }
}
