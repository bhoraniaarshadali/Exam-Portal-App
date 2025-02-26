package com.example.exam_portal_app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TeacherDashboardActivity extends AppCompatActivity {

    private static final String TAG = "TeacherDashboard"; // Log tag for debugging

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
            finish();
        }

        // UI elements
        examTitleEditText = findViewById(R.id.examTitleEditText);
        examDurationEditText = findViewById(R.id.examDurationEditText);
        examStartTimeButton = findViewById(R.id.examStartTimeButton);
        examEndTimeButton = findViewById(R.id.examEndTimeButton);
        scheduleExamButton = findViewById(R.id.scheduleExamButton);

        // Set click listeners for date/time buttons
        examStartTimeButton.setOnClickListener(v -> showDateTimePicker(true));
        examEndTimeButton.setOnClickListener(v -> showDateTimePicker(false));

        // Schedule exam button click
        scheduleExamButton.setOnClickListener(v -> scheduleExam());
    }

    private void checkTeacherRole(FirebaseUser user) {
        // Use UID consistent with RegisterActivity and MainActivity
        db.collection("Teacher").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Teacher role verified for user: " + user.getEmail() + ", UID: " + user.getUid());
                        // Teacher role confirmed, proceed with dashboard
                    } else {
                        Toast.makeText(this, "Access denied. You are not registered as a teacher.", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "User " + user.getEmail() + " not found in Teacher collection, UID: " + user.getUid());
                        mAuth.signOut();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error verifying teacher role: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Role verification failed for " + user.getEmail() + ": " + e.getMessage());
                    mAuth.signOut();
                    finish();
                });
    }

    private void showDateTimePicker(boolean isStartTime) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new DatePickerDialog(this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
            new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
                calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
                long timeMillis = calendar.getTimeInMillis();

                if (isStartTime) {
                    startTime = timeMillis;
                    examStartTimeButton.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.getTime()));
                } else {
                    endTime = timeMillis;
                    examEndTimeButton.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(calendar.getTime()));
                }

                if (startTime != 0 && endTime != 0 && endTime <= startTime) {
                    Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                    endTime = 0;
                    examEndTimeButton.setText("Select End Time");
                }
            }, hour, minute, true).show();
        }, year, month, day).show();
    }

    private void scheduleExam() {
        String title = examTitleEditText.getText().toString().trim();
        String durationStr = examDurationEditText.getText().toString().trim();

        if (title.isEmpty() || durationStr.isEmpty() || startTime == 0 || endTime == 0) {
            Toast.makeText(this, "Please fill all fields and select times", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int duration = Integer.parseInt(durationStr);

            if (endTime <= startTime) {
                Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            String email = user.getEmail();
            String displayName = user.getDisplayName() != null ? user.getDisplayName() : "Unknown Teacher";

            Map<String, Object> examData = new HashMap<>();
            examData.put("title", title);
            examData.put("duration", duration);
            examData.put("start_time", startTime);
            examData.put("end_time", endTime);
            examData.put("created_by", user.getUid()); // Use UID here for consistency
            examData.put("teacher_name", displayName); // Human-readable name
            examData.put("max_attempts", 1);
            examData.put("question_types", "MCQ");

            db.collection("exams").add(examData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Exam scheduled!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Exam " + title + " scheduled by " + email + ", UID: " + user.getUid());
                        sendNotificationToStudents(title, new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(startTime));
                        clearFields();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error scheduling exam: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Failed to schedule exam by " + email + ": " + e.getMessage());
                        if (e.getMessage().contains("PERMISSION_DENIED")) {
                            Toast.makeText(this, "Permission denied. Contact admin.", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid duration format", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Invalid duration input: " + durationStr);
        }
    }

    private void sendNotificationToStudents(String examTitle, String startTime) {
        FirebaseMessaging.getInstance().subscribeToTopic("students")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Notified students about " + examTitle, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Notification sent for exam: " + examTitle + " at " + startTime);
                    } else {
                        Log.w(TAG, "Failed to subscribe to students topic: " + task.getException().getMessage());
                    }
                });
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