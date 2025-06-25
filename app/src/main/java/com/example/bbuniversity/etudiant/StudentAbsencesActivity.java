package com.example.bbuniversity.etudiant;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.AbsenceAdapter;
import com.example.bbuniversity.models.Abscence;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentAbsencesActivity extends AppCompatActivity {

    // Reference to Firestore database
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // List containing all absences fetched
    private final List<Abscence> allAbsences = new ArrayList<>();
    // List containing absences matching the current filter
    private final List<Abscence> filteredAbsences = new ArrayList<>();
    // Adapter to bind absence data to RecyclerView
    private AbsenceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable edge-to-edge display
        EdgeToEdge.enable(this);
        // Set the layout for this activity
        setContentView(R.layout.activity_student_absences);
        // Hide navigation bar and enable immersive mode
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // Find UI elements: subject filter input, date filter input, and RecyclerView
        TextInputEditText etSubject = findViewById(R.id.etFilterSubject);
        TextInputEditText etDate = findViewById(R.id.etFilterDate);
        RecyclerView recycler = findViewById(R.id.recyclerAbsences);

        // Initialize the adapter with the filteredAbsences list
        adapter = new AbsenceAdapter(filteredAbsences);
        // Use a vertical LinearLayoutManager for the RecyclerView
        recycler.setLayoutManager(new LinearLayoutManager(this));
        // Attach the adapter to the RecyclerView
        recycler.setAdapter(adapter);

        // Get the currently authenticated user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Load absences for this user
            loadAbsences(user.getUid());
        }

        // Create a TextWatcher to listen for filter input changes
        TextWatcher watcher = new SimpleWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Retrieve current filter values
                String subj = etSubject.getText() != null ? etSubject.getText().toString() : "";
                String date = etDate.getText() != null ? etDate.getText().toString() : "";
                // Apply filtering
                filterAbsences(subj, date);
            }
        };
        // Attach the watcher to both input fields
        etSubject.addTextChangedListener(watcher);
        etDate.addTextChangedListener(watcher);
    }

    /**
     * Loads absences from Firestore for the given user ID.
     */
    private void loadAbsences(String uid) {
        db.collection("users") // Go to "users" collection
                .document(uid)      // Select document matching user ID
                .collection("abscence") // Access "abscence" sub-collection
                .get()
                .addOnSuccessListener(query -> {
                    // Clear previous data
                    allAbsences.clear();
                    // Loop through each document in the result
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        // Convert Firestore document to Abscence object
                        Abscence a = doc.toObject(Abscence.class);
                        if (a != null) allAbsences.add(a);
                    }
                    // After loading, apply current filters (initially none)
                    filterAbsences("", "");
                });
    }

    /**
     * Filters the list of absences by subject and date.
     * @param subject Subject substring to filter on
     * @param dateStr Date string in dd/MM/yyyy format to filter on
     */
    private void filterAbsences(String subject, String dateStr) {
        // Clear the filtered list before applying new filter
        filteredAbsences.clear();
        // Formatter to convert Date to String for comparison
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        for (Abscence a : allAbsences) {
            boolean match = true;
            // If a subject filter is specified, check if the absence's subject contains it
            if (subject != null && !subject.isEmpty()) {
                if (a.getMatiere() == null || !a.getMatiere().toLowerCase().contains(subject.toLowerCase())) {
                    match = false;
                }
            }
            // If still matching and a date filter is specified, check the formatted date
            if (match && dateStr != null && !dateStr.isEmpty()) {
                String f = sdf.format(a.getDate());
                if (!f.contains(dateStr)) match = false;
            }
            // If both checks pass, add to filtered list
            if (match) filteredAbsences.add(a);
        }
        // Notify the adapter to refresh the RecyclerView display
        adapter.notifyDataSetChanged();
    }

    /**
     * Simplified TextWatcher to only override afterTextChanged.
     */
    private abstract static class SimpleWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
}
