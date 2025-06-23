package com.example.bbuniversity.teacher;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.StudentAdapter;
import com.example.bbuniversity.models.Etudiant;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;
import java.util.Map;
import java.util.HashMap;

import java.util.ArrayList;
import java.util.List;

public class ClassStudentsActivity extends AppCompatActivity implements StudentAdapter.OnStudentClickListener {

    private final List<Etudiant> students = new ArrayList<>();
    private StudentAdapter adapter;
    private String className;
    private String subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_students);

        className = getIntent().getStringExtra("class");
        subject = getIntent().getStringExtra("subject");

        TextView title = findViewById(R.id.tvClassTitle);
        title.setText(className + " - " + subject);

        RecyclerView recycler = findViewById(R.id.recyclerStudents);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(students, this);
        recycler.setAdapter(adapter);

        loadStudents();
    }

    private void loadStudents() {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("role", "student")
                .whereEqualTo("classe", className)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        students.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            try {
                                Map<String, Object> data = new HashMap<>(doc.getData());

                                // Ensure matricule is stored as String
                                if (data.get("matricule") instanceof Number) {
                                    data.put("matricule", String.valueOf(data.get("matricule")));
                                }

                                String codeClasse = data.containsKey("codeClasse")
                                        ? (String) data.get("codeClasse")
                                        : generateCodeClasse(
                                                ((Number) data.getOrDefault("niveau", 0)).intValue(),
                                                (String) data.getOrDefault("filiere", ""),
                                                (String) data.getOrDefault("classe", "")
                                        );

                                Etudiant e = new Etudiant(
                                        doc.getId(),
                                        (String) data.get("nom"),
                                        (String) data.get("prenom"),
                                        (String) data.get("email"),
                                        (String) data.get("matricule"),
                                        ((Number) data.getOrDefault("niveau", 0)).intValue(),
                                        (String) data.get("filiere"),
                                        codeClasse
                                );
                                e.setClasse((String) data.get("classe"));
                                students.add(e);
                            } catch (Exception ex) {
                                Log.e("StudentLoadError", "Error mapping student: " + ex.getMessage());
                            }
                        }
                        adapter.updateList(students);
                    } else {
                        Log.e("StudentLoadError", "Failed to load students" +
                                (task.getException() != null ? ": " + task.getException().getMessage() : ""));
                    }
                });
    }

    private String generateCodeClasse(int niveau, String filiere, String classe) {
        return niveau + filiere + classe;
    }

    @Override
    public void onStudentClick(Etudiant student) {
        // no-op
    }

    @Override
    public void onStudentLongClick(Etudiant student, android.view.View view) {
        // no-op
    }
}