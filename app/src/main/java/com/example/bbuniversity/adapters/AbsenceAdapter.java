package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Abscence;

import java.text.SimpleDateFormat;
import java.util.List;

public class AbsenceAdapter extends RecyclerView.Adapter<AbsenceAdapter.AbsenceViewHolder> {
    private List<Abscence> absences;

    public AbsenceAdapter(List<Abscence> absences) {
        this.absences = absences;
    }

    @Override
    public AbsenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.absence_item, parent, false);
        return new AbsenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AbsenceViewHolder holder, int position) {
        Abscence a = absences.get(position);
        holder.subject.setText("Matière : " + a.getMatiere());
        holder.justified.setText("Justifiée : " + (a.isJustifiee() ? "Oui" : "Non"));
        holder.date.setText("Date : " + a.getDate().toString());

        Abscence absence = absences.get(position);

        // Format the date to dd/MM/yyyy
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = dateFormat.format(absence.getDate()); // assuming the date is a Date object

        holder.date.setText(formattedDate);
        holder.subject.setText(absence.getMatiere());
    }

    @Override
    public int getItemCount() {
        return absences.size();
    }

    class AbsenceViewHolder extends RecyclerView.ViewHolder {
        TextView subject, justified, date;

        public AbsenceViewHolder(View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.abs_subject);
            justified = itemView.findViewById(R.id.abs_justified);
            date = itemView.findViewById(R.id.abs_date);

        }
    }
}
