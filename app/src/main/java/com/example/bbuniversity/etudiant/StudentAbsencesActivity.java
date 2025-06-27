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

    // Firestore instance used to retrieve the absences
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Storage for every absence loaded from Firestore
    private final List<Abscence> allAbsences = new ArrayList<>();
    // Storage for only the absences that match the filters
    private final List<Abscence> filteredAbsences = new ArrayList<>();
    // Adapter displayed in the recycler view
    private AbsenceAdapter adapter;

    @Override // entry point when the screen is created
    protected void onCreate(Bundle savedInstanceState) {
        // call parent implementation
        super.onCreate(savedInstanceState);
        // allow drawing behind system bars
        EdgeToEdge.enable(this);
        // display the activity layout
        setContentView(R.layout.activity_student_absences);
        // hide navigation bar for immersive mode
        getWindow().getDecorView()
                .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // reference to the subject filter text field
        TextInputEditText etSubject = findViewById(R.id.etFilterSubject);
        // reference to the date filter text field
        TextInputEditText etDate = findViewById(R.id.etFilterDate);
        // recycler view showing absences
        RecyclerView recycler = findViewById(R.id.recyclerAbsences);

        // adapter driving the recycler view
        adapter = new AbsenceAdapter(filteredAbsences);
        // layout manager arranging items vertically
        recycler.setLayoutManager(new LinearLayoutManager(this));
        // connect adapter to recycler view
        recycler.setAdapter(adapter);

        // fetch current logged in user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // load user absences if available
        if (user != null) loadAbsences(user.getUid());

        // watcher invoked whenever filter inputs change
        TextWatcher watcher = new SimpleWatcher() {
            @Override // we only use afterTextChanged
            public void afterTextChanged(Editable s) {
                // text from subject field or empty
                String subj = etSubject.getText() == null ? "" : etSubject.getText().toString();
                // text from date field or empty
                String date = etDate.getText() == null ? "" : etDate.getText().toString();
                // filter list accordingly
                filterAbsences(subj, date);
            }
        };
        // link watcher to subject field
        etSubject.addTextChangedListener(watcher);
        // link watcher to date field
        etDate.addTextChangedListener(watcher);
    }

    /** Fetch absences from Firestore for a user */
    private void loadAbsences(String uid) {
        // query the "abscence" sub collection of the user
        db.collection("users").document(uid).collection("abscence").get()
                .addOnSuccessListener(q -> {
                    // remove old entries
                    allAbsences.clear();
                    // convert each document into an Abscence object
                    for (DocumentSnapshot d : q.getDocuments()) {
                        Abscence a = d.toObject(Abscence.class);
                        if (a != null) allAbsences.add(a);
                    }
                    // show everything with no filter
                    filterAbsences("", "");
                });
    }

    /** Filter absences by subject and date */
    private void filterAbsences(String subject, String dateStr) {
        // reset the displayed list
        filteredAbsences.clear();
        // formatter used for date comparison
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        for (Abscence a : allAbsences) {
            // check if subject text matches
            boolean matchSubj = subject.isEmpty() ||
                    (a.getMatiere() != null && a.getMatiere().toLowerCase().contains(subject.toLowerCase()));
            // check if date text matches
            boolean matchDate = dateStr.isEmpty() || sdf.format(a.getDate()).contains(dateStr);
            // add when both conditions are true
            if (matchSubj && matchDate) filteredAbsences.add(a);
        }
        // refresh the recycler view
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