package com.example.exam_portal_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private List<Exam> examList;
    private OnExamStartListener listener;

    public ExamAdapter(OnExamStartListener listener) {
        this.examList = new ArrayList<>();
        this.listener = listener;
    }

    public void setExamList(List<Exam> exams) {
        this.examList = exams;
        notifyDataSetChanged();
    }

    @Override
    public ExamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExamViewHolder holder, int position) {
        Exam exam = examList.get(position);
        holder.examTitleTextView.setText(exam.getTitle());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String startTime = sdf.format(exam.getStartTime());
        holder.examTimeTextView.setText("Starts: " + startTime);
        holder.startExamButton.setOnClickListener(v -> listener.onExamStart(exam));
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    interface OnExamStartListener {
        void onExamStart(Exam exam);
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView examTitleTextView, examTimeTextView;
        Button startExamButton;

        ExamViewHolder(View itemView) {
            super(itemView);
            examTitleTextView = itemView.findViewById(R.id.examTitleTextView);
            examTimeTextView = itemView.findViewById(R.id.examTimeTextView);
            startExamButton = itemView.findViewById(R.id.startExamButton);
        }
    }
}