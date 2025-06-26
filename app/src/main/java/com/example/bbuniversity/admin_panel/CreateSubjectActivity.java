package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateSubjectActivity extends AppCompatActivity {

    private TextInputEditText etSubjectName;
    private Button btnCreate, btnCancel;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_subject);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        etSubjectName = findViewById(R.id.etSubjectName);
        btnCreate = findViewById(R.id.btnCreateSubject);
        btnCancel = findViewById(R.id.btnCancel);
        db = FirebaseFirestore.getInstance();

        btnCreate.setOnClickListener(v -> addSubject());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void addSubject() {
        String nom = etSubjectName.getText().toString().trim();
        if (nom.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer le nom de la matière", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("nom", nom);

        db.collection("matieres").document(nom).set(data)
                .addOnSuccessListener(r -> {
                    Toast.makeText(this, "Matière créée", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}