package com.example.bbuniversity.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Note;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> listeNotes;
    private SimpleDateFormat dateFormat;
    private FirebaseFirestore db;
    private Map<String, String> studentNamesCache = new HashMap<>();

    public NoteAdapter(List<Note> listeNotes, FirebaseFirestore db) {
        this.listeNotes = listeNotes;
        this.db = db;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRANCE);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = listeNotes.get(position);

        // 1. Display note data (existing code)
        String matiere = note.getMatiere() != null ?
                formatMatiereId(note.getMatiere()) : "Matière inconnue";
        holder.textMatiere.setText("Matière: " + matiere);
        holder.textControle.setText(String.format(Locale.FRANCE, "Contrôle continu: %.1f", note.getControle()));
        holder.textExamen.setText(String.format(Locale.FRANCE, "Examen: %.1f", note.getExamenFinal()));
        holder.textParticipation.setText(String.format(Locale.FRANCE, "Participation: %.1f", note.getParticipation()));
        holder.textMoyenne.setText(String.format(Locale.FRANCE, "Moyenne: %.1f", note.getNoteGenerale()));

        // 2. Fetch student name (new code)
        String documentPath = note.getDocumentPath(); // You need to store this in Note.java
        if (documentPath != null) {
            String[] parts = documentPath.split("/");
            if (parts.length >= 2) {
                String studentUid = parts[1]; // Extracts UID from path like "users/{uid}/notes/{noteId}"

                db.collection("users").document(studentUid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String studentName = documentSnapshot.getString("nom") + " " + documentSnapshot.getString("prenom");
                                holder.textEtudiant.setText(studentName); // Add this TextView to your item_note.xml
                            }
                        })
                        .addOnFailureListener(e -> Log.e("NoteAdapter", "Error fetching student name", e));
            }
        }
    }

    @Override
    public int getItemCount() {
        return listeNotes != null ? listeNotes.size() : 0;
    }

    public void mettreAJourListe(List<Note> nouvellesNotes) {
        this.listeNotes = nouvellesNotes;
        notifyDataSetChanged();
    }

    private String formatMatiereId(String matiereId) {
        if (matiereId == null) return "Matière inconnue";

        switch (matiereId) {
            case "math_101": return "Mathématiques";
            case "phys_101": return "Physique";
            case "info_101": return "Informatique";
            default: return matiereId;
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textMatiere, textControle, textExamen, textParticipation,
                textMoyenne, textDate, textAbsences,textEtudiant;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textMatiere = itemView.findViewById(R.id.textMatiere);
            textControle = itemView.findViewById(R.id.textControle);
            textExamen = itemView.findViewById(R.id.textExamen);
            textParticipation = itemView.findViewById(R.id.textParticipation);
            textMoyenne = itemView.findViewById(R.id.textMoyenne);
            textDate = itemView.findViewById(R.id.textDate);
            textEtudiant = itemView.findViewById(R.id.textEtudiant); // Add this to your layout
        }
    }
}