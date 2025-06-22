package com.example.bbuniversity.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Complaint;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {

    private final List<Complaint> complaints;

    public ComplaintAdapter(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint c = complaints.get(position);
        holder.message.setText(c.getMessage());
        if (c.getTimestamp() != null) {
            String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(c.getTimestamp().toDate());
            holder.date.setText(date);
        }
    }

    @Override
    public int getItemCount() {
        return complaints != null ? complaints.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, date;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.tvComplaintMessage);
            date = itemView.findViewById(R.id.tvComplaintDate);
        }
    }
}