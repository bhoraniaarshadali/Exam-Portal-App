package com.example.exam_portal_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private List<Question> questions;

    public QuestionAdapter(List<Question> questions) {
        this.questions = questions;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Question question = questions.get(position);
        holder.questionTextView.setText((position + 1) + ". " + question.getText());

        // Clear previous views
        holder.radioGroup.removeAllViews();

        if ("MCQ".equals(question.getType())) {
            holder.radioGroup.setVisibility(View.VISIBLE);
            holder.subjectiveAnswerEditText.setVisibility(View.GONE);

            String[] options = question.getOptions();
            if (options != null) {
                for (int i = 0; i < options.length; i++) {
                    RadioButton radioButton = new RadioButton(holder.itemView.getContext());
                    radioButton.setId(View.generateViewId());
                    radioButton.setText(options[i]);
                    holder.radioGroup.addView(radioButton);

                    // Check if this option was previously selected
                    if (options[i].equals(question.getUserAnswer())) {
                        radioButton.setChecked(true);
                    }
                }
            }

            // Set listener for radio button selection
            holder.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton selectedButton = holder.itemView.findViewById(checkedId);
                if (selectedButton != null) {
                    question.setUserAnswer(selectedButton.getText().toString());
                }
            });
        } else if ("subjective".equals(question.getType())) {
            holder.radioGroup.setVisibility(View.GONE);
            holder.subjectiveAnswerEditText.setVisibility(View.VISIBLE);

            // Set previous answer if exists
            holder.subjectiveAnswerEditText.setText(question.getUserAnswer());

            // Set listener for text changes
            holder.subjectiveAnswerEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    question.setUserAnswer(holder.subjectiveAnswerEditText.getText().toString());
                }
            });
        } else {
            // Default to MCQ if type is not recognized
            holder.radioGroup.setVisibility(View.VISIBLE);
            holder.subjectiveAnswerEditText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView;
        RadioGroup radioGroup;
        EditText subjectiveAnswerEditText;

        QuestionViewHolder(View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            subjectiveAnswerEditText = itemView.findViewById(R.id.subjectiveAnswerEditText);
        }
    }
}