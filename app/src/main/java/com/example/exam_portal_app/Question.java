package com.example.exam_portal_app;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {
    private String id;
    private String questionText;
    private String type; // "MCQ", "subjective", "coding"
    private List<String> options; // For MCQ questions
    private String correctAnswer; // For MCQ/subjective questions
    private String codeTemplate; // For coding questions
    private String examId;

    public Question() {
    }

    public Question(String id, String questionText, String type, List<String> options, String correctAnswer, String codeTemplate, String examId) {
        this.id = id;
        this.questionText = questionText;
        this.type = type;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.codeTemplate = codeTemplate;
        this.examId = examId;
    }

    // Getters
    public String getId() {
        return id;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getCodeTemplate() {
        return codeTemplate;
    }

    public void setCodeTemplate(String codeTemplate) {
        this.codeTemplate = codeTemplate;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    // Method to check if an answer is correct (for MCQ/subjective)
    public boolean isCorrect(String userAnswer) {
        if (userAnswer == null || correctAnswer == null) return false;
        return correctAnswer.trim().equalsIgnoreCase(userAnswer.trim());
    }

    // Method to get user answer (placeholder, can be expanded)
    public String getUserAnswer() {
        // This could be implemented to retrieve user-submitted answers from a data store or UI
        return null; // Default to null; update based on your needs
    }
}