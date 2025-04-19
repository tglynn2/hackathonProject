package com.example.demo.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GameQuestionService {
    
    private final GeminiQuestionGeneratorService generatorService;
    private final ObjectMapper objectMapper;
    
    public GameQuestionService(GeminiQuestionGeneratorService generatorService) {
        this.generatorService = generatorService;
        this.objectMapper = new ObjectMapper();
    }
    
    public List<Question> generateGameQuestions(String material) {
        String promptInstruction = "Generate 10 multiple-choice questions with 4 options each, " +
            "where exactly 1 option is correct. Format each question as JSON with the following structure: " +
            "{\"id\": \"q1\", \"text\": \"Question text here\", \"choices\": [{\"text\": \"Option A\", \"correct\": true}, " +
            "{\"text\": \"Option B\", \"correct\": false}, ...]}";
        
        String jsonResponse = generatorService.generateQuestions(material, promptInstruction).block();
        
        try {
            return parseQuestionsFromJson(jsonResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse generated questions", e);
        }
    }
    
    private List<Question> parseQuestionsFromJson(String json) throws JsonProcessingException {
        List<Question> questions = new ArrayList<>();
        
        // Try to parse as a JSON array of questions
        try {
            questions = objectMapper.readValue(json, new TypeReference<List<Question>>() {});
        } catch (JsonProcessingException e) {
            // If that fails, try to extract JSON from the text response
            // (AI might wrap the JSON in explanatory text)
            String jsonPart = extractJsonFromText(json);
            if (jsonPart != null) {
                questions = objectMapper.readValue(jsonPart, new TypeReference<List<Question>>() {});
            } else {
                throw e;
            }
        }
        
        return questions;
    }
    
    private String extractJsonFromText(String text) {
        // Simple extraction - look for array of objects
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        
        return null;
    }
    
    // Question model class
    public static class Question {
        private String id;
        private String text;
        private List<Choice> choices;
        
        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public List<Choice> getChoices() { return choices; }
        public void setChoices(List<Choice> choices) { this.choices = choices; }
        
        public static class Choice {
            private String text;
            private boolean correct;
            
            // Getters and setters
            public String getText() { return text; }
            public void setText(String text) { this.text = text; }
            public boolean isCorrect() { return correct; }
            public void setCorrect(boolean correct) { this.correct = correct; }
        }
    }
}