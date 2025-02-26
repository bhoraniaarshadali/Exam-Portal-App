package com.example.exam_portal_app;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private List<Question> questions;
    private OnQuestionClickListener listener;
    private final AppCompatActivity activity;

    public interface OnQuestionClickListener {
        void onQuestionClick(Question question);
    }

    public QuestionAdapter(OnQuestionClickListener listener, AppCompatActivity activity) {
        this.questions = new ArrayList<>();
        this.listener = listener;
        this.activity = activity;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
        notifyDataSetChanged();
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
        notifyItemInserted(questions.size() - 1);
    }

    public void updateQuestion(Question question) {
        int index = questions.indexOf(question);
        if (index >= 0) {
            questions.set(index, question);
            notifyItemChanged(index);
        }
    }

    public void removeQuestion(Question question) {
        int index = questions.indexOf(question);
        if (index >= 0) {
            questions.remove(index);
            notifyItemRemoved(index);
        }
    }

    @Override
    public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(QuestionViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.questionTextView.setText(question.getQuestionText());
        holder.detailsTextView.setText("Type: " + question.getType() + "\n" +
                (question.getOptions() != null ? "Options: " + String.join(", ", question.getOptions()) : "") +
                (question.getCorrectAnswer() != null ? "\nCorrect Answer: " + question.getCorrectAnswer() : "") +
                (question.getCodeTemplate() != null ? "\nCode Template: " + question.getCodeTemplate() : ""));

        holder.editButton.setOnClickListener(v -> listener.onQuestionClick(question));
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(activity)
                    .setTitle("Delete Question")
                    .setMessage("Are you sure you want to delete this question?")
                    .setPositiveButton("Yes", (dialog, which) -> listener.onQuestionClick(question))
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView, detailsTextView;
        Button editButton, deleteButton;

        QuestionViewHolder(View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            detailsTextView = itemView.findViewById(R.id.detailsTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}