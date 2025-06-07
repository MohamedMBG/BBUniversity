package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Etudiant;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateStudentActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etNom, etPassword, etPrenom;
    private TextInputEditText etMatricule, etFiliere, etClasse, etNiveau;
    private Button btnCreateStudent, cancel;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private AutoCompleteTextView classDropdown;
    private List<String> classList = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_student);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etNom = findViewById(R.id.etNom);
        etPassword = findViewById(R.id.etPassword);
        etPrenom = findViewById(R.id.etPrenom);
        etMatricule = findViewById(R.id.etMatricule);
        etFiliere = findViewById(R.id.etFiliere);
        classDropdown = findViewById(R.id.classDropdown);
        cancel = findViewById(R.id.btnCancel);

        cancel.setOnClickListener(v -> finish());

        etNiveau = findViewById(R.id.etNiveau);
        btnCreateStudent = findViewById(R.id.btnCreateStudent);

        btnCreateStudent.setOnClickListener(v -> createStudent());

        fetchClasses();
    }

    private void fetchClasses() {
        db.collection("classes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                classList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String className = doc.getString("name");
                    if (className != null) classList.add(className);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, classList);
                classDropdown.setAdapter(adapter);

                // Add this to show dropdown when clicked
                classDropdown.setOnClickListener(v -> {
                    classDropdown.showDropDown();
                });

                // Set threshold to 1 character to show all options when clicked
                classDropdown.setThreshold(1);
            } else {
                Toast.makeText(this, "Erreur lors du chargement des classes", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void createStudent() {
        String email = etEmail.getText().toString().trim();
        String nom = etNom.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String matriculeStr = etMatricule.getText().toString().trim();
        String filiere = etFiliere.getText().toString().trim();
        String classe = classDropdown.getText().toString().trim();
        String niveauStr = etNiveau.getText().toString().trim();

        if (email.isEmpty() || nom.isEmpty() || password.isEmpty() || prenom.isEmpty() ||
                matriculeStr.isEmpty() || filiere.isEmpty() || classe.isEmpty() || niveauStr.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int matricule = Integer.parseInt(matriculeStr);
            int niveau = Integer.parseInt(niveauStr);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(authTask -> {
                        if (authTask.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(prenom + " " + nom)
                                    .build();

                            mAuth.getCurrentUser().updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            createFirestoreStudent(uid, email, nom, prenom, matricule, filiere, classe, niveau);
                                        } else {
                                            Toast.makeText(this,
                                                    "Erreur de mise à jour du profil: " + profileTask.getException(),
                                                    Toast.LENGTH_SHORT).show();
                                            mAuth.getCurrentUser().delete();
                                        }
                                    });
                        } else {
                            Toast.makeText(this,
                                    "Erreur de création du compte: " + authTask.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Matricule et niveau doivent être des nombres", Toast.LENGTH_SHORT).show();
        }
    }

    private void createFirestoreStudent(String uid, String email, String nom, String prenom,
                                        int matricule, String filiere, String classe, int niveau) {
        Map<String, Object> studentData = new HashMap<>();
        studentData.put("uid", uid);
        studentData.put("email", email);
        studentData.put("nom", nom);
        studentData.put("prenom", prenom);
        studentData.put("matricule", matricule);
        studentData.put("filiere", filiere);
        studentData.put("classe", classe);
        studentData.put("niveau", niveau);
        studentData.put("role", "student");

        db.collection("users")
                .document(uid)
                .set(studentData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Étudiant créé avec succès", Toast.LENGTH_SHORT).show();

                    // Optionnel : créer une note de base pour une matière (peut être supprimé)
                    Map<String, Object> noteInit = new HashMap<>();
                    noteInit.put("matiereId", "placeholder");
                    noteInit.put("noteParticipation", 0.0);
                    noteInit.put("noteCC", 0.0);
                    noteInit.put("noteExamen", 0.0);
                    noteInit.put("absencesNonJustifiees", 0);
                    noteInit.put("noteTotale", 0.0);

                    db.collection("users").document(uid)
                            .collection("notes")
                            .document("placeholder")
                            .set(noteInit);

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Erreur de création dans Firestore: " + e.getMessage() +
                                    "\nLe compte d'authentification a été supprimé",
                            Toast.LENGTH_LONG).show();
                    mAuth.getCurrentUser().delete();
                });
    }


}