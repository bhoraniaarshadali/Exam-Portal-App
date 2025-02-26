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
    private Map<String, String> userAnswers = new HashMap<>(); // Track user answers (questionId -> answer)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elements
        examTitleTextView = findViewById(R.id.examTitleTextView);
        questionsLayout = findViewById(R.id.questionsLayout);
        submitExamButton = findViewById(R.id.submitExamButton);

        // Get exam from intent
        exam = (Exam) getIntent().getSerializableExtra("exam");
        if (exam == null || exam.getId() == null) {
            Toast.makeText(this, "Invalid exam data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load and display exam details and questions
        loadExamDetails();
        loadQuestions();
        setupSubmitButton();
    }

    private void loadExamDetails() {
        examTitleTextView.setText(exam.getTitle());
    }

    private void loadQuestions() {
        db.collection("questions")
                .whereEqualTo("examId", exam.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    questions.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Question question = document.toObject(Question.class);
                        questions.add(question);
                        displayQuestion(question);
                    }
                    if (questions.isEmpty()) {
                        Toast.makeText(this, "No questions found for this exam", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading questions: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void displayQuestion(Question question) {
        View questionView = LayoutInflater.from(this).inflate(R.layout.item_exam_question, questionsLayout, false);

        TextView questionText = questionView.findViewById(R.id.questionText);
        questionText.setText(question.getQuestionText());

        if ("MCQ".equals(question.getType())) {
            LinearLayout optionsLayout = questionView.findViewById(R.id.optionsLayout);
            for (String option : question.getOptions()) {
                Button optionButton = new Button(this);
                optionButton.setText(option);
                optionButton.setOnClickListener(v -> {
                    userAnswers.put(question.getId(), option);
                    Toast.makeText(this, "Selected: " + option, Toast.LENGTH_SHORT).show();
                });
                optionsLayout.addView(optionButton);
            }
        } else if ("subjective".equals(question.getType())) {
            EditText answerEditText = questionView.findViewById(R.id.answerEditText);
            answerEditText.setVisibility(View.VISIBLE);
            answerEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    userAnswers.put(question.getId(), answerEditText.getText().toString().trim());
                }
            });
        } else if ("coding".equals(question.getType())) {
            EditText codeEditText = questionView.findViewById(R.id.codeEditText);
            codeEditText.setVisibility(View.VISIBLE);
            codeEditText.setText(question.getCodeTemplate());
            codeEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    userAnswers.put(question.getId(), codeEditText.getText().toString().trim());
                }
            });
        }

        questionsLayout.addView(questionView);
    }

    private void setupSubmitButton() {
        submitExamButton.setOnClickListener(v -> submitExam());
    }

    private void submitExam() {
        if (userAnswers.isEmpty()) {
            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentId = mAuth.getCurrentUser().getUid();
        Map<String, Object> attemptData = new HashMap<>();
        attemptData.put("student_id", studentId);
        attemptData.put("exam_id", exam.getId());
        attemptData.put("answers", userAnswers);
        attemptData.put("timestamp", System.currentTimeMillis());

        db.collection("student_attempts").add(attemptData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Exam submitted successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error submitting exam: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}