package com.example.bbuniversity.teacher;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bbuniversity.R;
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

        complaintPath = getIntent().getStringExtra("path");
        notePath = getIntent().getStringExtra("notePath");
        String message = getIntent().getStringExtra("message");

        etNewGrade = findViewById(R.id.etNewGrade);
        tvMessage = findViewById(R.id.tvComplaintMessage);
        tvMessage.setText(message);

        Button accept = findViewById(R.id.btnAcceptComplaint);
        Button reject = findViewById(R.id.btnRejectComplaint);

        accept.setOnClickListener(v -> acceptComplaint());
        reject.setOnClickListener(v -> rejectComplaint());
    }

    private void acceptComplaint() {
        String gradeStr = etNewGrade.getText().toString().trim();
        if (gradeStr.isEmpty()) {
            Toast.makeText(this, "Enter new grade", Toast.LENGTH_SHORT).show();
            return;
        }
        double grade = Double.parseDouble(gradeStr);
        if (complaintPath == null || notePath == null) return;
        DocumentReference compRef = FirebaseFirestore.getInstance().document(complaintPath);
        DocumentReference noteRef = FirebaseFirestore.getInstance().document(notePath);

        noteRef.update("noteGenerale", grade);
        compRef.update("status", "accepted", "response", "Grade updated to " + grade)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Complaint accepted", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void rejectComplaint() {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Reason")
                .setView(input)
                .setPositiveButton("Submit", (d, w) -> {
                    String reason = input.getText().toString().trim();
                    if (complaintPath == null) return;
                    FirebaseFirestore.getInstance().document(complaintPath)
                            .update("status", "rejected", "response", reason)
                            .addOnSuccessListener(v2 -> {
                                Toast.makeText(this, "Complaint rejected", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
