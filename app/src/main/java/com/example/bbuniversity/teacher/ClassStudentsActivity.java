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
                .whereEqualTo("classeCode", className)
                .get()
                .addOnSuccessListener(query -> {
                    students.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Etudiant e = doc.toObject(Etudiant.class);
                        if (e != null) students.add(e);
                    }
                    adapter.updateList(students);
                });
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