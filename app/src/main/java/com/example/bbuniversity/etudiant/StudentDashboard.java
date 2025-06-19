package com.example.bbuniversity.etudiant;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.adapters.AbsenceAdapter;
import com.example.bbuniversity.models.Abscence;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class StudentDashboard extends AppCompatActivity {

    private TextView tvName, tvFiliere, tvMatricule, tvClasse, absencesCountText;
    private RecyclerView absencesRecyclerView;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<Abscence> absenceList = new ArrayList<>();
    private AbsenceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
        EdgeToEdge.enable(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        //declaration des elements de ui
        tvName = findViewById(R.id.FullStdName);
        tvFiliere = findViewById(R.id.stdFiliere);
        tvMatricule = findViewById(R.id.stdMatricule);
        tvClasse = findViewById(R.id.stdClasse);
        absencesCountText = findViewById(R.id.absences_count_text);
        absencesRecyclerView = findViewById(R.id.absences_recycler_view);

        adapter = new AbsenceAdapter(absenceList);
        absencesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        absencesRecyclerView.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            loadUserData(uid);
            loadAbsences(uid);
        } else {
            Toast.makeText(this, "Utilisateur non connecté." , Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(this::updateUI)
                .addOnFailureListener(e -> Toast.makeText(this , "Erreur de chargement utilisateur : " + e.getMessage() , Toast.LENGTH_SHORT).show());
    }

    private void loadAbsences(String uid) {
        db.collection("users").document(uid).collection("abscence").get()
                .addOnSuccessListener(query -> {
                    absencesCountText.setText("Nombre d'absences : " + query.size());
                    absenceList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Abscence a = doc.toObject(Abscence.class);
                        if (a != null) absenceList.add(a);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this , "Erreur de chargement des absences : " + e.getMessage() , Toast.LENGTH_SHORT).show());
    }

    private void updateUI(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Données utilisateur non trouvées." , Toast.LENGTH_SHORT).show();
            return;
        }

        String nom = getSafe(doc, "nom");
        String prenom = getSafe(doc, "prenom");
        String filiere = getSafe(doc, "filiere");
        String matricule = getSafe(doc, "matricule");
        String niveau = getSafe(doc, "niveau");
        String classe = getSafe(doc, "classe");

        tvName.setText(nom + " " + prenom);
        tvFiliere.setText(filiere);
        tvMatricule.setText(matricule);
        tvClasse.setText(niveau + " " + filiere + " " + classe);
    }

    private String getSafe(DocumentSnapshot doc, String key) {
        Object val = doc.get(key);
        return val != null ? val.toString() : "";
    }

}
