package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Professeur;

import java.util.List;

public class TeacherAdapter extends RecyclerView.Adapter<TeacherAdapter.TeacherViewHolder> {

    private List<Professeur> profs; // Liste des professeurs

    public TeacherAdapter(List<Professeur> profs) {
        this.profs = profs;
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Création de la vue pour chaque élément
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        // Récupération du professeur courant
        Professeur prof = profs.get(position);

        // Remplissage des champs
        holder.tvFullName.setText(prof.getPrenom() + " " + prof.getNom());
        holder.tvEmail.setText("Email: " + prof.getEmail());
        holder.tvAdresse.setText("Adresse: " + prof.getAdresse());
        holder.tvDepartement.setText("Département: " + prof.getDepartement());

        // Construction de la liste des matières (ancienne version supprimée)
        StringBuilder matieres = new StringBuilder();
        if (prof.getEnseignement() != null) {
            for (String matiere : prof.getEnseignement().keySet()) {
                matieres.append("- ").append(matiere).append("\n");
            }
        } else {
            matieres.append("Aucune matière assignée.");
        }

        holder.tvMatieres.setText(matieres.toString().trim());
    }

    @Override
    public int getItemCount() {
        return profs != null ? profs.size() : 0;
    }

    // Classe interne qui représente chaque case dans la liste
    static class TeacherViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvEmail, tvAdresse, tvDepartement, tvMatieres;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvAdresse = itemView.findViewById(R.id.tvAdresse);
            tvDepartement = itemView.findViewById(R.id.tvDepartement);
            tvMatieres = itemView.findViewById(R.id.tvMatieres);
        }
    }
}
