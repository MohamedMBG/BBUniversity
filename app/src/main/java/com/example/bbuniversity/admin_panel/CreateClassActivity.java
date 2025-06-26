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

public class CreateClassActivity extends AppCompatActivity {

    private TextInputEditText etClassName;
    private Button btnCreate, btnCancel;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_class);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        etClassName = findViewById(R.id.etClassName);
        btnCreate = findViewById(R.id.btnCreateClass);
        btnCancel = findViewById(R.id.btnCancel);
        db = FirebaseFirestore.getInstance();

        btnCreate.setOnClickListener(v -> addClass());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void addClass() {
        String name = etClassName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer le nom de la classe", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", name);

        db.collection("classes").document(name).set(data)
                .addOnSuccessListener(r -> {
                    Toast.makeText(this, "Classe créée", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}