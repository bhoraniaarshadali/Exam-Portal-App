package com.example.exam_portal_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button addExamButton, manageUsersButton, monitorActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Verify user is an admin before proceeding
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        checkAdminRole(user);

        // UI elements
        addExamButton = findViewById(R.id.addExamButton);
        manageUsersButton = findViewById(R.id.manageExamsButton); // Corrected from manageExamsButton
        monitorActivityButton = findViewById(R.id.monitorActivityButton);

        // Set click listeners
        addExamButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AddExamActivity.class);
            startActivity(intent);
        });

        manageUsersButton.setOnClickListener(v -> {
            // Implement manage users functionality (e.g., navigate to a new activity or fragment)
            Toast.makeText(this, "Manage Users feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        monitorActivityButton.setOnClickListener(v -> {
            // Implement monitor activity functionality (e.g., view student attempts or exam status)
            Toast.makeText(this, "Monitor Activity feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Tabbed interface for admin dashboard
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        // Set up ViewPager with AdminPagerAdapter
        AdminPagerAdapter pagerAdapter = new AdminPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Users");
                    break;
                case 1:
                    tab.setText("Exams");
                    break;
                case 2:
                    tab.setText("Questions");
                    break;
                case 3:
                    tab.setText("Attempts");
                    break;
            }
        }).attach();
    }

    private void checkAdminRole(FirebaseUser user) {
        String email = user.getEmail();
        String normalizedName = (user.getDisplayName() != null ? user.getDisplayName().trim() : "unknown").toLowerCase().replace(" ", "-") + "-" + email.replace("@", "-").replace(".", "-");

        db.collection("Admin").document(normalizedName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Proceed with admin dashboard
                    } else {
                        Toast.makeText(this, "Access denied. You are not an admin.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error verifying role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    // AdminPagerAdapter for ViewPager2
    private static class AdminPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {

        public AdminPagerAdapter(AppCompatActivity activity) {
            super(activity);
        }

        @Override
        public androidx.fragment.app.Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new UsersFragment(); // Manage users
                case 1:
                    return new ExamsFragment(); // Manage exams (uses your existing ExamsFragment)
                case 2:
                    return new QuestionsFragment(); // Manage questions
                case 3:
                    return new AttemptsFragment(); // Manage student attempts
                default:
                    return new UsersFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}