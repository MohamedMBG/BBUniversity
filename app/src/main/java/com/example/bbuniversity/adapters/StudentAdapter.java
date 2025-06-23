package com.example.bbuniversity.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bbuniversity.R;
import com.example.bbuniversity.models.Etudiant;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    public interface OnStudentClickListener {
        void onStudentClick(Etudiant student);
        void onStudentLongClick(Etudiant student, View view);
    }

    private List<Etudiant> students;
    private final OnStudentClickListener listener;

    public StudentAdapter(List<Etudiant> students, OnStudentClickListener listener) {
        this.students = students != null ? students : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        if (position >= 0 && position < students.size()) {
            Etudiant student = students.get(position);
            if (student != null) {
                holder.bind(student, listener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return students != null ? students.size() : 0;
    }

    public void updateList(List<Etudiant> newList) {
        this.students = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView studentName;
        private final TextView studentEmail;
        private final TextView studentClass;
        private final TextView studentMatricule;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            studentEmail = itemView.findViewById(R.id.studentEmail);
            studentClass = itemView.findViewById(R.id.studentClass);
            studentMatricule = itemView.findViewById(R.id.studentMatricule);
        }

        public void bind(@NonNull Etudiant student, @NonNull OnStudentClickListener listener) {
            // Safe text setting with null checks
            String fullName = (student.getPrenom() != null ? student.getPrenom() + " " : "") +
                    (student.getNom() != null ? student.getNom() : "");
            studentName.setText(fullName.trim().isEmpty() ? "No Name" : fullName);

            studentEmail.setText(student.getEmail() != null ? student.getEmail() : "No Email");
            String classDisplay = student.getClasse() != null ? student.getClasse() : student.getClasseCode();
            studentClass.setText(classDisplay != null ? classDisplay : "No Class");
            studentMatricule.setText(student.getMatricule() != null ? student.getMatricule() : "No ID");

            // Safe click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStudentClick(student);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onStudentLongClick(student, itemView);
                    return true;
                }
                return false;
            });
        }
    }
}