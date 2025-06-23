package com.example.bbuniversity.teacher;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.StudentAdapter;
import com.example.bbuniversity.models.Etudiant;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassStudentsActivity extends AppCompatActivity implements StudentAdapter.OnStudentClickListener {

    private final List<Etudiant> students = new ArrayList<>();
    private StudentAdapter adapter;
    private String className;
    private String subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_students);
        EdgeToEdge.enable(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


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

    private String generateCodeClasse(int niveau, String filiere, String classe) {
        return niveau + filiere + classe;
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
                    adapter.updateList(students);
                });
    }

    @Override
    public void onStudentClick(Etudiant student) {
        // no-op
        android.content.Intent intent = new android.content.Intent(this, com.example.bbuniversity.admin_panel.AddNoteActivity.class);
        intent.putExtra("studentEmail", student.getEmail());
        intent.putExtra("subject", subject);
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ?
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null) intent.putExtra("professorId", uid);
        startActivity(intent);
    }

    @Override
    public void onStudentLongClick(Etudiant student, android.view.View view) {
        // no-op
    }
}