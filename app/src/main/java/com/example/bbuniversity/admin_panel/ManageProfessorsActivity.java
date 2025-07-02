package com.example.bbuniversity.admin_panel;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.TeacherAdapter;
import com.example.bbuniversity.models.Professeur;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageProfessorsActivity extends AppCompatActivity implements TeacherAdapter.OnTeacherClickListener {

    private RecyclerView teachersRv;
    private TeacherAdapter adapter;
    private List<Professeur> teacherList = new ArrayList<>();
    private List<Professeur> filteredTeachers = new ArrayList<>();
    private TextInputEditText searchInput;
    private ImageView goBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_professors);
        EdgeToEdge.enable(this);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        // Initialisation des composants
        goBack = findViewById(R.id.btn_back);
        searchInput = findViewById(R.id.searchInput);
        teachersRv = findViewById(R.id.recyclerViewTeachers);

        db = FirebaseFirestore.getInstance();

        // Click sur bouton retour
        goBack.setOnClickListener(v -> finish());

        // Setup du RecyclerView
        adapter = new TeacherAdapter(filteredTeachers, this);
        teachersRv.setLayoutManager(new LinearLayoutManager(this));
        teachersRv.setAdapter(adapter);

        fetchProfessors();
        //activation de la barre de recherche :
        setupSearch();
    }

    private void setupSearch() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFilteredList(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void updateFilteredList(String query) {
        filteredTeachers.clear();
        if (query == null || query.isEmpty()) {
            filteredTeachers.addAll(teacherList);
        } else {
            String lower = query.toLowerCase();
            for (Professeur p : teacherList) {
                if (p != null && matchesQuery(p, lower)) {
                    filteredTeachers.add(p);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private boolean matchesQuery(Professeur p, String q) {
        return (p.getNom() != null && p.getNom().toLowerCase().contains(q)) ||
                (p.getPrenom() != null && p.getPrenom().toLowerCase().contains(q)) ||
                (p.getEmail() != null && p.getEmail().toLowerCase().contains(q)) ||
                (p.getDepartement() != null && p.getDepartement().toLowerCase().contains(q));
    }

    @Override
    public void onTeacherClick(Professeur teacher) {
        if (teacher != null && teacher.getUid() != null) {
            Intent intent = new Intent(this, CreateProfessorActivity.class);
            intent.putExtra("teacherId", teacher.getUid());
            startActivity(intent);
        }
    }

    private void fetchProfessors() {
        db.collection("users")
                .whereEqualTo("role", "professor")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    teacherList.clear();
                    filteredTeachers.clear();

                    Log.d("FIRESTORE", "Nombre de documents : " + querySnapshot.size()); // 🔥 Ajoute ça

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            Professeur prof = doc.toObject(Professeur.class);
                            Log.d("FIRESTORE", "Professeur récupéré : " + prof.getNom() + " " + prof.getPrenom()); // 🔥 et ça
                            if (prof != null) {
                                teacherList.add(prof);
                                filteredTeachers.add(prof);
                            }
                        } catch (Exception e) {
                            Log.e("FIRESTORE", "Erreur de mapping Professeur", e);
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }
}
