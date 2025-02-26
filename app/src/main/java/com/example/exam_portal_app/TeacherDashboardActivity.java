package com.example.exam_portal_app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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
        String email = user.getEmail();
        String normalizedName = (user.getDisplayName() != null ? user.getDisplayName() : "unknown").toLowerCase().replace(" ", "-") + "-" + email.replace("@", "-").replace(".", "-");

        db.collection("Teacher").document(normalizedName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Access denied. You are not a teacher.", Toast.LENGTH_SHORT).show();
                        finish(); // Exit if not a teacher
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error verifying role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

        // Show DatePickerDialog first
        new DatePickerDialog(this, (datePicker, selectedYear, selectedMonth, selectedDay) -> {
            // Then show TimePickerDialog
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

                // Validate that end time is after start time
                if (startTime != 0 && endTime != 0 && endTime <= startTime) {
                    Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
                    endTime = 0; // Reset end time if invalid
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
            String normalizedName = displayName.toLowerCase().replace(" ", "-") + "-" + email.replace("@", "-").replace(".", "-");

            Map<String, Object> examData = new HashMap<>();
            examData.put("title", title);
            examData.put("duration", duration);
            examData.put("start_time", startTime);
            examData.put("end_time", endTime);
            examData.put("created_by", normalizedName); // Must match the teacher's document ID in Teacher collection
            examData.put("teacher_name", displayName); // Human-readable name
            examData.put("max_attempts", 1);
            examData.put("question_types", "MCQ"); // Default, expandable later

            db.collection("exams").add(examData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Exam scheduled!", Toast.LENGTH_SHORT).show();
                        sendNotificationToStudents(title, new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(startTime));
                        clearFields();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error scheduling exam: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        if (e.getMessage().contains("PERMISSION_DENIED")) {
                            Toast.makeText(this, "Permission denied. Contact admin.", Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid duration format", Toast.LENGTH_SHORT).show();
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
        examStartTimeButton.setText("Select Start Time");
        examEndTimeButton.setText("Select End Time");
        startTime = 0;
        endTime = 0;
    }
}