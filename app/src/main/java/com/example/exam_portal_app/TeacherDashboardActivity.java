package com.example.exam_portal_app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TeacherDashboardActivity extends AppCompatActivity {

    private static final String TAG = "TeacherDashboard";

    private EditText examTitleEditText, examDurationEditText;
    private Button examStartTimeButton, examEndTimeButton, scheduleExamButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private long startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Verify user is a teacher before proceeding
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            checkTeacherRole(user);
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User not authenticated on dashboard load");
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Initialize UI elements
        initializeViews();

        // Set click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        examTitleEditText = findViewById(R.id.examTitleEditText);
        examDurationEditText = findViewById(R.id.examDurationEditText);
        examStartTimeButton = findViewById(R.id.examStartTimeButton);
        examEndTimeButton = findViewById(R.id.examEndTimeButton);
        scheduleExamButton = findViewById(R.id.scheduleExamButton);
    }

    private void setupClickListeners() {
        examStartTimeButton.setOnClickListener(v -> showDateTimePicker(true));
        examEndTimeButton.setOnClickListener(v -> showDateTimePicker(false));
        scheduleExamButton.setOnClickListener(v -> scheduleExam());
    }

    private void checkTeacherRole(FirebaseUser user) {
        db.collection("Teacher").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Access denied. You are not registered as a teacher.", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "User " + user.getEmail() + " not found in Teacher collection");
                        mAuth.signOut();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error verifying teacher role: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Role verification failed: " + e.getMessage());
                    mAuth.signOut();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    private void showDateTimePicker(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (datePicker, year, month, day) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timePicker, hour, minute) -> {
                                calendar.set(year, month, day, hour, minute);
                                long timeMillis = calendar.getTimeInMillis();

                                if (isStartTime) {
                                    if (timeMillis < System.currentTimeMillis()) {
                                        Toast.makeText(this, "Start time cannot be in the past", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    startTime = timeMillis;
                                    examStartTimeButton.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.getTime()));
                                } else {
                                    if (timeMillis <= startTime) {
                                        Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    endTime = timeMillis;
                                    examEndTimeButton.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.getTime()));
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void scheduleExam() {
        String title = examTitleEditText.getText().toString().trim();
        String durationStr = examDurationEditText.getText().toString().trim();

        if (validateExamData(title, durationStr)) {
            try {
                int duration = Integer.parseInt(durationStr);
                FirebaseUser user = mAuth.getCurrentUser();
                if (user == null) {
                    Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> examData = createExamData(title, duration, user);
                saveExamToFirestore(examData);

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid duration format", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Invalid duration input: " + durationStr);
            }
        }
    }

    private boolean validateExamData(String title, String durationStr) {
        if (title.isEmpty() || durationStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (startTime == 0 || endTime == 0) {
            Toast.makeText(this, "Please select start and end times", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private Map<String, Object> createExamData(String title, int duration, FirebaseUser user) {
        Map<String, Object> examData = new HashMap<>();
        examData.put("title", title);
        examData.put("duration", duration);
        examData.put("start_time", startTime);
        examData.put("end_time", endTime);
        examData.put("created_by", user.getUid());
        examData.put("teacher_name", user.getDisplayName() != null ? user.getDisplayName() : "Unknown Teacher");
        examData.put("max_attempts", 1);
        examData.put("question_types", "MCQ");
        examData.put("status", "scheduled");
        return examData;
    }

    private void saveExamToFirestore(Map<String, Object> examData) {
        db.collection("exams")
                .add(examData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Exam scheduled successfully!", Toast.LENGTH_SHORT).show();
                    sendNotificationToStudents((String) examData.get("title"),
                            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(startTime));
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to schedule exam: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Failed to schedule exam: " + e.getMessage());
                });
    }

    private void sendNotificationToStudents(String examTitle, String startTime) {
        Map<String, String> notification = new HashMap<>();
        notification.put("title", "New Exam Scheduled");
        notification.put("body", examTitle + " scheduled for " + startTime);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Notification saved for exam: " + examTitle))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to save notification: " + e.getMessage()));
    }

    private void clearFields() {
        examTitleEditText.setText("");
        examDurationEditText.setText("");
        examStartTimeButton.setText("Select Start Time");
        examEndTimeButton.setText("Select End Time");
        startTime = 0;
        endTime = 0;
    }
}