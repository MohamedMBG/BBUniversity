package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Etudiant;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditStudentDetailsActivity extends AppCompatActivity {

    private TextInputEditText etNom, etPrenom, etEmail, etMatricule, etNiveau, etFiliere, etClasseCode;
    private MaterialButton btnSave;
    private FirebaseFirestore db;
    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student_details);

        db = FirebaseFirestore.getInstance();
        studentId = getIntent().getStringExtra("studentId");

        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Student ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadStudentData();

        btnSave.setOnClickListener(v -> saveStudentData());
    }

    private void initializeViews() {
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etEmail = findViewById(R.id.etEmail);
        etMatricule = findViewById(R.id.etMatricule);
        etNiveau = findViewById(R.id.etNiveau);
        etFiliere = findViewById(R.id.etFiliere);
        etClasseCode = findViewById(R.id.etClasseCode);
        btnSave = findViewById(R.id.btnSave);

        // Make email non-editable as it's used for authentication
        etEmail.setEnabled(false);
    }

    private void loadStudentData() {
        db.collection("users").document(studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get data as map to handle type conversions
                            Map<String, Object> data = document.getData();
                            if (data != null) {
                                populateFields(data);
                            }
                        } else {
                            Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Error loading student data", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void populateFields(Map<String, Object> studentData) {
        etNom.setText(getStringValue(studentData, "nom"));
        etPrenom.setText(getStringValue(studentData, "prenom"));
        etEmail.setText(getStringValue(studentData, "email"));

        // Handle matricule which might be stored as number or string
        Object matricule = studentData.get("matricule");
        if (matricule instanceof Number) {
            etMatricule.setText(String.valueOf(((Number) matricule).longValue()));
        } else {
            etMatricule.setText(matricule != null ? matricule.toString() : "");
        }

        // Handle niveau
        Object niveau = studentData.get("niveau");
        if (niveau instanceof Number) {
            etNiveau.setText(String.valueOf(((Number) niveau).intValue()));
        } else {
            etNiveau.setText(niveau != null ? niveau.toString() : "0");
        }

        etFiliere.setText(getStringValue(studentData, "filiere"));
        etClasseCode.setText(getStringValue(studentData, "classeCode", "classe")); // Try both field names
    }

    private String getStringValue(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null) {
                return value.toString();
            }
        }
        return "";
    }

    private void saveStudentData() {
        // Get all field values with null checks
        String nom = etNom.getText() != null ? etNom.getText().toString().trim() : "";
        String prenom = etPrenom.getText() != null ? etPrenom.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String matriculeStr = etMatricule.getText() != null ? etMatricule.getText().toString().trim() : "";
        String niveauStr = etNiveau.getText() != null ? etNiveau.getText().toString().trim() : "0";
        String filiere = etFiliere.getText() != null ? etFiliere.getText().toString().trim() : "";
        String classeCode = etClasseCode.getText() != null ? etClasseCode.getText().toString().trim() : "";

        // Validate required fields
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || matriculeStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert numbers
        long matricule;
        int niveau;
        try {
            matricule = Long.parseLong(matriculeStr);
            niveau = Integer.parseInt(niveauStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Matricule and Niveau must be valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create update map (don't update email as it's tied to authentication)
        Map<String, Object> updates = new HashMap<>();
        updates.put("nom", nom);
        updates.put("prenom", prenom);
        updates.put("matricule", matricule);
        updates.put("niveau", niveau);
        updates.put("filiere", filiere);
        updates.put("classe", classeCode);
        updates.put("role", "student"); // Ensure role stays as student

        // Update in Firestore
        db.collection("users").document(studentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating student: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}