package com.example.exam_portal_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamActivity extends AppCompatActivity {

    private TextView examTitleTextView;
    private LinearLayout questionsLayout;
    private Button submitExamButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Exam exam;
    private List<Question> questions = new ArrayList<>();
    private Map<String, String> userAnswers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        initializeFirebase();
        initializeViews();
        getExamFromIntent();
        loadExamDetails();
        loadQuestions();
        setupSubmitButton();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        examTitleTextView = findViewById(R.id.examTitleTextView);
        questionsLayout = findViewById(R.id.questionsLayout);
        submitExamButton = findViewById(R.id.submitExamButton);
    }

    private void getExamFromIntent() {
        exam = (Exam) getIntent().getSerializableExtra("exam");
        if (exam == null || exam.getId() == null) {
            Toast.makeText(this, "Invalid exam data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadExamDetails() {
        if (exam != null) {
            examTitleTextView.setText(exam.getTitle());
        }
    }

    private void loadQuestions() {
        if (exam == null) return;

        db.collection("questions")
                .whereEqualTo("examId", exam.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    questions.clear();
                    questionsLayout.removeAllViews();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Question question = document.toObject(Question.class);
                        question.setId(document.getId());
                        questions.add(question);
                        displayQuestion(question);
                    }

                    if (questions.isEmpty()) {
                        showEmptyQuestionsMessage();
                    }
                })
                .addOnFailureListener(e -> showErrorMessage("Error loading questions: " + e.getMessage()));
    }

    private void showEmptyQuestionsMessage() {
        TextView messageView = new TextView(this);
        messageView.setText("No questions found for this exam");
        messageView.setPadding(16, 16, 16, 16);
        questionsLayout.addView(messageView);
        Toast.makeText(this, "No questions found for this exam", Toast.LENGTH_SHORT).show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void displayQuestion(Question question) {
        if (question == null) return;

        View questionView = LayoutInflater.from(this).inflate(R.layout.item_exam_question, questionsLayout, false);

        TextView questionText = questionView.findViewById(R.id.questionText);
        questionText.setText(question.getQuestionText());

        LinearLayout optionsLayout = questionView.findViewById(R.id.optionsLayout);
        EditText answerEditText = questionView.findViewById(R.id.answerEditText);
        EditText codeEditText = questionView.findViewById(R.id.codeEditText);

        switch (question.getType()) {
            case "MCQ":
                setupMCQQuestion(question, optionsLayout);
                break;
            case "subjective":
                setupSubjectiveQuestion(question, answerEditText);
                break;
            case "coding":
                setupCodingQuestion(question, codeEditText);
                break;
        }

        questionsLayout.addView(questionView);
    }

    private void setupMCQQuestion(Question question, LinearLayout optionsLayout) {
        if (question.getOptions() == null) return;

        for (String option : question.getOptions()) {
            Button optionButton = new Button(this);
            optionButton.setText(option);
            optionButton.setOnClickListener(v -> {
                userAnswers.put(question.getId(), option);
                Toast.makeText(this, "Selected: " + option, Toast.LENGTH_SHORT).show();
            });
            optionsLayout.addView(optionButton);
        }
    }

    private void setupSubjectiveQuestion(Question question, EditText answerEditText) {
        answerEditText.setVisibility(View.VISIBLE);
        answerEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String answer = answerEditText.getText().toString().trim();
                if (!answer.isEmpty()) {
                    userAnswers.put(question.getId(), answer);
                }
            }
        });
    }

    private void setupCodingQuestion(Question question, EditText codeEditText) {
        codeEditText.setVisibility(View.VISIBLE);
        codeEditText.setText(question.getCodeTemplate());
        codeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String code = codeEditText.getText().toString().trim();
                if (!code.isEmpty()) {
                    userAnswers.put(question.getId(), code);
                }
            }
        });
    }

    private void setupSubmitButton() {
        submitExamButton.setOnClickListener(v -> submitExam());
    }

    private void submitExam() {
        if (mAuth.getCurrentUser() == null) {
            showErrorMessage("User not authenticated");
            return;
        }

        if (userAnswers.size() < questions.size()) {
            showErrorMessage("Please answer all questions before submitting");
            return;
        }

        Map<String, Object> attemptData = new HashMap<>();
        attemptData.put("student_id", mAuth.getCurrentUser().getUid());
        attemptData.put("exam_id", exam.getId());
        attemptData.put("answers", userAnswers);
        attemptData.put("timestamp", System.currentTimeMillis());

        db.collection("student_attempts")
                .add(attemptData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Exam submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> showErrorMessage("Error submitting exam: " + e.getMessage()));
    }
}