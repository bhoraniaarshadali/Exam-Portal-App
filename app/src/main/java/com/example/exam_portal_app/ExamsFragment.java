package com.example.exam_portal_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExamsFragment extends Fragment {

    private RecyclerView examsRecyclerView;
    private ExamAdapter examAdapter;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exams, container, false);
        examsRecyclerView = view.findViewById(R.id.examsRecyclerView);
        examsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        examAdapter = new ExamAdapter(exam -> {
        }); // Placeholder listener, update as needed
        examsRecyclerView.setAdapter(examAdapter);

        db = FirebaseFirestore.getInstance();
        loadExams();

        return view;
    }

    private void loadExams() {
        List<Exam> exams = new ArrayList<>();
        db.collection("exams")
                .orderBy("start_time", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        exams.add(document.toObject(Exam.class));
                    }
                    examAdapter.setExamList(exams);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error loading exams: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}