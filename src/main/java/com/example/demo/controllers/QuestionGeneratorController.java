package com.example.demo.controllers;

import com.example.demo.models.Question;
import com.example.demo.services.UnifiedQuestionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionGeneratorController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionGeneratorController.class);
    private final UnifiedQuestionService questionService;
    
    public QuestionGeneratorController(UnifiedQuestionService questionService) {
        this.questionService = questionService;
    }
    
    @PostMapping("/generate-from-pdf")
    public ResponseEntity<?> generateQuestionsFromPDF(
            @RequestParam("file") MultipartFile pdfFile,
            @RequestParam(value = "numQuestions", defaultValue = "10") int numQuestions,
            @RequestParam(value = "questionType", defaultValue = "multiple-choice") String questionType,
            @RequestParam(value = "saveToDb", defaultValue = "false") boolean saveToDb) {
        
        try {
            // Extract text from PDF
            String pdfContent = questionService.extractTextFromPDF(pdfFile);
            
            // Generate questions
            List<Map<String, Object>> jsonQuestions = 
                questionService.generateQuestionsFromPDF(pdfContent, numQuestions, questionType);
            
            // If requested, save questions to the database
            if (saveToDb) {
                List<Question> questions = questionService.saveQuestions(jsonQuestions);
                return ResponseEntity.ok(questions);
            }
            
            // Otherwise just return the JSON
            return ResponseEntity.ok(jsonQuestions);
            
        } catch (Exception e) {
            logger.error("Error generating questions", e);
            return ResponseEntity.internalServerError().body("Error generating questions: " + e.getMessage());
        }
    }
    
    @GetMapping("/{questionId}")
    public ResponseEntity<?> getQuestion(@PathVariable UUID questionId) {
        return questionService.getQuestionById(questionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }
}