package com.example.studentlabgroupmanagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private final List<Student> studentListFull;
    private final List<Student> studentListFiltered;

    public StudentAdapter(List<Student> studentList) {
        this.studentListFull = new ArrayList<>(studentList);
        this.studentListFiltered = studentList;
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
        Student student = studentListFiltered.get(position);
        holder.tvName.setText(student.getName());
        holder.tvNumber.setText("Student No: " + student.getStudentNumber());
    }

    @Override
    public int getItemCount() {
        return studentListFiltered.size();
    }

    // Filter method for search bar
    public void filter(String text) {
        studentListFiltered.clear();
        if (text.isEmpty()) {
            studentListFiltered.addAll(studentListFull);
        } else {
            String query = text.toLowerCase().trim();
            for (Student student : studentListFull) {
                if (student.getName().toLowerCase().contains(query) ||
                        student.getStudentNumber().toLowerCase().contains(query)) {
                    studentListFiltered.add(student);
                }
            }
        }
        notifyDataSetChanged();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvNumber = itemView.findViewById(R.id.tvStudentNumber);
        }
    }
}