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
import com.example.bbuniversity.TimetableViewActivity;
import com.example.bbuniversity.adapters.ClassAdapter;
import com.example.bbuniversity.admin_panel.AddNoteActivity;
import com.example.bbuniversity.admin_panel.AdminActivity;
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
        findViewById(R.id.btnTeacherTimetable).setOnClickListener(v -> {
            Intent i = new Intent(this, TimetableViewActivity.class);
            FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
            if (current != null) {
                i.putExtra("teacherId", current.getUid());
            }
            startActivity(i);
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadTeacherInfo(user.getUid());
        }
        logout();
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

    private void logout() {
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, AdminActivity.class));
            finish();
        });

    }

    @Override
    public void onClassClick(ClassInfo info) {
        Intent i = new Intent(this, ClassStudentsActivity.class);
        i.putExtra("class", info.getClassName());
        i.putExtra("subject", info.getSubject());
        startActivity(i);
    }
}