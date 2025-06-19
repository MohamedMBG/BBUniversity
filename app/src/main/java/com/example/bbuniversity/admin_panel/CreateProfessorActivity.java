package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Professeur;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.android.material.textfield.TextInputEditText;
import java.util.*;

public class CreateProfessorActivity extends AppCompatActivity {

    // Champs de saisie
    private TextInputEditText etNom, etPrenom, etEmail, etPassword, etAdresse, etDepartement;
    private AutoCompleteTextView autoMatiere;
    private Button btnCreate, btnCancel, btnAssocierClasses;

    // Listes pour toutes les classes Firestore et celles sélectionnées par l'utilisateur
    private final List<String> allClasses = new ArrayList<>();
    private final List<String> selectedClasses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_professor);

        // Lier les vues avec leurs IDs
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etAdresse = findViewById(R.id.etAdresse);
        etDepartement = findViewById(R.id.etDepartement);
        autoMatiere = findViewById(R.id.autoMatiere);
        btnCreate = findViewById(R.id.btnCreateStudent);
        btnCancel = findViewById(R.id.btnCancel);
        btnAssocierClasses = findViewById(R.id.btnAssocierClasses);

        // Afficher la liste déroulante des matières
        autoMatiere.setOnClickListener(v -> autoMatiere.showDropDown());

        // Charger les matières depuis Firestore
        FirebaseFirestore.getInstance().collection("matieres").get()
                .addOnSuccessListener(snapshot -> {
                    List<String> matieres = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot) {
                        String nom = doc.getString("nom");
                        if (nom != null) matieres.add(nom);
                    }
                    autoMatiere.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, matieres));
                });

        // Boutons
        btnCreate.setOnClickListener(v -> createProfessor());
        btnCancel.setOnClickListener(v -> finish());
        btnAssocierClasses.setOnClickListener(v -> showClassDialog());
    }

    // Affiche une boîte de dialogue pour choisir les classes (depuis Firestore)
    private void showClassDialog() {
        FirebaseFirestore.getInstance().collection("classes").get()
                .addOnSuccessListener(snapshot -> {
                    allClasses.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String name = doc.getString("name");
                        if (name != null) allClasses.add(name);
                    }

                    if (allClasses.isEmpty()) {
                        Toast.makeText(this, "Aucune classe trouvée", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean[] checked = new boolean[allClasses.size()];
                    String[] classesArray = allClasses.toArray(new String[0]);

                    new AlertDialog.Builder(this)
                            .setTitle("Sélectionner les classes")
                            .setMultiChoiceItems(classesArray, checked, (dialog, i, isChecked) -> {
                                String c = classesArray[i];
                                if (isChecked) selectedClasses.add(c); else selectedClasses.remove(c);
                            })
                            .setPositiveButton("OK", null)
                            .setNegativeButton("Annuler", null)
                            .show();
                });
    }

    // Crée un compte professeur et l'enregistre dans Firestore
    private void createProfessor() {
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String adresse = etAdresse.getText().toString().trim();
        String dep = etDepartement.getText().toString().trim();
        String matiere = autoMatiere.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Email et mot de passe requis", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(auth -> {
                    String uid = auth.getUser().getUid();
                    Map<String, List<String>> enseignement = new HashMap<>();
                    enseignement.put(matiere, new ArrayList<>(selectedClasses)); // matière → classes

                    //appeler le constrcuteur professeur
                    Professeur prof = new Professeur(uid, nom, prenom, email, "professor", dep, adresse, enseignement);

                    //specification d'url d'enregistrement
                    FirebaseFirestore.getInstance().collection("users").document(uid).set(prof)
                            .addOnSuccessListener(r -> {
                                Toast.makeText(this, "Professeur créé avec succès", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Erreur Firestore", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur Auth", Toast.LENGTH_SHORT).show());
    }
}