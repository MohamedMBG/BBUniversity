package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Note;

import java.util.List;
import java.util.Locale;

/**
 * Adaptateur pour afficher les notes de l'\u00e9tudiant et permettre une r\u00e9clamation.
 */
public class StudentNoteAdapter extends RecyclerView.Adapter<StudentNoteAdapter.NoteViewHolder> {
    /** Interface de rappel pour g\u00e9rer le clic sur le bouton de plainte */
    public interface OnComplainListener {
        void onComplain(Note note);
    }

    private List<Note> notes;                     // liste des notes \u00e0 afficher
    private final OnComplainListener listener;    // callback pour les plaintes

    /** Constructeur */
    public StudentNoteAdapter(List<Note> notes, OnComplainListener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Cr\u00e9ation de la vue d'un \u00e9l\u00e9ment de note
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        // R\u00e9cup\u00e9ration de la note correspondante
        Note note = notes.get(position);
        // Affichage des valeurs dans les champs
        holder.textMatiere.setText(note.getMatiere());
        holder.textControle.setText(String.format(Locale.FRANCE, "Contr\u00f4le : %.1f", note.getControle()));
        holder.textExamen.setText(String.format(Locale.FRANCE, "Examen : %.1f", note.getExamenFinal()));
        holder.textParticipation.setText(String.format(Locale.FRANCE, "Participation : %.1f", note.getParticipation()));
        holder.textMoyenne.setText(String.format(Locale.FRANCE, "Moyenne : %.1f", note.getNoteGenerale()));
        // Gestion du clic sur le bouton de r\u00e9clamation
        holder.btnComplain.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComplain(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    /** Mise \u00e0 jour des donn\u00e9es */
    public void updateNotes(List<Note> newNotes) {
        this.notes = newNotes;
        notifyDataSetChanged();
    }

    /** ViewHolder contenant les vues d'une note */
    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textMatiere, textControle, textExamen, textParticipation, textMoyenne;
        ImageButton btnComplain;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            // R\u00e9f\u00e9rence des widgets de l'item
            textMatiere = itemView.findViewById(R.id.textMatiere);
            textControle = itemView.findViewById(R.id.textControle);
            textExamen = itemView.findViewById(R.id.textExamen);
            textParticipation = itemView.findViewById(R.id.textParticipation);
            textMoyenne = itemView.findViewById(R.id.textMoyenne);
            btnComplain = itemView.findViewById(R.id.btnComplain);
        }
    }
}
