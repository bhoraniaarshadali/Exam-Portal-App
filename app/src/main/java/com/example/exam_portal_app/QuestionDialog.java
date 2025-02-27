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
    private List<EditText> optionEditTexts;
    private Button saveButton, deleteButton, addOptionButton;
    private LinearLayout optionsContainer;

    public QuestionDialog(@NonNull Context context, Question question, OnQuestionSavedListener listener) {
        super(context);
        this.context = context;
        this.question = question;
        this.listener = listener;
        this.optionEditTexts = new ArrayList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_question);

        initializeViews();
        setupTypeSpinner();
        populateExistingQuestion();
        setupListeners();
    }

    private void initializeViews() {
        questionTextEditText = findViewById(R.id.questionTextEditText);
        typeSpinner = findViewById(R.id.typeSpinner);
        answerEditText = findViewById(R.id.answerEditText);
        codeTemplateEditText = findViewById(R.id.codeTemplateEditText);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        addOptionButton = findViewById(R.id.addOptionButton);
        optionsContainer = findViewById(R.id.optionsContainer);

        if (questionTextEditText == null || typeSpinner == null || answerEditText == null ||
                codeTemplateEditText == null || saveButton == null || deleteButton == null ||
                addOptionButton == null || optionsContainer == null) {
            throw new IllegalStateException("Required views not found in layout");
        }
    }

    private void setupTypeSpinner() {
        try {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                    R.array.question_types, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            typeSpinner.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(context, "Error setting up question types", Toast.LENGTH_SHORT).show();
        }
    }

    private void populateExistingQuestion() {
        if (question != null) {
            questionTextEditText.setText(question.getQuestionText());
            int typeIndex = getIndex(typeSpinner, question.getType());
            typeSpinner.setSelection(typeIndex);

            updateViewsForType(question.getType());

            if ("MCQ".equals(question.getType()) && question.getOptions() != null) {
                showOptions(question.getOptions());
            } else if ("subjective".equals(question.getType())) {
                answerEditText.setText(question.getCorrectAnswer());
            } else if ("coding".equals(question.getType())) {
                codeTemplateEditText.setText(question.getCodeTemplate());
            }

            deleteButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupListeners() {
        typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String type = parent.getItemAtPosition(position).toString();
                updateViewsForType(type);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        addOptionButton.setOnClickListener(v -> addOption(""));
        saveButton.setOnClickListener(v -> saveQuestion());
        deleteButton.setOnClickListener(v -> {
            if (question != null && listener != null) {
                listener.onQuestionDeleted(question);
                dismiss();
            }
        });
    }

    private void updateViewsForType(String type) {
        answerEditText.setVisibility(View.GONE);
        codeTemplateEditText.setVisibility(View.GONE);
        optionsContainer.setVisibility(View.GONE);
        addOptionButton.setVisibility(View.GONE);
        clearOptions();

        switch (type) {
            case "MCQ":
                answerEditText.setVisibility(View.VISIBLE);
                optionsContainer.setVisibility(View.VISIBLE);
                addOptionButton.setVisibility(View.VISIBLE);
                showOptions(new ArrayList<>());
                break;
            case "subjective":
                answerEditText.setVisibility(View.VISIBLE);
                break;
            case "coding":
                codeTemplateEditText.setVisibility(View.VISIBLE);
                break;
        }
    }

    private int getIndex(Spinner spinner, String value) {
        if (value == null) return 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private void showOptions(List<String> options) {
        clearOptions();
        if (options != null) {
            for (String option : options) {
                addOption(option);
            }
        }
    }

    private void addOption(String text) {
        try {
            View optionView = LayoutInflater.from(context).inflate(R.layout.item_option, optionsContainer, false);
            EditText optionEditText = optionView.findViewById(R.id.optionEditText);
            Button removeButton = optionView.findViewById(R.id.removeOptionButton);

            if (text != null) {
                optionEditText.setText(text);
            }

            removeButton.setOnClickListener(v -> {
                optionEditTexts.remove(optionEditText);
                optionsContainer.removeView(optionView);
            });

            optionsContainer.addView(optionView);
            optionEditTexts.add(optionEditText);
        } catch (Exception e) {
            Toast.makeText(context, "Error adding option", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearOptions() {
        optionsContainer.removeAllViews();
        optionEditTexts.clear();
    }

    private void saveQuestion() {
        try {
            String questionText = questionTextEditText.getText().toString().trim();
            String type = typeSpinner.getSelectedItem().toString();
            String answer = answerEditText.getText().toString().trim();
            String codeTemplate = codeTemplateEditText.getText().toString().trim();

            if (questionText.isEmpty()) {
                Toast.makeText(context, "Question text is required", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> options = new ArrayList<>();
            if (type.equals("MCQ")) {
                for (EditText editText : optionEditTexts) {
                    String option = editText.getText().toString().trim();
                    if (!option.isEmpty()) {
                        options.add(option);
                    }
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
                    question != null ? question.getExamId() : null
            );

            if (listener != null) {
                listener.onQuestionSaved(newQuestion);
            }
            dismiss();
        } catch (Exception e) {
            Toast.makeText(context, "Error saving question", Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnQuestionSavedListener {
        void onQuestionSaved(Question question);

        void onQuestionDeleted(Question question);
    }
}