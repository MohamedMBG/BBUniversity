package com.example.bbuniversity.teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class TeacherComplaintDetailActivity extends AppCompatActivity {

    private String complaintPath;
    private String notePath;

    private EditText etNewGrade;
    private TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_complaint_detail);
        EdgeToEdge.enable(this);
        getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );

        // 1) Read the extras with the SAME keys you used when launching
        complaintPath = getIntent().getStringExtra("complaintPath");
        notePath      = getIntent().getStringExtra("noteId");
        String message= getIntent().getStringExtra("description");

        // 2) Bind views
        etNewGrade = findViewById(R.id.etNewGrade);
        tvMessage  = findViewById(R.id.tvComplaintMessage);
        tvMessage.setText(message != null ? message : "");

        Button accept = findViewById(R.id.btnAcceptComplaint);
        Button reject = findViewById(R.id.btnRejectComplaint);

        accept.setOnClickListener(v -> acceptComplaint());
        reject.setOnClickListener(v -> rejectComplaint());
    }

    private void acceptComplaint() {
        String gradeStr = etNewGrade.getText().toString().trim();
        if (gradeStr.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer la nouvelle note", Toast.LENGTH_SHORT).show();
            return;
        }

        double grade;
        try {
            grade = Double.parseDouble(gradeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Note invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        if (complaintPath == null || notePath == null) {
            Toast.makeText(this, "Données manquantes", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference compRef = FirebaseFirestore.getInstance().document(complaintPath);
        DocumentReference noteRef = FirebaseFirestore.getInstance().document(notePath);

        // 3) Update the student’s grade
        noteRef.update("noteGenerale", grade)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur mise à jour note : " + e.getMessage(), Toast.LENGTH_LONG).show()
                );

        // 4) Update complaint: status, response, and dateProcessed
        compRef.update(
                "status", "accepted", "modifiedGrade" , grade ,
                "response", "Note mise à jour à " + grade,
                "dateProcessed", Timestamp.now()
        ).addOnSuccessListener(v -> {
            Toast.makeText(this, "Plainte acceptée", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Erreur mise à jour plainte : " + e.getMessage(), Toast.LENGTH_LONG).show()
        );
    }

    private void rejectComplaint() {
        if (complaintPath == null) {
            Toast.makeText(this, "Données manquantes", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Raison du refus")
                .setView(input)
                .setPositiveButton("Envoyer", (d, w) -> {
                    String reason = input.getText().toString().trim();
                    if (reason.isEmpty()) {
                        Toast.makeText(this, "Veuillez entrer une raison", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentReference compRef = FirebaseFirestore.getInstance().document(complaintPath);
                    compRef.update(
                            "status", "rejected",
                            "response", reason,
                            "dateProcessed", Timestamp.now()
                    ).addOnSuccessListener(v2 -> {
                        Toast.makeText(this, "Plainte refusée", Toast.LENGTH_SHORT).show();
                        finish();
                    }).addOnFailureListener(e2 ->
                            Toast.makeText(this, "Erreur mise à jour plainte : " + e2.getMessage(), Toast.LENGTH_LONG).show()
                    );
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}