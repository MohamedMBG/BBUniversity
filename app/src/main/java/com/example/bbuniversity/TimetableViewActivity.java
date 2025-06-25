package com.example.bbuniversity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bbuniversity.models.TimetableEntry;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>());
        list.setAdapter(adapter);

        String className = getIntent().getStringExtra("class");
        if (className == null) {
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("timetables")
                .document(className)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<Map<String, String>> data = (List<Map<String, String>>) doc.get("entries");
                        if (data != null) {
                            for (Map<String, String> m : data) {
                                adapter.add(m.get("day") + " - " + m.get("subject") + " " + m.get("start") + "-" + m.get("end"));
                            }
                        }
                    }
                });
    }
}