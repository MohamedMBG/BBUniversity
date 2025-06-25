package com.example.bbuniversity.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.ClassAdapter;
import com.example.bbuniversity.admin_panel.AddNoteActivity;
import com.example.bbuniversity.models.ClassInfo;
import com.example.bbuniversity.models.Professeur;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeacherDashboard extends AppCompatActivity implements ClassAdapter.OnClassClickListener {

    private TextView tvWelcome;
    private RecyclerView rvNextClasses;
    private ClassAdapter adapter;
    private final List<ClassInfo> classInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_dashboard);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        tvWelcome = findViewById(R.id.tvWelcome);
        rvNextClasses = findViewById(R.id.rvNextClasses);
        rvNextClasses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ClassAdapter(classInfos, this);
        rvNextClasses.setAdapter(adapter);

        findViewById(R.id.btnComplaints).setOnClickListener(v ->
                startActivity(new Intent(this, TeacherComplaintsActivity.class)));
        findViewById(R.id.btnGrade).setOnClickListener(v ->
                startActivity(new Intent(this, AddNoteActivity.class)));
        findViewById(R.id.btnAllClasses).setOnClickListener(v ->
                startActivity(new Intent(this, TeacherClassesActivity.class)));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadTeacherInfo(user.getUid());
        }
    }

    private void loadTeacherInfo(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(this::populateInfo);
    }

    private void populateInfo(DocumentSnapshot doc) {
        Professeur prof = doc.toObject(Professeur.class);
        if (prof == null) return;
        String name = prof.getPrenom() + " " + prof.getNom();
        tvWelcome.setText(getString(R.string.welcome_teacher, name));

        Map<String, List<String>> ens = prof.getEnseignement();
        classInfos.clear();
        if (ens != null) {
            for (Map.Entry<String, List<String>> e : ens.entrySet()) {
                String matiere = e.getKey();
                for (String c : e.getValue()) {
                    classInfos.add(new ClassInfo(c, matiere));
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