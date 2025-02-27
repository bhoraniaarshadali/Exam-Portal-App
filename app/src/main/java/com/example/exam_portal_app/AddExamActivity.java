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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddExamActivity extends AppCompatActivity {

    private EditText examTitleEditText, examDurationEditText;
    private Button examStartTimeButton, examEndTimeButton, submitExamButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private long startTime, endTime;
    private boolean isTeacherVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exam);

        initializeFirebase();
        initializeViews();
        verifyUser();
        setupClickListeners();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        examTitleEditText = findViewById(R.id.examTitleEditText);
        examDurationEditText = findViewById(R.id.examDurationEditText);
        examStartTimeButton = findViewById(R.id.examStartTimeButton);
        examEndTimeButton = findViewById(R.id.examEndTimeButton);
        submitExamButton = findViewById(R.id.submitExamButton);
        submitExamButton.setEnabled(false);
    }

    private void verifyUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            showError("User not authenticated");
            finish();
            return;
        }
        verifyTeacherRole(user);
    }

    private void setupClickListeners() {
        examStartTimeButton.setOnClickListener(v -> showDateTimePicker(true));
        examEndTimeButton.setOnClickListener(v -> showDateTimePicker(false));
        submitExamButton.setOnClickListener(v -> scheduleExam());
    }

    private void verifyTeacherRole(FirebaseUser user) {
        String email = user.getEmail();
        String displayName = user.getDisplayName();
        String normalizedName = getNormalizedName(displayName, email);
        String authUid = user.getUid();

        Log.d("AddExamActivity", "Verifying teacher role for: " + normalizedName);

        db.collection("Teacher").document(normalizedName).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String docUid = document.getString("uid");
                        if (docUid != null && docUid.equals(authUid)) {
                            handleTeacherVerificationSuccess();
                        } else {
                            handleTeacherVerificationFailure("UID mismatch");
                        }
                    } else {
                        handleTeacherVerificationFailure("Not a teacher");
                    }
                })
                .addOnFailureListener(e -> {
                    handleTeacherVerificationFailure(e.getMessage());
                });
    }

    private String getNormalizedName(String displayName, String email) {
        String name = (displayName != null ? displayName.trim() : "unknown");
        return name.toLowerCase().replace(" ", "-") + "-" +
                email.replace("@", "-").replace(".", "-");
    }

    private void handleTeacherVerificationSuccess() {
        isTeacherVerified = true;
        submitExamButton.setEnabled(true);
        showMessage("Teacher role verified");
    }

    private void handleTeacherVerificationFailure(String reason) {
        Log.w("AddExamActivity", "Teacher verification failed: " + reason);
        showError("Access denied: " + reason);
        finish();
    }

    private void showDateTimePicker(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dateDialog = new DatePickerDialog(
                this,
                (datePicker, year, month, day) -> showTimePicker(year, month, day, isStartTime),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dateDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dateDialog.show();
    }

    private void showTimePicker(int year, int month, int day, boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(this, (timePicker, hour, minute) -> {
            calendar.set(year, month, day, hour, minute);
            long selectedTime = calendar.getTimeInMillis();

            if (isStartTime) {
                if (selectedTime < System.currentTimeMillis()) {
                    showError("Start time cannot be in the past");
                    return;
                }
                startTime = selectedTime;
                updateTimeButton(examStartTimeButton, calendar);
            } else {
                if (startTime == 0) {
                    showError("Please select start time first");
                    return;
                }
                if (selectedTime <= startTime) {
                    showError("End time must be after start time");
                    return;
                }
                endTime = selectedTime;
                updateTimeButton(examEndTimeButton, calendar);
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void updateTimeButton(Button button, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        button.setText(sdf.format(calendar.getTime()));
    }

    private void scheduleExam() {
        if (!validateExamData()) {
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        String title = examTitleEditText.getText().toString().trim();
        int duration = Integer.parseInt(examDurationEditText.getText().toString().trim());

        Map<String, Object> examData = createExamData(user, title, duration);

        db.collection("exams").add(examData)
                .addOnSuccessListener(documentReference -> {
                    showMessage("Exam scheduled successfully!");
                    finish();
                })
                .addOnFailureListener(e -> {
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("PERMISSION_DENIED")) {
                        showError("Permission denied. Contact admin.");
                    } else {
                        showError("Failed to schedule exam: " + errorMsg);
                    }
                    Log.e("AddExamActivity", "Error scheduling exam", e);
                });
    }

    private boolean validateExamData() {
        if (!isTeacherVerified) {
            showError("Teacher role not verified");
            return false;
        }

        String title = examTitleEditText.getText().toString().trim();
        String durationStr = examDurationEditText.getText().toString().trim();

        if (title.isEmpty() || durationStr.isEmpty()) {
            showError("Please fill all fields");
            return false;
        }

        try {
            int duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                showError("Duration must be positive");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Invalid duration format");
            return false;
        }

        if (startTime == 0 || endTime == 0) {
            showError("Please select both start and end times");
            return false;
        }

        return true;
    }

    private Map<String, Object> createExamData(FirebaseUser user, String title, int duration) {
        String displayName = user.getDisplayName() != null ? user.getDisplayName().trim() : "Unknown Teacher";
        String normalizedName = getNormalizedName(displayName, user.getEmail());

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

        return examData;
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
}