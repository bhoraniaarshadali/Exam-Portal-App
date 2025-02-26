package com.example.exam_portal_app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class QuestionDialog extends Dialog {

    private final Context context;
    private final Question question;
    private final OnQuestionSavedListener listener;
    private EditText questionTextEditText, answerEditText, codeTemplateEditText;
    private Spinner typeSpinner;
    private List<EditText> optionEditTexts = new ArrayList<>();
    private Button saveButton, deleteButton, addOptionButton;
    private LinearLayout optionsContainer; // Explicitly typed as LinearLayout

    public interface OnQuestionSavedListener {
        void onQuestionSaved(Question question);
        void onQuestionDeleted(Question question);
    }

    public QuestionDialog(@NonNull Context context, Question question, OnQuestionSavedListener listener) {
        super(context);
        this.context = context;
        this.question = question;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_question); // Ensure this layout exists

        // Initialize UI elements
        questionTextEditText = findViewById(R.id.questionTextEditText);
        typeSpinner = findViewById(R.id.typeSpinner);
        answerEditText = findViewById(R.id.answerEditText);
        codeTemplateEditText = findViewById(R.id.codeTemplateEditText);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        addOptionButton = findViewById(R.id.addOptionButton);
        optionsContainer = findViewById(R.id.optionsContainer); // Cast to LinearLayout

        // Set up type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.question_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);

        // Populate fields if editing existing question
        if (question != null) {
            questionTextEditText.setText(question.getQuestionText());
            typeSpinner.setSelection(getIndex(typeSpinner, question.getType()));
            if ("MCQ".equals(question.getType())) {
                answerEditText.setVisibility(View.VISIBLE);
                optionsContainer.setVisibility(View.VISIBLE);
                showOptions(question.getOptions());
            } else if ("subjective".equals(question.getType())) {
                answerEditText.setVisibility(View.VISIBLE);
                answerEditText.setText(question.getCorrectAnswer());
            } else if ("coding".equals(question.getType())) {
                codeTemplateEditText.setVisibility(View.VISIBLE);
                codeTemplateEditText.setText(question.getCodeTemplate());
            }
            deleteButton.setVisibility(View.VISIBLE);
        }

        // Toggle visibility based on question type
        typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String type = parent.getItemAtPosition(position).toString();
                answerEditText.setVisibility(View.GONE);
                codeTemplateEditText.setVisibility(View.GONE);
                optionsContainer.setVisibility(View.GONE);
                clearOptions();

                if (type.equals("MCQ")) {
                    answerEditText.setVisibility(View.VISIBLE);
                    optionsContainer.setVisibility(View.VISIBLE);
                    showOptions(new ArrayList<>());
                } else if (type.equals("subjective")) {
                    answerEditText.setVisibility(View.VISIBLE);
                } else if (type.equals("coding")) {
                    codeTemplateEditText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        addOptionButton.setOnClickListener(v -> addOption());

        saveButton.setOnClickListener(v -> saveQuestion());
        deleteButton.setOnClickListener(v -> {
            if (question != null) {
                listener.onQuestionDeleted(question);
                dismiss();
            }
        });
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private void showOptions(List<String> options) {
        clearOptions();
        for (String option : options) {
            addOption(option);
        }
    }

    private void addOption(String text) {
        View optionView = LayoutInflater.from(context).inflate(R.layout.item_option, optionsContainer, false);
        EditText optionEditText = optionView.findViewById(R.id.optionEditText);
        if (text != null) optionEditText.setText(text);
        Button removeButton = optionView.findViewById(R.id.removeOptionButton);
        removeButton.setOnClickListener(v -> {
            optionEditTexts.remove(optionEditText);
            ((LinearLayout) optionView.getParent()).removeView(optionView); // Cast parent to LinearLayout
        });
        optionsContainer.addView(optionView); // Use LinearLayout's addView
        optionEditTexts.add(optionEditText);
    }

    private void addOption() {
        addOption("");
    }

    private void clearOptions() {
        if (optionsContainer != null) {
            optionsContainer.removeAllViews(); // Use LinearLayout's removeAllViews
        }
        optionEditTexts.clear();
    }

    private void saveQuestion() {
        String questionText = questionTextEditText.getText().toString().trim();
        String type = typeSpinner.getSelectedItem().toString();
        List<String> options = new ArrayList<>();
        String answer = answerEditText.getText().toString().trim();
        String codeTemplate = codeTemplateEditText.getText().toString().trim();

        if (questionText.isEmpty()) {
            Toast.makeText(context, "Question text is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (type.equals("MCQ")) {
            for (EditText editText : optionEditTexts) {
                String option = editText.getText().toString().trim();
                if (!option.isEmpty()) options.add(option);
            }
            if (options.size() < 2 || answer.isEmpty()) {
                Toast.makeText(context, "At least 2 options and an answer are required for MCQ", Toast.LENGTH_SHORT).show();
                return;
            }
        } else if (type.equals("subjective") && answer.isEmpty()) {
            Toast.makeText(context, "Answer is required for subjective questions", Toast.LENGTH_SHORT).show();
            return;
        } else if (type.equals("coding") && codeTemplate.isEmpty()) {
            Toast.makeText(context, "Code template is required for coding questions", Toast.LENGTH_SHORT).show();
            return;
        }

        Question newQuestion = new Question(
                question != null ? question.getId() : null,
                questionText,
                type,
                type.equals("MCQ") ? options : null,
                type.equals("MCQ") || type.equals("subjective") ? answer : null,
                type.equals("coding") ? codeTemplate : null,
                question != null ? question.getExamId() : ""
        );
        listener.onQuestionSaved(newQuestion);
        dismiss();
    }
}