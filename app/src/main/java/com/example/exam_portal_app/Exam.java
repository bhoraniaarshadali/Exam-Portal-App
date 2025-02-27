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
    private String created_by;
    private String teacher_name;
    private int maxAttempts;
    private String questionTypes;
    private List<String> questions;

    public Exam() {
        this.questions = new ArrayList<>();
    }

    public Exam(String id, String title, long startTime, long endTime, int duration,
                String created_by, String teacher_name, int maxAttempts,
                String questionTypes, List<String> questions) {
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getCreated_by() {
        return created_by;
    }

    public void setCreated_by(String created_by) {
        this.created_by = created_by;
    }

    public String getTeacher_name() {
        return teacher_name;
    }

    public void setTeacher_name(String teacher_name) {
        this.teacher_name = teacher_name;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getQuestionTypes() {
        return questionTypes;
    }

    public void setQuestionTypes(String questionTypes) {
        this.questionTypes = questionTypes;
    }

    public List<String> getQuestions() {
        return questions != null ? questions : new ArrayList<>();
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions != null ? questions : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Exam{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", duration=" + duration +
                ", created_by='" + created_by + '\'' +
                ", teacher_name='" + teacher_name + '\'' +
                ", maxAttempts=" + maxAttempts +
                ", questionTypes='" + questionTypes + '\'' +
                ", questions=" + questions +
                '}';
    }
}