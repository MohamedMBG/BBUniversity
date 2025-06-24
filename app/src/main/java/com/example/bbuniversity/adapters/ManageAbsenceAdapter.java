package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Abscence;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ManageAbsenceAdapter extends RecyclerView.Adapter<ManageAbsenceAdapter.AbsenceViewHolder> {

    public interface OnJustifyListener {
        void onJustify(Abscence absence);
    }

    private List<Abscence> absences;
    private final OnJustifyListener listener;

    public ManageAbsenceAdapter(List<Abscence> absences, OnJustifyListener listener) {
        this.absences = absences;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AbsenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.absence_manage_item, parent, false);
        return new AbsenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsenceViewHolder holder, int position) {
        Abscence a = absences.get(position);
        holder.subject.setText(a.getMatiere());
        String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(a.getDate());
        holder.date.setText(date);
        holder.justified.setText(a.isJustifiee() ? "Justifiée" : "Non justifiée");
        if (!a.isJustifiee()) {
            holder.btnJustify.setVisibility(View.VISIBLE);
            holder.btnJustify.setOnClickListener(v -> listener.onJustify(a));
        } else {
            holder.btnJustify.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return absences != null ? absences.size() : 0;
    }

    public void updateData(List<Abscence> list) {
        this.absences = list;
        notifyDataSetChanged();
    }

    static class AbsenceViewHolder extends RecyclerView.ViewHolder {
        TextView subject, date, justified;
        MaterialButton btnJustify;
        AbsenceViewHolder(@NonNull View itemView) {
            super(itemView);
            subject = itemView.findViewById(R.id.abs_subject);
            date = itemView.findViewById(R.id.abs_date);
            justified = itemView.findViewById(R.id.abs_justified);
            btnJustify = itemView.findViewById(R.id.btnJustify);
        }
    }
}