package com.example.bbuniversity.admin_panel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button connectBtn, back;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // Initialize views
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        connectBtn = findViewById(R.id.connect_btn);
        back = findViewById(R.id.back);

        back.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();

        connectBtn.setOnClickListener(view -> {
            String username = String.valueOf(etUsername.getText()).trim();
            String password = String.valueOf(etPassword.getText()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            authenticateAdmin(username, password);
        });
    }

    private void authenticateAdmin(String email, String password) {
        db.collection("users")
                .whereEqualTo("role", "admin")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String storedPassword = document.getString("password");
                            if (password.equals(storedPassword)) {
                                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, AdminDashboard.class));
                                finish();
                                return;
                            }
                        }
                        Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Invalid credentials or error occurred", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
