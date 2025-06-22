package com.example.bbuniversity.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class TeacherActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button connectBtn, back;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        connectBtn = findViewById(R.id.connect_btn);
        back = findViewById(R.id.back);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        back.setOnClickListener(v -> finish());

        connectBtn.setOnClickListener(v -> {
            String email = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            loginTeacher(email, password);
        });
    }

    private void loginTeacher(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user == null) {
                        Toast.makeText(this, "Authentication error", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String uid = user.getUid();
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists() && "professor".equalsIgnoreCase(doc.getString("role"))) {
                                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, TeacherDashboard.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "Access Denied: Not a professor", Toast.LENGTH_SHORT).show();
                                    auth.signOut();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error fetching user role", Toast.LENGTH_SHORT).show();
                                auth.signOut();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}