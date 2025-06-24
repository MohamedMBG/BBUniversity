package com.example.bbuniversity.admin_panel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.ManageAbsenceAdapter;
import com.example.bbuniversity.models.Abscence;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageAbsencesActivity extends AppCompatActivity implements ManageAbsenceAdapter.OnJustifyListener {

    private String studentId;
    private final List<Abscence> absences = new ArrayList<>();
    private ManageAbsenceAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_absences);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        studentId = getIntent().getStringExtra("studentId");
        if (studentId == null) {
            Toast.makeText(this, "Student ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) RecyclerView recycler = findViewById(R.id.recyclerAbsences);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageAbsenceAdapter(absences, this);
        recycler.setAdapter(adapter);

        loadAbsences();
    }

    private void loadAbsences() {
        db.collection("users").document(studentId).collection("abscence").get()
                .addOnSuccessListener(query -> {
                    absences.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Abscence a = doc.toObject(Abscence.class);
                        if (a != null) {
                            a.setDocumentId(doc.getId());
                            absences.add(a);
                        }
                    }
                    adapter.updateData(absences);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onJustify(Abscence absence) {
        DocumentReference ref = db.collection("users").document(studentId)
                .collection("abscence").document(absence.getDocumentId());
        ref.update("justifiee", true)
                .addOnSuccessListener(aVoid -> {
                    adjustGrade(studentId, absence.getMatiere(), 0.2);
                    loadAbsences();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void adjustGrade(String uid, String matiere, double delta) {
        DocumentReference ref = db.collection("users").document(uid).collection("notes").document(matiere);
        ref.get().addOnSuccessListener(doc -> {
            Double note = doc.getDouble("noteGenerale");
            if (note != null) {
                double newNote = note + delta;
                Map<String, Object> update = new HashMap<>();
                update.put("noteGenerale", newNote);
                update.put("derniereMiseAJour", Timestamp.now());
                ref.update(update);
            }
        });
    }
}