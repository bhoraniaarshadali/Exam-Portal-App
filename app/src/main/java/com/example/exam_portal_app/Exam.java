package com.example.exam_portal_app;

import java.io.Serializable;

public class Exam implements Serializable {
    private String id;
    private String title;
    private long startTime;
    private long endTime;
    private int duration;
    private String createdBy;
    private int maxAttempts;
    private String questionTypes;

    public Exam() {
    }

    public Exam(String id, String title, long startTime, long endTime, int duration, String createdBy, int maxAttempts, String questionTypes) {
        this.id = id;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.createdBy = createdBy;
        this.maxAttempts = maxAttempts;
        this.questionTypes = questionTypes;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getDuration() {
        return duration;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public String getQuestionTypes() {
        return questionTypes;
    }
}