package com.example.exam_portal_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private List<Question> questions;

    public QuestionAdapter(List<Question> questions) {
        this.questions = questions;
    }

    @Override
    public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(QuestionViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.questionTextView.setText(question.getText());
        if ("MCQ".equals(question.getType())) {
            holder.radioGroup.setVisibility(View.VISIBLE);
            String[] options = question.getOptions();
            for (int i = 0; i < options.length; i++) {
                RadioButton radioButton = new RadioButton(holder.itemView.getContext());
                radioButton.setText(options[i]);
                holder.radioGroup.addView(radioButton);
            }
        } else {
            holder.radioGroup.setVisibility(View.GONE);
            // TODO: Handle subjective/coding questions
        }
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView;
        RadioGroup radioGroup;

        QuestionViewHolder(View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            radioGroup = itemView.findViewById(R.id.radioGroup);
        }
    }
}