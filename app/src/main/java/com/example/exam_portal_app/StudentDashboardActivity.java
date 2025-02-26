package com.example.exam_portal_app;

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

        // Verify user is authenticated before proceeding
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // UI elements
        examsRecyclerView = findViewById(R.id.examsRecyclerView);
        examsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        examAdapter = new ExamAdapter(this); // Pass this activity as the listener
        examsRecyclerView.setAdapter(examAdapter);

        // Load available exams
        loadAvailableExams();
    }

    private void loadAvailableExams() {
        db.collection("exams")
                .whereGreaterThan("end_time", System.currentTimeMillis())
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
                        String teacherName = document.getString("teacher_name");
                        int maxAttempts = document.getLong("max_attempts").intValue();
                        String questionTypes = document.getString("question_types");
                        List<String> questions = (List<String>) document.get("questions"); // Retrieve questions field

                        // Create Exam object with all parameters
                        Exam exam = new Exam(id, title, startTime, endTime, duration, createdBy, teacherName, maxAttempts, questionTypes, questions);
                        examList.add(exam);
                    }
                    examAdapter.setExamList(examList);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading available exams: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onExamStart(Exam exam) {
        // Handle exam start (e.g., navigate to ExamActivity)
        Toast.makeText(this, "Starting exam: " + exam.getTitle(), Toast.LENGTH_SHORT).show();
        // Add navigation logic here (e.g., start ExamActivity with exam data)
    }
}