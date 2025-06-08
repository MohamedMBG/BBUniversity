package com.example.bbuniversity.admin_panel;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.StudentAdapter;
import com.example.bbuniversity.models.Etudiant;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageStudentsActivity extends AppCompatActivity implements StudentAdapter.OnStudentClickListener {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private List<Etudiant> studentList = new ArrayList<>();
    private List<Etudiant> filteredList = new ArrayList<>();
    private TextInputEditText searchInput;
    private ImageView fabBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_manage_students);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        try {
            db = FirebaseFirestore.getInstance();
            initializeViews();
            setupRecyclerView();
            loadStudents();
            setupSearch();
        } catch (Exception e) {
            Toast.makeText(this, "Initialization error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        searchInput = findViewById(R.id.searchInput);
        recyclerView = findViewById(R.id.recyclerViewStudents);
        fabBack = findViewById(R.id.btn_back);
        fabBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(filteredList, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadStudents() {
        db.collection("users")
                .whereEqualTo("role", "student")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        studentList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                // Convert Firestore document to a Map
                                Map<String, Object> data = new HashMap<>(doc.getData());

                                // Fix: Convert 'matricule' from Long to String if needed
                                if (data.get("matricule") instanceof Long) {
                                    data.put("matricule", String.valueOf(data.get("matricule")));
                                }

                                // Get codeClasse if exists, otherwise generate it from components
                                String codeClasse = data.containsKey("codeClasse") ?
                                        (String) data.get("codeClasse") :
                                        generateCodeClasse(
                                                ((Number) data.getOrDefault("niveau", 0)).intValue(),
                                                (String) data.getOrDefault("filiere", ""),
                                                (String) data.getOrDefault("classe", "")
                                        );

                                // Manually create Etudiant object
                                Etudiant student = new Etudiant(
                                        doc.getId(),
                                        (String) data.get("nom"),
                                        (String) data.get("prenom"),
                                        (String) data.get("email"),
                                        (String) data.get("matricule"),  // Now ensured to be String
                                        ((Number) data.getOrDefault("niveau", 0)).intValue(),  // Handle Number -> int
                                        (String) data.get("filiere"),
                                        codeClasse  // Using codeClasse instead of classe
                                );

                                studentList.add(student);
                            } catch (Exception e) {
                                Log.e("DeserializationError", "Error processing student: " + e.getMessage());
                            }
                        }
                        updateFilteredList("");
                    } else {
                        Toast.makeText(this, "Error loading students", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateCodeClasse(int niveau, String filiere, String classe) {
        return niveau + filiere + classe;
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFilteredList(s != null ? s.toString() : "");
            }
        });
    }

    private void updateFilteredList(String query) {
        try {
            filteredList.clear();

            if (query == null || query.isEmpty()) {
                filteredList.addAll(studentList);
            } else {
                String lowerCaseQuery = query.toLowerCase();
                for (Etudiant student : studentList) {
                    if (student != null && matchesQuery(student, lowerCaseQuery)) {
                        filteredList.add(student);
                    }
                }
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(this, "Error filtering students: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean matchesQuery(Etudiant student, String lowerCaseQuery) {
        return (student.getNom() != null && student.getNom().toLowerCase().contains(lowerCaseQuery)) ||
                (student.getPrenom() != null && student.getPrenom().toLowerCase().contains(lowerCaseQuery)) ||
                (student.getEmail() != null && student.getEmail().toLowerCase().contains(lowerCaseQuery)) ||
                (student.getMatricule() != null && student.getMatricule().toLowerCase().contains(lowerCaseQuery)) ||
                (student.getClasseCode() != null && student.getClasseCode().toLowerCase().contains(lowerCaseQuery)) ||
                (student.getFiliere() != null && student.getFiliere().toLowerCase().contains(lowerCaseQuery));
    }

    @Override
    public void onStudentClick(Etudiant student) {
        try {
            if (student != null && student.getUid() != null) {
                Intent intent = new Intent(this, EditStudentDetailsActivity.class);
                intent.putExtra("studentId", student.getUid());
                startActivity(intent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error opening student details: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStudentLongClick(Etudiant student, View view) {
        try {
            if (student != null && student.getPrenom() != null) {
                Toast.makeText(this, "Long pressed: " + student.getPrenom(),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Silent fail for long press
        }
    }
}