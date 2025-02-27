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
        if (exams != null) {
            this.examList = exams;
            notifyDataSetChanged();
        }
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
        if (holder == null || position < 0 || position >= examList.size()) {
            return;
        }

        Exam exam = examList.get(position);
        if (exam == null) {
            return;
        }

        holder.examTitleTextView.setText(exam.getTitle() != null ? exam.getTitle() : "");

        // Display teacher name if available
        String teacherName = exam.getTeacher_name();
        holder.teacherNameTextView.setText("Created by: " + (teacherName != null ? teacherName : "Unknown"));

        // Display questions count
        int questionCount = (exam.getQuestions() != null) ? exam.getQuestions().size() : 0;
        holder.questionsTextView.setText("Questions: " + questionCount);

        // Display duration
        holder.durationTextView.setText("Duration: " + exam.getDuration() + " min");

        try {
            // Format and display start time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String startTime = sdf.format(new Date(exam.getStartTime()));
            holder.startTimeTextView.setText("Starts: " + startTime);

            // Format and display end time
            String endTime = sdf.format(new Date(exam.getEndTime()));
            holder.endTimeTextView.setText("Ends: " + endTime);

            // Set button state based on exam status
            long now = System.currentTimeMillis();
            if (now < exam.getStartTime()) {
                // Exam hasn't started yet
                holder.startExamButton.setText("Upcoming");
                holder.startExamButton.setEnabled(false);
            } else if (now > exam.getEndTime()) {
                // Exam has ended
                holder.startExamButton.setText("Expired");
                holder.startExamButton.setEnabled(false);
            } else {
                // Exam is active
                holder.startExamButton.setText("Start");
                holder.startExamButton.setEnabled(true);
                holder.startExamButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onExamStart(exam);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.startTimeTextView.setText("Start time unavailable");
            holder.endTimeTextView.setText("End time unavailable");
            holder.startExamButton.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return examList != null ? examList.size() : 0;
    }

    public interface OnExamStartListener {
        void onExamStart(Exam exam);
    }

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        TextView examTitleTextView, teacherNameTextView, questionsTextView,
                durationTextView, startTimeTextView, endTimeTextView;
        Button startExamButton;

        ExamViewHolder(View itemView) {
            super(itemView);
            examTitleTextView = itemView.findViewById(R.id.examTitleTextView);
            teacherNameTextView = itemView.findViewById(R.id.teacherNameTextView);
            questionsTextView = itemView.findViewById(R.id.questionsTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            startExamButton = itemView.findViewById(R.id.startExamButton);
        }
    }
}