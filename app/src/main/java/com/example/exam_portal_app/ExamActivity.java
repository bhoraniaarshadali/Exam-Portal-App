package com.example.exam_portal_app;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ExamActivity extends AppCompatActivity {

    private TextView examTitleTextView, timeRemainingTextView;
    private RecyclerView questionsRecyclerView;
    private Button submitExamButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CountDownTimer countDownTimer;
    private Exam exam;
    private List<Question> questions = new ArrayList<>();

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

        // Set up RecyclerView for questions (placeholder, fetch from Firestore later)
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadQuestions();

        submitExamButton.setOnClickListener(v -> submitExam());
    }

    private void loadQuestions() {
        // TODO: Fetch questions from Firestore based on exam criteria
        // For now, add a dummy question
        questions.add(new Question("1", "What is 2 + 2?", "MCQ", new String[]{"3", "4", "5"}, "4"));
        QuestionAdapter questionAdapter = new QuestionAdapter(questions);
        questionsRecyclerView.setAdapter(questionAdapter);
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
        // TODO: Save answers to Firestore, evaluate MCQs, and notify teacher for subjective answers
        Toast.makeText(this, "Exam submitted!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}