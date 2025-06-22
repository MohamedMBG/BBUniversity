package com.example.bbuniversity.teacher;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.ComplaintAdapter;
import com.example.bbuniversity.models.Complaint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TeacherComplaintsActivity extends AppCompatActivity {

    private final List<Complaint> complaints = new ArrayList<>();
    private ComplaintAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_complaints);

        RecyclerView recycler = findViewById(R.id.recyclerComplaints);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(complaints);
        recycler.setAdapter(adapter);

        loadComplaints();
    }

    private void loadComplaints() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collectionGroup("plaintes")
                .whereEqualTo("professeurId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    complaints.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Complaint c = doc.toObject(Complaint.class);
                        if (c != null) complaints.add(c);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}