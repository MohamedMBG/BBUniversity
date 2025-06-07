package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Professeur;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateProfessorActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etNom, etPrenom, etPassword, etAdresse, etDepartement;
    private MaterialButton btnCreate, btnCancel;
    AutoCompleteTextView classDropdown;


    private List<String> allClasses = new ArrayList<>();
    private boolean[] selectedClasses;
    private List<String> selectedClassList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_professor);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        //initialization of firebase firestore and auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //connecting the ui elements
        etEmail = findViewById(R.id.etEmail);
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etPassword = findViewById(R.id.etPassword);
        etAdresse = findViewById(R.id.etAdresse);
        etDepartement = findViewById(R.id.etDepartement);
        classDropdown = findViewById(R.id.classDropdown);



        fetchClasses();

        //create and cancel buttons
        btnCreate = findViewById(R.id.btnCreateStudent);
        btnCancel = findViewById(R.id.btnCancel);

        //calling the methods on actions
        btnCreate.setOnClickListener(v -> createProfesseur());
        btnCancel.setOnClickListener(v -> finish());

    }

    private void fetchClasses() {
        db.collection("classes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allClasses.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String className = doc.getString("name");
                    if (className != null) allClasses.add(className);
                }

                selectedClasses = new boolean[allClasses.size()];

                classDropdown.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Sélectionner les classes");

                    builder.setMultiChoiceItems(allClasses.toArray(new String[0]), selectedClasses, (dialog, which, isChecked) -> {
                        String selected = allClasses.get(which);
                        if (isChecked && !selectedClassList.contains(selected)) {
                            selectedClassList.add(selected);
                        } else if (!isChecked) {
                            selectedClassList.remove(selected);
                        }
                    });

                    builder.setPositiveButton("OK", (dialog, which) -> {
                        classDropdown.setText(TextUtils.join(", ", selectedClassList));
                    });

                    builder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

                    builder.show();
                });

            } else {
                Toast.makeText(this, "Erreur lors du chargement des classes", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void createProfesseur() {
        //get the values from the edittexts in the ui
        String email = etEmail.getText().toString().trim();
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String adresse = etAdresse.getText().toString().trim();
        String departement = etDepartement.getText().toString().trim();
        if (selectedClassList.isEmpty()) {
            Toast.makeText(this, "Veuillez sélectionner au moins une classe", Toast.LENGTH_SHORT).show();
            return;
        }
        //checking if the fields are empty
        if (email.isEmpty() || nom.isEmpty() || prenom.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        //la creation du compte d'utilisateur dans la phase d'atuhentification
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // Mise à jour du nom de profil
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nom + " " + prenom)
                                    .build();
                            firebaseUser.updateProfile(profileUpdates);

                            // Préparer la structure matieres (exemple : "Math" → ["3IIR1"])
                            Map<String, List<String>> matieres = new HashMap<>();
                            matieres.put("À définir", selectedClassList);

                            Professeur professeur = new Professeur(uid, nom, prenom, email, "professor", departement, adresse, matieres);

                            //saving the professor in the users collections in firestore
                            db.collection("users").document(uid).set(professeur)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Professeur créé avec succès", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Échec de l'inscription: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}