// Déclaration du package pour cette activité
package com.example.bbuniversity;

// Importations des classes nécessaires
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bbuniversity.models.Professeur;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableViewActivity extends AppCompatActivity {

    // Méthode appelée lors de la création de l'activité
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_timetable_view);

        ListView list = findViewById(R.id.listTimetable);
        // Crée une liste pour stocker les données de l'emploi du temps
        List<Map<String, String>> dataSet = new ArrayList<>();
        // Crée un adaptateur pour afficher les données dans la ListView
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                dataSet,
                android.R.layout.simple_list_item_2,
                new String[]{"line1", "line2"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        // Associe l'adaptateur à la ListView
        list.setAdapter(adapter);
        // Récupère les paramètres passés à l'activité
        String className = getIntent().getStringExtra("class");
        String teacherId = getIntent().getStringExtra("teacherId");
        // Si aucun paramètre n'est fourni, termine l'activité
        if (className == null && teacherId == null) {
            finish();
            return;
        }
        // Si un nom de classe est fourni, charge son emploi du temps
        if (className != null) {
            loadClassTimetable(className, dataSet, adapter);
        }
        // Si un ID de professeur est fourni, charge son emploi du temps
        if (teacherId != null) {
            loadTeacherTimetable(teacherId, dataSet, adapter);
        }
    }
    // Méthode pour charger l'emploi du temps d'une classe
    private void loadClassTimetable(String className, List<Map<String, String>> dataSet, SimpleAdapter adapter) {
        // Accède à la base de données Firestore
        FirebaseFirestore.getInstance()
                .collection("timetables")
                .document(className)
                .get()
                .addOnSuccessListener(doc -> {
                    // Si le document existe
                    if (doc.exists()) {
                        // Récupère les entrées d'emploi du temps
                        List<Map<String, String>> data = (List<Map<String, String>>) doc.get("entries");
                        if (data != null) {
                            // Pour chaque entrée, crée un élément à afficher
                            for (Map<String, String> m : data) {
                                Map<String, String> row = new HashMap<>();
                                // Formatte le jour et l'heure (ex: "Lundi 08:00-10:00")
                                row.put("line1", m.get("day") + " " + m.get("start") + "-" + m.get("end"));
                                // Ajoute le nom de la matière
                                row.put("line2", m.get("subject"));
                                dataSet.add(row);
                            }
                            // Notifie l'adaptateur que les données ont changé
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    /**
     * Charge et agrège l'emploi du temps d'un professeur
     * @param teacherId ID du professeur
     * @param dataSet Liste des données à afficher
     * @param adapter Adaptateur pour la ListView
     */
    private void loadTeacherTimetable(String teacherId, List<Map<String, String>> dataSet, SimpleAdapter adapter) {
        // Accède à la base de données Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Récupère les informations du professeur
        db.collection("users").document(teacherId).get()
                .addOnSuccessListener(doc -> {
                    // Convertit le document en objet Professeur
                    Professeur prof = doc.toObject(Professeur.class);
                    // Vérifie que le professeur et ses enseignements existent
                    if (prof == null || prof.getEnseignement() == null) return;

                    // Parcourt chaque matière enseignée par le professeur
                    for (Map.Entry<String, List<String>> e : prof.getEnseignement().entrySet()) {
                        String subject = e.getKey();
                        // Pour chaque classe où il enseigne cette matière
                        for (String c : e.getValue()) {
                            // Récupère l'emploi du temps de la classe
                            db.collection("timetables")
                                    .document(c)
                                    .get()
                                    .addOnSuccessListener(tDoc -> {
                                        if (!tDoc.exists()) return;
                                        // Récupère les entrées d'emploi du temps
                                        List<Map<String, String>> data = (List<Map<String, String>>) tDoc.get("entries");
                                        if (data == null) return;
                                        // Pour chaque entrée
                                        for (Map<String, String> m : data) {
                                            String entrySubject = m.get("subject");
                                            // Si l'entrée correspond à la matière enseignée
                                            if (entrySubject != null && entrySubject.equalsIgnoreCase(subject)) {
                                                Map<String, String> row = new HashMap<>();
                                                // Formatte le jour et l'heure
                                                row.put("line1", m.get("day") + " " + m.get("start") + "-" + m.get("end"));
                                                // Ajoute la classe et la matière
                                                row.put("line2", c + " - " + entrySubject);
                                                dataSet.add(row);
                                            }
                                        }
                                        // Notifie l'adaptateur que les données ont changé
                                        adapter.notifyDataSetChanged();
                                    });
                        }
                    }
                });
    }
}