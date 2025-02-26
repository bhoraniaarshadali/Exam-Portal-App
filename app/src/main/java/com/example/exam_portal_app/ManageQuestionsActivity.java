package com.example.exam_portal_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageQuestionsActivity extends AppCompatActivity {

    private RecyclerView questionsRecyclerView;
    private QuestionAdapter questionAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isTeacherVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_questions);

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
        verifyTeacherRole(user);

        // UI elements
        questionsRecyclerView = findViewById(R.id.questionsRecyclerView);
        Button addQuestionButton = findViewById(R.id.addQuestionButton);

        // Set up RecyclerView
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionAdapter = new QuestionAdapter(question -> showQuestionDialog(question), this);
        questionsRecyclerView.setAdapter(questionAdapter);

        // Load questions only if teacher role is verified
        if (isTeacherVerified) {
            loadQuestions();
        }

        // Add question button click
        addQuestionButton.setOnClickListener(v -> {
            if (isTeacherVerified) {
                showQuestionDialog(null);
            } else {
                Toast.makeText(this, "Teacher role not verified. Please wait.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyTeacherRole(FirebaseUser user) {
        String email = user.getEmail();
        String normalizedName = (user.getDisplayName() != null ? user.getDisplayName().trim() : "unknown").toLowerCase().replace(" ", "-") + "-" + email.replace("@", "-").replace(".", "-");

        db.collection("Teacher").document(normalizedName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        com.google.firebase.firestore.DocumentSnapshot document = task.getResult();
                        if (document.exists() && document.getString("uid") != null && document.getString("uid").equals(user.getUid())) {
                            isTeacherVerified = true;
                            // Reload questions after verification
                            loadQuestions();
                            Toast.makeText(ManageQuestionsActivity.this, "Teacher role verified", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ManageQuestionsActivity.this, "Access denied. You are not a teacher.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(ManageQuestionsActivity.this, "Error verifying role: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void loadQuestions() {
        db.collection("questions")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Question> questions = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        questions.add(document.toObject(Question.class));
                    }
                    questionAdapter.setQuestions(questions);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading questions: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showQuestionDialog(Question question) {
        QuestionDialog dialog = new QuestionDialog(this, question, new QuestionDialog.OnQuestionSavedListener() {
            @Override
            public void onQuestionSaved(Question newQuestion) {
                if (newQuestion.getId() == null) {
                    Map<String, Object> questionData = new HashMap<>();
                    questionData.put("questionText", newQuestion.getQuestionText());
                    questionData.put("options", newQuestion.getOptions());
                    questionData.put("correctAnswer", newQuestion.getCorrectAnswer());
                    questionData.put("examId", ""); // Will be linked later to an exam

                    db.collection("questions").add(questionData)
                            .addOnSuccessListener(documentReference -> {
                                newQuestion.setId(documentReference.getId());
                                questionAdapter.addQuestion(newQuestion);
                                Toast.makeText(ManageQuestionsActivity.this, "Question added!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ManageQuestionsActivity.this, "Error adding question: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Map<String, Object> questionData = new HashMap<>();
                    questionData.put("questionText", newQuestion.getQuestionText());
                    questionData.put("options", newQuestion.getOptions());
                    questionData.put("correctAnswer", newQuestion.getCorrectAnswer());
                    questionData.put("examId", newQuestion.getExamId());

                    db.collection("questions").document(newQuestion.getId())
                            .update(questionData)
                            .addOnSuccessListener(aVoid -> {
                                questionAdapter.updateQuestion(newQuestion);
                                Toast.makeText(ManageQuestionsActivity.this, "Question updated!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ManageQuestionsActivity.this, "Error updating question: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onQuestionDeleted(Question question) {
                if (question.getId() != null) {
                    db.collection("questions").document(question.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                questionAdapter.removeQuestion(question);
                                Toast.makeText(ManageQuestionsActivity.this, "Question deleted!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(ManageQuestionsActivity.this, "Error deleting question: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
        dialog.show();
    }
}