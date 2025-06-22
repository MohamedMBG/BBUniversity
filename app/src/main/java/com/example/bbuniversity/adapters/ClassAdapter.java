package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bbuniversity.R;
import com.example.bbuniversity.models.ClassInfo;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    public interface OnClassClickListener {
        void onClassClick(ClassInfo info);
    }

    private final List<ClassInfo> classes;
    private final OnClassClickListener listener;

    public ClassAdapter(List<ClassInfo> classes, OnClassClickListener listener) {
        this.classes = classes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClassViewHolder holder, int position) {
        ClassInfo info = classes.get(position);
        holder.bind(info, listener);
    }

    @Override
    public int getItemCount() {
        return classes != null ? classes.size() : 0;
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {
        TextView tvClassName, tvSubject;
        ClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClassName = itemView.findViewById(R.id.tvClassName);
            tvSubject = itemView.findViewById(R.id.tvSubject);
        }
        void bind(ClassInfo info, OnClassClickListener listener) {
            tvClassName.setText(info.getClassName());
            tvSubject.setText(info.getSubject());
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClassClick(info);
            });
        }
    }
}