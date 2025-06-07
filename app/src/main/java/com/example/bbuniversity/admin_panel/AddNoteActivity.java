package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddNoteActivity extends AppCompatActivity {

    private TextInputEditText etStudentEmail, etMatiere, etControle, etExamen, etParticipation;
    private TextView tvPreviewGrade;
    private Button btnAddNote, btnCancel;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_note);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        db = FirebaseFirestore.getInstance();

        // Initialize views
        etStudentEmail = findViewById(R.id.etStudentEmail);
        etMatiere = findViewById(R.id.etMatiere);
        etControle = findViewById(R.id.etControle);
        etExamen = findViewById(R.id.etExamen);
        etParticipation = findViewById(R.id.etParticipation);
        tvPreviewGrade = findViewById(R.id.tvPreviewGrade);
        btnAddNote = findViewById(R.id.btnAddNote);
        btnCancel = findViewById(R.id.btnCancel);

        // Setup listeners for grade calculation
        setupGradeCalculators();

        btnAddNote.setOnClickListener(v -> addNote());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void setupGradeCalculators() {
        View.OnClickListener gradeCalculator = v -> calculateAverage();
        etControle.setOnClickListener(gradeCalculator);
        etExamen.setOnClickListener(gradeCalculator);
        etParticipation.setOnClickListener(gradeCalculator);
    }

    private void calculateAverage() {
        try {
            double controle = etControle.getText().toString().isEmpty() ? 0 :
                    Double.parseDouble(etControle.getText().toString());
            double examen = etExamen.getText().toString().isEmpty() ? 0 :
                    Double.parseDouble(etExamen.getText().toString());
            double participation = etParticipation.getText().toString().isEmpty() ? 0 :
                    Double.parseDouble(etParticipation.getText().toString());

            double moyenne = (controle + examen + participation) / 3;
            tvPreviewGrade.setText(String.format("%.2f/20", moyenne));
        } catch (NumberFormatException e) {
            tvPreviewGrade.setText("--/20");
        }
    }

    private void addNote() {
        String studentEmail = etStudentEmail.getText().toString().trim();
        String matiere = etMatiere.getText().toString().trim();
        String controleStr = etControle.getText().toString().trim();
        String examenStr = etExamen.getText().toString().trim();
        String participationStr = etParticipation.getText().toString().trim();

        if (studentEmail.isEmpty() || matiere.isEmpty() ||
                controleStr.isEmpty() || examenStr.isEmpty() || participationStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double controle = Double.parseDouble(controleStr);
            double examen = Double.parseDouble(examenStr);
            double participation = Double.parseDouble(participationStr);

            double moyenne = (controle + examen + participation) / 3;

            //1. Trouver l'étudiant par email pour récupérer le UID
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("email", studentEmail)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Toast.makeText(this, "Étudiant introuvable", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String uid = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // In the addNote() method, modify the note map creation:
                        Map<String, Object> note = new HashMap<>();
                        note.put("matiere", matiere);  // Exact Firestore field name
                        note.put("controle", controle);
                        note.put("examenFinal", examen);
                        note.put("participation", participation);
                        note.put("noteGenerale", moyenne);
                        note.put("professeurId", "admin");
                        note.put("derniereMiseAJour", Timestamp.now());



                        //  Stocker dans la sous-collection de l'étudiant
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .collection("notes")
                                .document(matiere)
                                .set(note)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Note enregistrée", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erreur Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Les notes doivent être des nombres valides", Toast.LENGTH_SHORT).show();
        }
    }

}