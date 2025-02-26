package com.example.exam_portal_app;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamActivity extends AppCompatActivity {

    private static final String TAG = "ExamActivity";

    private TextView examTitleTextView, timeRemainingTextView;
    private RecyclerView questionsRecyclerView;
    private Button submitExamButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CountDownTimer countDownTimer;
    private Exam exam;
    private List<Question> questions = new ArrayList<>();
    private QuestionAdapter questionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elements
        examTitleTextView = findViewById(R.id.examTitleTextView);
        timeRemainingTextView = findViewById(R.id.timeRemainingTextView);
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView);
        submitExamButton = findViewById(R.id.submitExamButton);

        // Get exam data from intent
        exam = (Exam) getIntent().getSerializableExtra("exam");
        if (exam == null) {
            Toast.makeText(this, "Exam data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        examTitleTextView.setText(exam.getTitle());
        startTimer(exam.getDuration() * 60 * 1000); // Convert minutes to milliseconds

        // Set up RecyclerView for questions
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionAdapter = new QuestionAdapter(questions);
        questionsRecyclerView.setAdapter(questionAdapter);

        loadQuestions();

        submitExamButton.setOnClickListener(v -> submitExam());
    }

    private void loadQuestions() {
        db.collection("exams").document(exam.getId())
                .collection("questions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Fallback to sample questions if none exist in the database
                        addSampleQuestions();
                    } else {
                        questions.clear();
                        for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                            Question question = queryDocumentSnapshots.getDocuments().get(i).toObject(Question.class);
                            questions.add(question);
                        }
                    }
                    questionAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading questions: " + e.getMessage());
                    addSampleQuestions();
                });
    }

    private void addSampleQuestions() {
        // Sample questions as fallback
        questions.clear();
        questions.add(new Question("1", "What is 2 + 2?", "MCQ", new String[]{"3", "4", "5", "6"}, "4"));
        questions.add(new Question("2", "What is the capital of France?", "MCQ", new String[]{"London", "Berlin", "Paris", "Madrid"}, "Paris"));
        questions.add(new Question("3", "Which of these is a programming language?", "MCQ", new String[]{"HTML", "CSS", "Java", "All of the above"}, "All of the above"));
        questionAdapter.notifyDataSetChanged();
    }

    private void startTimer(long millisInFuture) {
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 1000 / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timeRemainingTextView.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                timeRemainingTextView.setText("Time's up!");
                submitExam();
            }
        }.start();
    }

    private void submitExam() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Calculate score for MCQ questions
        int correctAnswers = 0;
        int totalQuestions = 0;

        for (Question question : questions) {
            if ("MCQ".equals(question.getType())) {
                totalQuestions++;
                if (question.isCorrect()) {
                    correctAnswers++;
                }
            }
        }

        double score = totalQuestions > 0 ? (correctAnswers * 100.0 / totalQuestions) : 0;

        // Save submission to Firestore
        Map<String, Object> submission = new HashMap<>();
        submission.put("exam_id", exam.getId());
        submission.put("user_id", user.getUid());
        submission.put("timestamp", System.currentTimeMillis());
        submission.put("score", score);
        submission.put("correct_answers", correctAnswers);
        submission.put("total_questions", totalQuestions);

        db.collection("submissions").add(submission)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Exam submitted successfully!", Toast.LENGTH_SHORT).show();
                    // Save each answer
                    for (int i = 0; i < questions.size(); i++) {
                        Question question = questions.get(i);
                        Map<String, Object> answerData = new HashMap<>();
                        answerData.put("question_id", question.getId());
                        answerData.put("user_answer", question.getUserAnswer());
                        answerData.put("is_correct", question.isCorrect());

                        db.collection("submissions").document(documentReference.getId())
                                .collection("answers").add(answerData);
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error submitting exam: " + e.getMessage());
                    Toast.makeText(this, "Error submitting exam. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}