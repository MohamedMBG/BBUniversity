package com.example.bbuniversity.admin_panel;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.TimetableEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Écran d'administration permettant de créer l'emploi du temps d'une classe
 */
public class TimetableAdminActivity extends AppCompatActivity {

    // Sélecteur de classe
    private Spinner spinnerClass;
    // Sélecteur de matière
    private Spinner spinnerSubject;
    // Sélecteur de jour
    private Spinner spinnerDay;
    // Champs pour les heures de début et de fin
    private EditText etStart, etEnd;
    // Liste affichant les séances ajoutées
    private ListView listView;
    // Liste interne des entrées
    private final List<TimetableEntry> entries = new ArrayList<>();
    // Adaptateur pour la ListView
    private ArrayAdapter<String> listAdapter;

    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_timetable_admin);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        db = FirebaseFirestore.getInstance();

        // Liaison des vues
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerDay = findViewById(R.id.spinnerDay);
        etStart = findViewById(R.id.etStart);
        etEnd = findViewById(R.id.etEnd);
        listView = findViewById(R.id.listEntries);
        Button btnAdd = findViewById(R.id.btnAddEntry);
        Button btnSave = findViewById(R.id.btnSaveTimetable);

        listAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>());
        listView.setAdapter(listAdapter);

        // Chargement des classes et matières
        loadClasses();
        loadSubjects();

        // Ajout d'une entrée au clic
        btnAdd.setOnClickListener(v -> addEntry());
        // Sauvegarde dans Firestore
        btnSave.setOnClickListener(v -> saveTimetable());
    }

    /** Charge la liste des classes depuis Firestore */
    private void loadClasses() {
        db.collection("classes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> classes = new ArrayList<>();
                for (QueryDocumentSnapshot d : task.getResult()) {
                    String name = d.getString("name");
                    if (name != null) classes.add(name);
                }
                spinnerClass.setAdapter(new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        classes));
            }
        });
    }

    /** Charge la liste des matières depuis Firestore */
    private void loadSubjects() {
        db.collection("matieres").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> matieres = new ArrayList<>();
                for (QueryDocumentSnapshot d : task.getResult()) {
                    String nom = d.getString("nom");
                    if (nom != null) matieres.add(nom);
                }
                spinnerSubject.setAdapter(new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        matieres));
            }
        });
    }

    /** Ajoute une séance à la liste locale et à l'affichage */
    private void addEntry() {
        String day = spinnerDay.getSelectedItem().toString();
        String subject = spinnerSubject.getSelectedItem().toString();
        String start = etStart.getText().toString().trim();
        String end = etEnd.getText().toString().trim();
        if (start.isEmpty() || end.isEmpty()) {
            Toast.makeText(this, "Heures manquantes", Toast.LENGTH_SHORT).show();
            return;
        }
        TimetableEntry entry = new TimetableEntry(day, subject, start, end);
        entries.add(entry);
        listAdapter.add(day + " - " + subject + " " + start + "-" + end);
        etStart.setText("");
        etEnd.setText("");
    }

    /** Enregistre l'emploi du temps de la classe sélectionnée */
    private void saveTimetable() {
        String className = spinnerClass.getSelectedItem().toString();
        List<Map<String, String>> data = new ArrayList<>();
        for (TimetableEntry e : entries) {
            Map<String, String> m = new HashMap<>();
            m.put("day", e.getDay());
            m.put("subject", e.getSubject());
            m.put("start", e.getStart());
            m.put("end", e.getEnd());
            data.add(m);
        }
        Map<String, Object> doc = new HashMap<>();
        doc.put("class", className);
        doc.put("entries", data);
        db.collection("timetables").document(className).set(doc)
                .addOnSuccessListener(r -> {
                    Toast.makeText(this, "Emploi du temps enregistré", Toast.LENGTH_SHORT).show();
                    entries.clear();
                    listAdapter.clear();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}