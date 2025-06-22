package com.example.bbuniversity.teacher;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.ClassAdapter;
import com.example.bbuniversity.models.ClassInfo;
import com.example.bbuniversity.models.Professeur;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeacherClassesActivity extends AppCompatActivity implements ClassAdapter.OnClassClickListener {

    private ClassAdapter adapter;
    private final List<ClassInfo> classes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_classes);

        RecyclerView recycler = findViewById(R.id.recyclerClasses);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(classes, this);
        recycler.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) loadClasses(user.getUid());
    }

    private void loadClasses(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(this::handleDoc);
    }

    private void handleDoc(DocumentSnapshot doc) {
        Professeur p = doc.toObject(Professeur.class);
        classes.clear();
        if (p != null && p.getEnseignement() != null) {
            for (Map.Entry<String, List<String>> e : p.getEnseignement().entrySet()) {
                for (String c : e.getValue()) {
                    classes.add(new ClassInfo(c, e.getKey()));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClassClick(ClassInfo info) {
        Intent i = new Intent(this, ClassStudentsActivity.class);
        i.putExtra("class", info.getClassName());
        i.putExtra("subject", info.getSubject());
        startActivity(i);
    }
}