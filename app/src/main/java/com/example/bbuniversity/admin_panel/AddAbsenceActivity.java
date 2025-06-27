package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddAbsenceActivity extends AppCompatActivity {

    private TextInputEditText etStudentEmail, etDate;
    private TextInputEditText etMatiere;
    private MaterialCheckBox cbJustified;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_absence);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        db = FirebaseFirestore.getInstance();

        etStudentEmail = findViewById(R.id.etStudentEmail);
        etMatiere = findViewById(R.id.etMatiere);
        etDate = findViewById(R.id.etDate);
        cbJustified = findViewById(R.id.cbJustified);
        MaterialButton btnAdd = findViewById(R.id.btnAddAbsence);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);

        etMatiere.setFocusable(false);
        etMatiere.setOnClickListener(v -> showSubjectDialog());

        btnAdd.setOnClickListener(v -> addAbsence());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showSubjectDialog() {
        db.collection("matieres").get()
                .addOnSuccessListener(snapshot -> {
                    java.util.List<String> subjects = new java.util.ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String nom = doc.getString("nom");
                        if (nom != null) subjects.add(nom);
                    }
                    if (subjects.isEmpty()) {
                        Toast.makeText(this, "Aucune matière trouvée", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] array = subjects.toArray(new String[0]);
                    new AlertDialog.Builder(this)
                            .setTitle("Sélectionner la matière")
                            .setItems(array, (d, which) -> etMatiere.setText(array[which]))
                            .show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addAbsence() {
        String email = etStudentEmail.getText().toString().trim();
        String matiere = etMatiere.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        boolean justified = cbJustified.isChecked();

        if (email.isEmpty() || matiere.isEmpty() || dateStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        Date date;
        try {
            date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr);
        } catch (ParseException e) {
            Toast.makeText(this, "Format de date invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        Toast.makeText(this, "Étudiant introuvable", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String uid = q.getDocuments().get(0).getId();
                    Map<String, Object> data = new HashMap<>();
                    data.put("matiere", matiere);
                    data.put("date", date);
                    data.put("justifiee", justified);

                    db.collection("users").document(uid).collection("abscence")
                            .add(data)
                            .addOnSuccessListener(r -> {
                                if (!justified) adjustGrade(uid, matiere, -0.2);
                                Toast.makeText(this, "Absence enregistrée", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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