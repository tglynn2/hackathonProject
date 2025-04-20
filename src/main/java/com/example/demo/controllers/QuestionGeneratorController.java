package com.example.demo.controllers;

import com.example.demo.models.Question;
import com.example.demo.services.GeminiQuestionGeneratorService;
import com.example.demo.services.PDFProcessingService;
import com.example.demo.services.QuestionService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionGeneratorController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionGeneratorController.class);
    
    private final GeminiQuestionGeneratorService questionGeneratorService;
    private final PDFProcessingService pdfProcessingService;
    private final QuestionService questionService;
    private final ObjectMapper objectMapper;
    
    public QuestionGeneratorController(
            GeminiQuestionGeneratorService questionGeneratorService,
            PDFProcessingService pdfProcessingService,
            QuestionService questionService,
            ObjectMapper objectMapper) {
        this.questionGeneratorService = questionGeneratorService;
        this.pdfProcessingService = pdfProcessingService;
        this.questionService = questionService;
        this.objectMapper = objectMapper;
    }
    
    @PostMapping("/generate-from-pdf")
    public ResponseEntity<?> generateQuestionsFromPDF(
            @RequestParam("file") MultipartFile pdfFile,
            @RequestParam(value = "numQuestions", defaultValue = "10") int numQuestions,
            @RequestParam(value = "saveToDb", defaultValue = "false") boolean saveToDb) {
        
        try {
            // Extract text from PDF
            String pdfContent = pdfProcessingService.extractTextFromPDF(pdfFile);
            
            // Construct prompt for multiple-choice question generation
            String prompt = constructMultipleChoicePrompt(numQuestions);
            
            // Generate questions using the Gemini API
            String questionsJson = questionGeneratorService.generateQuestions(pdfContent, prompt)
                    .block(); // Blocking call to get the result
            
            // Parse the JSON response
            List<Map<String, Object>> jsonQuestions = objectMapper.readValue(
                    questionsJson, 
                    new TypeReference<List<Map<String, Object>>>() {}
            );
            
            // If requested, save questions to the database
            if (saveToDb) {
                List<Question> questions = new ArrayList<>();
                for (Map<String, Object> jsonQuestion : jsonQuestions) {
                    Question question = questionService.createFromJson(jsonQuestion);
                    questions.add(question);
                }
                questionService.saveAllQuestions(questions);
                return ResponseEntity.ok(questions);
            }
            
            // Otherwise just return the JSON
            return ResponseEntity.ok(jsonQuestions);
            
        } catch (IOException e) {
            logger.error("Error processing PDF file", e);
            return ResponseEntity.badRequest().body("Error processing PDF file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error generating questions", e);
            return ResponseEntity.internalServerError().body("Error generating questions: " + e.getMessage());
        }
    }
    
    private String constructMultipleChoicePrompt(int numQuestions) {
        return String.format(
                "Generate %d multiple-choice questions with 4 options each, where exactly 1 option is correct. " +
                "Format each question as JSON in this exact structure: " +
                "[{\"id\": \"q1\", \"text\": \"Question text\", \"choices\": " +
                "[{\"text\": \"Option A\", \"correct\": true}, " +
                "{\"text\": \"Option B\", \"correct\": false}, " +
                "{\"text\": \"Option C\", \"correct\": false}, " +
                "{\"text\": \"Option D\", \"correct\": false}]}]",
                numQuestions
        );
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