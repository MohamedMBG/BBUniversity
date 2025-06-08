package com.example.bbuniversity.admin_panel;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

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

public class ManageProfessorsActivity extends AppCompatActivity {

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

        // Initialisation des composants
        goBack = findViewById(R.id.btn_back);
        searchInput = findViewById(R.id.searchInput);
        teachersRv = findViewById(R.id.recyclerViewTeachers);

        db = FirebaseFirestore.getInstance();

        // Click sur bouton retour
        goBack.setOnClickListener(v -> finish());

        // Setup du RecyclerView
        adapter = new TeacherAdapter(filteredTeachers);
        teachersRv.setLayoutManager(new LinearLayoutManager(this));
        teachersRv.setAdapter(adapter);

        fetchProfessors();
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
