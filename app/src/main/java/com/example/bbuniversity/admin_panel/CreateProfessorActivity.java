package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bbuniversity.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateProfessorActivity extends AppCompatActivity {

    private EditText etNom, etPrenom, etEmail, etPassword, etAdresse, etDepartement;
    private AutoCompleteTextView autoMatiere;
    private Button btnAssocierClasses, btnCreateStudent, btnCancel;

    private final Map<String, List<String>> enseignement = new HashMap<>();
    private final String[] CLASSES = {"1AP1", "1AP2", "2AP1", "2AP2", "3IIR1", "3IIR2"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_professor);

        initUI();

        btnAssocierClasses.setOnClickListener(v -> showClassDialog());
        btnCreateStudent.setOnClickListener(v -> createProfessor());
        btnCancel.setOnClickListener(v -> finish());
    }

    // Initialiser les composants de l'interface
    private void initUI() {
        etNom = findViewById(R.id.etNom);
        etPrenom = findViewById(R.id.etPrenom);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etAdresse = findViewById(R.id.etAdresse);
        etDepartement = findViewById(R.id.etDepartement);
        autoMatiere = findViewById(R.id.autoMatiere);

        // Ouvrir le menu déroulant au clic
        autoMatiere.setOnClickListener(v -> autoMatiere.showDropDown());

        // Charger dynamiquement les matières depuis Firestore
        FirebaseFirestore.getInstance().collection("matieres")
                .get()
                .addOnSuccessListener(query -> {
                    List<String> matieresList = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String nom = doc.getString("nom");
                        if (nom != null) matieresList.add(nom);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, matieresList);
                    autoMatiere.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur de chargement des matières", Toast.LENGTH_SHORT).show();
                });

        btnAssocierClasses = findViewById(R.id.btnAssocierClasses);
        btnCreateStudent = findViewById(R.id.btnCreateStudent);
        btnCancel = findViewById(R.id.btnCancel);
    }

    // Dialog pour sélectionner les classes associées à une matière
    private void showClassDialog() {
        String selectedMatiere = autoMatiere.getText().toString().trim();
        if (selectedMatiere.isEmpty()) {
            Toast.makeText(this, "Sélectionnez une matière d'abord", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean[] selected = new boolean[CLASSES.length];
        List<String> selectedClasses = new ArrayList<>();

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sélectionner des classes")
                .setMultiChoiceItems(CLASSES, selected, (dialog, which, isChecked) -> {
                    if (isChecked) selectedClasses.add(CLASSES[which]);
                    else selectedClasses.remove(CLASSES[which]);
                })
                .setPositiveButton("Valider", (dialog, which) -> {
                    if (!selectedClasses.isEmpty())
                        enseignement.put(selectedMatiere, new ArrayList<>(selectedClasses));
                    Toast.makeText(this, "Classes associées à " + selectedMatiere, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    // Création du professeur dans Firestore
    private void createProfessor() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String nom = etNom.getText().toString().trim();
        String prenom = etPrenom.getText().toString().trim();
        String adresse = etAdresse.getText().toString().trim();
        String departement = etDepartement.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || nom.isEmpty() || prenom.isEmpty())
            return;

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        user.updateProfile(new UserProfileChangeRequest.Builder()
                                .setDisplayName(prenom + " " + nom).build());

                        Map<String, Object> profData = new HashMap<>();
                        profData.put("uid", user.getUid());
                        profData.put("nom", nom);
                        profData.put("prenom", prenom);
                        profData.put("email", email);
                        profData.put("role", "professor");
                        profData.put("adresse", adresse);
                        profData.put("departement", departement);
                        profData.put("enseignement", enseignement);

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getUid())
                                .set(profData)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Professeur créé avec succès", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Erreur : " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}