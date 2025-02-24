package com.example.exam_portal_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TeacherDashboardActivity extends AppCompatActivity {

    private EditText examTitleEditText, examDurationEditText, examStartTimeEditText, examEndTimeEditText;
    private Button scheduleExamButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elements
        examTitleEditText = findViewById(R.id.examTitleEditText);
        examDurationEditText = findViewById(R.id.examDurationEditText);
        examStartTimeEditText = findViewById(R.id.examStartTimeEditText);
        examEndTimeEditText = findViewById(R.id.examEndTimeEditText);
        scheduleExamButton = findViewById(R.id.scheduleExamButton);

        // Schedule exam button click
        scheduleExamButton.setOnClickListener(v -> scheduleExam());
    }

    private void scheduleExam() {
        String title = examTitleEditText.getText().toString().trim();
        String durationStr = examDurationEditText.getText().toString().trim();
        String startTimeStr = examStartTimeEditText.getText().toString().trim();
        String endTimeStr = examEndTimeEditText.getText().toString().trim();

        if (title.isEmpty() || durationStr.isEmpty() || startTimeStr.isEmpty() || endTimeStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            long startTime = sdf.parse(startTimeStr).getTime();
            long endTime = sdf.parse(endTimeStr).getTime();

            if (startTime >= endTime) {
                Toast.makeText(this, "Start time must be before end time", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> examData = new HashMap<>();
            examData.put("title", title);
            examData.put("duration", duration);
            examData.put("start_time", startTime);
            examData.put("end_time", endTime);
            examData.put("created_by", mAuth.getCurrentUser().getUid());
            examData.put("max_attempts", 1);
            examData.put("question_types", "MCQ"); // Default, expandable later

            db.collection("exams").add(examData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Exam scheduled!", Toast.LENGTH_SHORT).show();
                        sendNotificationToStudents(title, startTimeStr);
                        clearFields();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            Toast.makeText(this, "Invalid date format or duration", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotificationToStudents(String examTitle, String startTime) {
        // For simplicity, send to a topic "students"
        FirebaseMessaging.getInstance().subscribeToTopic("students")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Notified students about " + examTitle, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearFields() {
        examTitleEditText.setText("");
        examDurationEditText.setText("");
        examStartTimeEditText.setText("");
        examEndTimeEditText.setText("");
    }
}