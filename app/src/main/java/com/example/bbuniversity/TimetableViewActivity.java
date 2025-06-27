package com.example.bbuniversity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.models.Professeur;
import com.example.bbuniversity.models.TimetableEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activité simple pour afficher un emploi du temps
 */
public class TimetableViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_timetable_view);

        ListView list = findViewById(R.id.listTimetable);
        List<Map<String, String>> dataSet = new ArrayList<>();
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                dataSet,
                android.R.layout.simple_list_item_2,
                new String[]{"line1", "line2"},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        list.setAdapter(adapter);

        String className = getIntent().getStringExtra("class");
        String teacherId = getIntent().getStringExtra("teacherId");

        if (className == null && teacherId == null) {
            finish();
            return;
        }

        if (className != null) {
            loadClassTimetable(className, dataSet, adapter);
        }

        if (teacherId != null) {
            loadTeacherTimetable(teacherId, dataSet, adapter);
        }
    }

    private void loadClassTimetable(String className, List<Map<String, String>> dataSet, SimpleAdapter adapter) {
        FirebaseFirestore.getInstance()
                .collection("timetables")
                .document(className)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<Map<String, String>> data = (List<Map<String, String>>) doc.get("entries");
                        if (data != null) {
                            for (Map<String, String> m : data) {
                                Map<String, String> row = new HashMap<>();
                                row.put("line1", m.get("day") + " " + m.get("start") + "-" + m.get("end"));
                                row.put("line2", m.get("subject"));
                                dataSet.add(row);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }
    /** Charge et agrège l'emploi du temps d'un professeur */
    private void loadTeacherTimetable(String teacherId, List<Map<String, String>> dataSet, SimpleAdapter adapter) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(teacherId).get()
                .addOnSuccessListener(doc -> {
                    Professeur prof = doc.toObject(Professeur.class);
                    if (prof == null || prof.getEnseignement() == null) return;

                    for (Map.Entry<String, List<String>> e : prof.getEnseignement().entrySet()) {
                        String subject = e.getKey();
                        for (String c : e.getValue()) {
                            db.collection("timetables")
                                    .document(c)
                                    .get()
                                    .addOnSuccessListener(tDoc -> {
                                        if (!tDoc.exists()) return;
                                        List<Map<String, String>> data = (List<Map<String, String>>) tDoc.get("entries");
                                        if (data == null) return;
                                        for (Map<String, String> m : data) {
                                            String entrySubject = m.get("subject");
                                            if (entrySubject != null && entrySubject.equalsIgnoreCase(subject)) {
                                                Map<String, String> row = new HashMap<>();
                                                row.put("line1", m.get("day") + " " + m.get("start") + "-" + m.get("end"));
                                                row.put("line2", c + " - " + entrySubject);
                                                dataSet.add(row);
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                    });
                        }
                    }
                });
    }
}