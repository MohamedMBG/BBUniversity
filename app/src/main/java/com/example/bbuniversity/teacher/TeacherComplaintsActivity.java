package com.example.bbuniversity.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.ComplaintAdapter;
import com.example.bbuniversity.models.Complaint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class TeacherComplaintsActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private ComplaintAdapter adapter;
    private final List<Complaint> complaints = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_complaints);

        recycler = findViewById(R.id.recyclerComplaints);
        if (recycler == null) {
            Log.e("TeacherComplaints", "RecyclerView is null! Check your XML id.");
            finish();
            return;
        }
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(complaints, this::openDetail);
        recycler.setAdapter(adapter);

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current == null) {
            Log.e("TeacherComplaints", "No logged-in user!");
            Toast.makeText(this, "Utilisateur non connecté.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadComplaints();
    }


    private void loadComplaints() {
        String teacherUid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        db.collection("complaints")
                .whereEqualTo("teacherId", teacherUid)
                .orderBy("dateFiled", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    complaints.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Complaint c = doc.toObject(Complaint.class);
                        if (c != null) {
                            // Store the path locally for detail/Edit
                            c.setDocumentPath(doc.getReference().getPath());
                            complaints.add(c);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("TeacherComplaints", "loadComplaints failed", e);
                    Toast.makeText(this,
                            "Erreur lors du chargement : " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void openDetail(@NonNull Complaint complaint) {
        Intent intent = new Intent(this, TeacherComplaintDetailActivity.class);
        intent.putExtra("complaintPath", complaint.getDocumentPath());
        intent.putExtra("noteId",         complaint.getNoteId());
        intent.putExtra("studentId",      complaint.getStudentId());
        intent.putExtra("description",    complaint.getDescription());
        intent.putExtra("initialGrade",   complaint.getInitialGrade());
        intent.putExtra("status",         complaint.getStatus());
        startActivity(intent);
    }
}
