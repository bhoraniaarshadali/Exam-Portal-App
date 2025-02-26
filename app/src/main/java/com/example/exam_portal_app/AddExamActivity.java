package com.example.exam_portal_app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;

public class AddExamActivity extends AppCompatActivity {

    private EditText examTitleEditText, examDurationEditText;
    private Button examStartTimeButton, examEndTimeButton, submitExamButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private long startTime, endTime;
    private boolean isTeacherVerified = false; // Track teacher verification status

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exam);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Verify user is a teacher before proceeding
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        verifyTeacherRole(user); // Async verification

        // UI elements (removed examSubjectEditText as per your request)
        examTitleEditText = findViewById(R.id.examTitleEditText);
        examDurationEditText = findViewById(R.id.examDurationEditText);
        examStartTimeButton = findViewById(R.id.examStartTimeButton);
        examEndTimeButton = findViewById(R.id.examEndTimeButton);
        submitExamButton = findViewById(R.id.submitExamButton);

        // Disable submit button until teacher role is verified
        submitExamButton.setEnabled(false);

        // Set click listeners for date/time buttons
        examStartTimeButton.setOnClickListener(v -> showDateTimePicker(true));
        examEndTimeButton.setOnClickListener(v -> showDateTimePicker(false));

        // Submit exam button click (only enabled after role verification)
        submitExamButton.setOnClickListener(v -> scheduleExam());
    }

    private void verifyTeacherRole(FirebaseUser user) {
        String email = user.getEmail();
        String normalizedName = (user.getDisplayName() != null ? user.getDisplayName().trim() : "unknown").toLowerCase().replace(" ", "-") + "-" + email.replace("@", "-").replace(".", "-");

        db.collection("Teacher").document(normalizedName).get()
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.firestore.DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            com.google.firebase.firestore.DocumentSnapshot document = task.getResult();
                            if (document.exists() && document.getString("uid").equals(user.getUid())) {
                                isTeacherVerified = true;
                                submitExamButton.setEnabled(true);
                                Toast.makeText(AddExamActivity.this, "Teacher role verified", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(AddExamActivity.this, "Access denied. You are not a teacher.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(AddExamActivity.this, "Error verifying role: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
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
        if (!isTeacherVerified) {
            Toast.makeText(this, "Teacher role not verified. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

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
            String displayName = user.getDisplayName() != null ? user.getDisplayName().trim() : "Unknown Teacher";
            String normalizedName = displayName.toLowerCase().replace(" ", "-") + "-" + email.replace("@", "-").replace(".", "-");

            Map<String, Object> examData = new HashMap<>();
            examData.put("title", title);
            examData.put("duration", duration);
            examData.put("start_time", startTime);
            examData.put("end_time", endTime);
            examData.put("created_by", normalizedName);
            examData.put("teacher_name", displayName);
            examData.put("max_attempts", 1);
            examData.put("question_types", "MCQ");
            examData.put("questions", new ArrayList<>());

            db.collection("exams").add(examData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Exam scheduled!", Toast.LENGTH_SHORT).show();
                        finish(); // Return to TeacherDashboardActivity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error scheduling exam: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        if (e.getMessage().contains("PERMISSION_DENIED")) {
                            Toast.makeText(this, "Permission denied. Contact admin.", Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid duration format", Toast.LENGTH_SHORT).show();
        }
    }
}