package com.example.exam_portal_app;

import java.io.Serializable;
import java.util.Arrays;

public class Question implements Serializable {
    private String id;
    private String text;
    private String type; // "MCQ", "subjective", "coding"
    private String[] options; // For MCQs
    private String correctAnswer; // For MCQs
    private String userAnswer; // To store the user's answer

    public Question() {
        // Required empty constructor for Firestore
    }

    public Question(String id, String text, String type, String[] options, String correctAnswer) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.userAnswer = "";
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    // Helper method to check if answer is correct
    public boolean isCorrect() {
        return userAnswer != null && userAnswer.equals(correctAnswer);
    }

    @Override
    public String toString() {
        return "Question{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", type='" + type + '\'' +
                ", options=" + Arrays.toString(options) +
                ", correctAnswer='" + correctAnswer + '\'' +
                '}';
    }
}