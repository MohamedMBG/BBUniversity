package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Complaint;
import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * RecyclerView adapter for displaying a list of Complaint objects.
 */
public class ComplaintAdapter
        extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {

    public interface OnComplaintClickListener {
        void onComplaintClick(Complaint complaint);
    }

    private final List<Complaint> complaints;
    private final OnComplaintClickListener listener;

    public ComplaintAdapter(List<Complaint> complaints,
                            OnComplaintClickListener listener) {
        this.complaints = complaints;
        this.listener   = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        Complaint c = complaints.get(pos);

        holder.titleView.setText(c.getTitle());
        holder.statusView.setText(c.getStatus());
        holder.messageView.setText(c.getDescription());

        Timestamp ts = c.getDateFiled();
        if (ts != null) {
            holder.dateView.setText(
                    DateFormat.getDateTimeInstance().format(ts.toDate())
            );
        } else {
            holder.dateView.setText("Date inconnue");
        }

        holder.itemView.setOnClickListener(v -> listener.onComplaintClick(c));
    }

    @Override public int getItemCount() { return complaints.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView titleView;
        final TextView statusView;
        final TextView messageView;
        final TextView dateView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView   = itemView.findViewById(R.id.complaintTitle);
            statusView  = itemView.findViewById(R.id.complaintStatus);
            messageView = itemView.findViewById(R.id.complaintMessage);
            dateView    = itemView.findViewById(R.id.complaintDate);
        }
    }
}
