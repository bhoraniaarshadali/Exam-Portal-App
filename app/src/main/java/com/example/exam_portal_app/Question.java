package com.example.exam_portal_app;

public class Question {
    private String id;
    private String text;
    private String type; // "MCQ", "subjective", "coding"
    private String[] options; // For MCQs
    private String correctAnswer; // For MCQs

    public Question() {
    }

    public Question(String id, String text, String type, String[] options, String correctAnswer) {
        this.id = id;
        this.text = text;
        this.type = type;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public String[] getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}