package com.example.exam_portal_app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Exam implements Serializable {
    private String id;
    private String title;
    private long startTime;
    private long endTime;
    private int duration;
    private String created_by; // Normalized name-based ID
    private String teacher_name; // Full teacher name
    private int maxAttempts;
    private String questionTypes;
    private List<String> questions; // List of question IDs linked to this exam

    public Exam() {
        this.questions = new ArrayList<>();
    }

    public Exam(String id, String title, long startTime, long endTime, int duration, String created_by, String teacher_name, int maxAttempts, String questionTypes, List<String> questions) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.created_by = created_by;
        this.teacher_name = teacher_name;
        this.maxAttempts = maxAttempts;
        this.questionTypes = questionTypes;
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    // Getters and setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public int getDuration() { return duration; }
    public String getCreated_by() { return created_by; }
    public String getTeacher_name() { return teacher_name; }
    public int getMaxAttempts() { return maxAttempts; }
    public String getQuestionTypes() { return questionTypes; }
    public List<String> getQuestions() { return questions; }

    public void setQuestions(List<String> questions) { this.questions = questions; }
}