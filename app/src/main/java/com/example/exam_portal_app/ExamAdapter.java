package com.example.exam_portal_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {

    private List<Exam> examList;
    private OnExamStartListener listener;
    private Context context;

    public ExamAdapter(OnExamStartListener listener) {
        this.examList = new ArrayList<>();
        this.listener = listener;
    }

    public void setExamList(List<Exam> exams) {
        this.examList = exams;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_exam, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        Exam exam = examList.get(position);
        holder.examTitleTextView.setText(exam.getTitle());

        // Display teacher name if available
        String teacherName = exam.getTeacher_name();
        holder.teacherNameTextView.setText("Created by: " + (teacherName != null ? teacherName : "Unknown"));

        // Format questions count
        //holder.questionsTextView.setText(context.getString(R.string.questions_count_format, "Multiple"));

        // Format duration
        //holder.durationTextView.setText(context.getString(R.string.duration_format, exam.getDuration()));

        // Format examinees count (placeholder for now)
        //holder.examineesTextView.setText(context.getString(R.string.examinees_count_format, "--"));

        // Format start time
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String startTime = sdf.format(new Date(exam.getStartTime()));
        holder.startTimeTextView.setText("Starts: " + startTime);

        // Set button state based on exam status
        long now = System.currentTimeMillis();
        if (now < exam.getStartTime()) {
            // Exam hasn't started yet
            //holder.startExamButton.setText(R.string.upcoming);
            holder.startExamButton.setEnabled(false);
        } else if (now > exam.getEndTime()) {
            // Exam has ended
            //holder.startExamButton.setText(R.string.expired);
            holder.startExamButton.setEnabled(false);
        } else {
            // Exam is active
            //holder.startExamButton.setText(R.string.start_exam);
            holder.startExamButton.setEnabled(true);
            holder.startExamButton.setOnClickListener(v -> listener.onExamStart(exam));
        }
    }

    @Override
    public int getItemCount() {
        return examList.size();
    }

    interface OnExamStartListener {
        void onExamStart(Exam exam);
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView examTitleTextView, teacherNameTextView, questionsTextView,
                durationTextView, examineesTextView, startTimeTextView;
        Button startExamButton;

        ExamViewHolder(View itemView) {
            super(itemView);
            examTitleTextView = itemView.findViewById(R.id.examTitleTextView);
            teacherNameTextView = itemView.findViewById(R.id.teacherNameTextView);
            questionsTextView = itemView.findViewById(R.id.questionsTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            examineesTextView = itemView.findViewById(R.id.examineesTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            startExamButton = itemView.findViewById(R.id.startExamButton);
        }
    }
}