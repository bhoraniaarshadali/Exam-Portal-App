package com.example.exam_portal_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentDashboardActivity extends AppCompatActivity implements ExamAdapter.OnExamStartListener {

    private RecyclerView examsRecyclerView;
    private ExamAdapter examAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        examsRecyclerView = findViewById(R.id.examsRecyclerView);
        examsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        examAdapter = new ExamAdapter(this);
        examsRecyclerView.setAdapter(examAdapter);

        // Fetch exams
        loadUpcomingExams();
    }

    private void loadUpcomingExams() {
        db.collection("exams")
                .whereGreaterThan("end_time", System.currentTimeMillis()) // Only show future exams
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Exam> examList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String id = document.getId();
                        String title = document.getString("title");
                        long startTime = document.getLong("start_time");
                        long endTime = document.getLong("end_time");
                        int duration = document.getLong("duration").intValue();
                        String createdBy = document.getString("created_by");
                        int maxAttempts = document.getLong("max_attempts").intValue();
                        String questionTypes = document.getString("question_types");
                        examList.add(new Exam(id, title, startTime, endTime, duration, createdBy, maxAttempts, questionTypes));
                    }
                    examAdapter.setExamList(examList);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading exams: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onExamStart(Exam exam) {
        long now = System.currentTimeMillis();
        if (now >= exam.getStartTime() && now <= exam.getEndTime()) {
            Intent intent = new Intent(this, ExamActivity.class);
            intent.putExtra("exam", exam);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Exam not available yet or has ended", Toast.LENGTH_SHORT).show();
        }
    }
}