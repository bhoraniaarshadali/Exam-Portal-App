package com.example.exam_portal_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Verify user is authenticated before proceeding
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        examsRecyclerView = findViewById(R.id.examsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Setup RecyclerView
        examsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        examAdapter = new ExamAdapter(this);
        examsRecyclerView.setAdapter(examAdapter);

        // Setup pull to refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadAvailableExams);

        // Initial load
        loadAvailableExams();
    }

    private void loadAvailableExams() {
        db.collection("exams")
                .whereGreaterThan("end_time", System.currentTimeMillis())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Exam> examList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String id = document.getId();
                            String title = document.getString("title");
                            long startTime = document.getLong("start_time");
                            long endTime = document.getLong("end_time");
                            int duration = document.getLong("duration").intValue();
                            String createdBy = document.getString("created_by");
                            String teacherName = document.getString("teacher_name");
                            int maxAttempts = document.getLong("max_attempts").intValue();
                            String questionTypes = document.getString("question_types");
                            List<String> questions = (List<String>) document.get("questions");

                            if (id != null && title != null && startTime > 0 && endTime > 0) {
                                Exam exam = new Exam(id, title, startTime, endTime, duration,
                                        createdBy, teacherName, maxAttempts, questionTypes, questions);
                                examList.add(exam);
                            }
                        } catch (Exception e) {
                            // Skip invalid exam entries
                            continue;
                        }
                    }
                    examAdapter.setExamList(examList);
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading exams: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    @Override
    public void onExamStart(Exam exam) {
        if (exam != null) {
            Intent intent = new Intent(this, ExamActivity.class);
            intent.putExtra("exam", exam);
            startActivity(intent);
        }
    }
}