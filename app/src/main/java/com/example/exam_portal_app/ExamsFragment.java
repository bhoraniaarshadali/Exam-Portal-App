package com.example.exam_portal_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExamsFragment extends Fragment implements ExamAdapter.OnExamStartListener {

    private RecyclerView examsRecyclerView;
    private ExamAdapter examAdapter;
    private FirebaseFirestore db;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exams, container, false);

        // Initialize views
        examsRecyclerView = view.findViewById(R.id.examsRecyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Setup RecyclerView
        examsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        examAdapter = new ExamAdapter(this);
        examsRecyclerView.setAdapter(examAdapter);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Setup pull to refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadExams);

        // Initial load
        loadExams();

        return view;
    }

    @Override
    public void onExamStart(Exam exam) {
        if (getActivity() != null && exam != null) {
            Intent intent = new Intent(getActivity(), ExamActivity.class);
            intent.putExtra("exam", exam);
            startActivity(intent);
        }
    }

    private void loadExams() {
        if (getContext() == null) return;

        List<Exam> exams = new ArrayList<>();
        db.collection("exams")
                .orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Exam exam = document.toObject(Exam.class);
                        exam.setId(document.getId()); // Ensure ID is set
                        exams.add(exam);
                    }
                    examAdapter.setExamList(exams);
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Error loading exams: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }
}