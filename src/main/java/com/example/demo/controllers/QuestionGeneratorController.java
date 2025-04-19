package com.example.demo.controllers;

import com.example.demo.services.GeminiQuestionGeneratorService;
import com.example.demo.services.PDFProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/questions")
public class QuestionGeneratorController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionGeneratorController.class);
    
    private final GeminiQuestionGeneratorService questionGeneratorService;
    private final PDFProcessingService pdfProcessingService;
    
    @Autowired
    public QuestionGeneratorController(
            GeminiQuestionGeneratorService questionGeneratorService,
            PDFProcessingService pdfProcessingService) {
        this.questionGeneratorService = questionGeneratorService;
        this.pdfProcessingService = pdfProcessingService;
    }
    
    @PostMapping("/generate-from-pdf")
    public ResponseEntity<String> generateQuestionsFromPDF(
            @RequestParam("file") MultipartFile pdfFile,
            @RequestParam(value = "numQuestions", defaultValue = "10") int numQuestions,
            @RequestParam(value = "questionType", defaultValue = "multiple-choice") String questionType) {
        
        try {
            // Extract text from PDF
            String pdfContent = pdfProcessingService.extractTextFromPDF(pdfFile);
            
            // Construct prompt for question generation
            String prompt = constructQuestionPrompt(numQuestions, questionType);
            
            // Generate questions using the Gemini API
            String questions = questionGeneratorService.generateQuestions(pdfContent, prompt)
                    .block(); // Blocking call to get the result
            
            return ResponseEntity.ok(questions);
        } catch (IOException e) {
            logger.error("Error processing PDF file", e);
            return ResponseEntity.badRequest().body("Error processing PDF file: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error generating questions", e);
            return ResponseEntity.internalServerError().body("Error generating questions: " + e.getMessage());
        }
    }
    
    private String constructQuestionPrompt(int numQuestions, String questionType) {
        if ("multiple-choice".equals(questionType)) {
            return String.format(
                    "Generate %d multiple-choice questions with 4 options each, where exactly 1 option is correct. " +
                    "Format each question as JSON in this exact structure: " +
                    "[{\"id\": \"q1\", \"text\": \"Question text\", \"choices\": " +
                    "[{\"text\": \"Option A\", \"correct\": true}, " +
                    "{\"text\": \"Option B\", \"correct\": false}, ...]}]",
                    numQuestions
            );
        } else if ("true-false".equals(questionType)) {
            return String.format(
                    "Generate %d true/false questions. Format each question as JSON in this exact structure: " +
                    "[{\"id\": \"q1\", \"text\": \"Question text\", \"answer\": true}]",
                    numQuestions
            );
        } else {
            return String.format(
                    "Generate %d %s questions based on the provided content. " +
                    "Format the output as a JSON array of question objects.",
                    numQuestions, questionType
            );
        }
    }
}