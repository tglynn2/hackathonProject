package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.models.Question;
import com.example.demo.repositories.QuestionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }
    
    public List<Question> saveAllQuestions(List<Question> questions) {
        return (List<Question>) questionRepository.saveAll(questions);
    }
    
    public Optional<Question> getQuestionById(UUID id) {
        return questionRepository.findById(id);
    }
    
    public List<Question> getAllQuestions() {
        return (List<Question>) questionRepository.findAll();
    }
    
    /**
     * Creates a Question object from JSON data received from the Gemini API
     */
    public Question createFromJson(Map<String, Object> jsonQuestion) {
        Question question = new Question();
        
        question.setQuestionText((String) jsonQuestion.get("text"));
        
        // Process options and set correct answers
        List<Map<String, Object>> choices = (List<Map<String, Object>>) jsonQuestion.get("choices");
        Map<String, Boolean> options = new HashMap<>();
        
        for (Map<String, Object> choice : choices) {
            String optionText = (String) choice.get("text");
            Boolean isCorrect = (Boolean) choice.get("correct");
            options.put(optionText, isCorrect);
        }
        
        question.setOptions(options);
        return question;
    }
}