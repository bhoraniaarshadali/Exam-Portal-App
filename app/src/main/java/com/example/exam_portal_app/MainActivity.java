package com.example.exam_portal_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "MainActivity"; // Log tag

    private EditText emailEditText, passwordEditText;
    private RadioGroup roleRadioGroup;
    private com.google.android.gms.common.SignInButton googleSignInButton;
    private View loginButton, registerButton, phoneLoginButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Manually initialize Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized manually");
        }

        // Initialize Firebase services
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        phoneLoginButton = findViewById(R.id.phoneLoginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Button clicks
        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> startRegisterActivity());
        phoneLoginButton.setOnClickListener(v -> {
            Toast.makeText(this, "Phone OTP coming soon!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Phone OTP feature clicked - coming soon");
        });
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Login attempt failed: Email or password fields empty");
            return;
        }

        int selectedRoleId = roleRadioGroup.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Login attempt failed: No role selected");
            return;
        }
        RadioButton selectedRoleButton = findViewById(selectedRoleId);
        String role = selectedRoleButton.getText().toString().trim().toLowerCase();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "Email login successful for user: " + user.getEmail() + ", UID: " + user.getUid());
                        checkUserRole(user, role);
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Login failed: " + task.getException().getMessage());
                    }
                });
    }

    private void startRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        Log.d(TAG, "Navigating to RegisterActivity");
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        Log.d(TAG, "Starting Google Sign-In intent");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "Google Sign-In successful for account: " + account.getEmail() + ", ID Token: " + account.getIdToken());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Google Sign-In failed: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "Google authentication successful for user: " + user.getEmail() + ", UID: " + user.getUid());
                        // Prompt for role selection instead of defaulting to "student"
                        showRoleSelectionDialog(user);
                    } else {
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Google authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    private void showRoleSelectionDialog(FirebaseUser user) {
        // This is a placeholder for a dialog to select role (Student, Teacher, Admin)
        // You can implement a dialog or use an Activity/Fragment for role selection
        new android.app.AlertDialog.Builder(this)
                .setTitle("Select Role")
                .setItems(new String[]{"Student", "Teacher", "Admin"}, (dialog, which) -> {
                    String role = new String[]{"student", "teacher", "admin"}[which];
                    Log.d(TAG, "Selected role for Google user: " + role);
                    saveUserRole(user, role);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(this, "Role selection canceled", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Role selection canceled for Google user: " + user.getEmail());
                    mAuth.signOut();
                })
                .show();
    }

    private void saveUserRole(FirebaseUser user, String role) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getDisplayName() != null ? user.getDisplayName() : "Google User");
        userData.put("email", user.getEmail());

        String collection;
        switch (role.toLowerCase()) {
            case "student":
                collection = "Student";
                break;
            case "teacher":
                collection = "Teacher";
                break;
            case "admin":
                collection = "Admin";
                break;
            default:
                collection = "Student"; // Default to Student
                break;
        }
        Log.d(TAG, "Saving user " + user.getEmail() + " as " + role + " in collection: " + collection);
        db.collection(collection).document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User registered as " + role, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "User registered successfully as " + role + " in collection: " + collection);
                    goToDashboard(role);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error saving role for user " + user.getEmail() + " in " + collection + ": " + e.getMessage());
                    mAuth.signOut();
                });
    }

    private void checkUserRole(FirebaseUser user, String selectedRole) {
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "User not authenticated during role check");
            return;
        }

        // Capitalize the role for collection name (e.g., "Teacher", "Student", "Admin")
        String collection = selectedRole.substring(0, 1).toUpperCase() + selectedRole.substring(1);
        Log.d(TAG, "Checking role for user " + user.getEmail() + " in collection: " + collection + ", UID: " + user.getUid());

        db.collection(collection).document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User found in the selected role's collection
                        Map<String, Object> userData = documentSnapshot.getData();
                        Log.d(TAG, "User " + user.getEmail() + " found in " + collection + " with data: " + userData);

                        // Optionally, verify email consistency (extra safety)
                        String storedEmail = (String) userData.get("email");
                        if (storedEmail != null && storedEmail.equals(user.getEmail())) {
                            Toast.makeText(this, "Login successful as " + selectedRole, Toast.LENGTH_SHORT).show();
                            goToDashboard(selectedRole);
                        } else {
                            Toast.makeText(this, "Email mismatch for " + selectedRole + " role.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "Email mismatch for user " + user.getEmail() + " in " + collection);
                            mAuth.signOut();
                        }
                    } else {
                        // User not found in the selected role's collection
                        Toast.makeText(this, "User not registered as " + selectedRole + ". Please register or select the correct role.", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "User " + user.getEmail() + " not found in " + collection);
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage().contains("PERMISSION_DENIED")) {
                        Toast.makeText(this, "Access denied: Check Firestore permissions for " + selectedRole, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Permission denied for " + user.getEmail() + " in " + collection + ": " + e.getMessage());
                    } else {
                        Toast.makeText(this, "Error verifying role: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Role verification failed for " + user.getEmail() + " in " + collection + ": " + e.getMessage());
                    }
                    mAuth.signOut();
                });
    }

    private void goToDashboard(String role) {
        Intent intent;
        switch (role) {
            case "student":
                intent = new Intent(this, StudentDashboardActivity.class);
                break;
            case "teacher":
                intent = new Intent(this, TeacherDashboardActivity.class);
                break;
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            default:
                Toast.makeText(this, "Invalid role: " + role, Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Invalid role detected: " + role);
                return;
        }
        startActivity(intent);
        finish();
        Log.d(TAG, "Navigating to " + role + " dashboard");
    }
}