package com.example.demo.services;

import com.example.demo.models.Question;
import com.example.demo.repositories.QuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unified service for handling all question-related functionality:
 * - Question generation via AI
 * - PDF processing
 * - JSON parsing
 * - Database operations
 */
@Service
public class UnifiedQuestionService {
    
    private static final Logger logger = LoggerFactory.getLogger(UnifiedQuestionService.class);
    
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;
    
    /**
     * Constructor for dependency injection.
     */
    public UnifiedQuestionService(
            QuestionRepository questionRepository,
            ObjectMapper objectMapper,
            WebClient.Builder webClientBuilder,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.url}") String apiUrl) {
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }
    
    // --- PDF Processing Methods ---
    
    /**
     * Extracts text content from a PDF file
     */
    public String extractTextFromPDF(MultipartFile pdfFile) throws IOException {
        logger.info("Extracting text from PDF: {}", pdfFile.getOriginalFilename());
        
        try (org.apache.pdfbox.pdmodel.PDDocument document = 
                org.apache.pdfbox.pdmodel.PDDocument.load(pdfFile.getInputStream())) {
            org.apache.pdfbox.text.PDFTextStripper textStripper = new org.apache.pdfbox.text.PDFTextStripper();
            String text = textStripper.getText(document);
            
            logger.info("Successfully extracted {} characters of text", text.length());
            return text;
        } catch (IOException e) {
            logger.error("Failed to extract text from PDF", e);
            throw e;
        }
    }
    
    // --- Question Generation Methods ---
    
    /**
     * Generates questions from PDF content
     */
    public List<Map<String, Object>> generateQuestionsFromPDF(
            String pdfContent, 
            int numQuestions, 
            String questionType) throws Exception {
        
        // Construct prompt based on question type
        String prompt;
        if ("multiple-choice".equals(questionType)) {
            prompt = constructMultipleChoicePrompt(numQuestions);
        } else if ("true-false".equals(questionType)) {
            prompt = constructTrueFalsePrompt(numQuestions);
        } else if ("short-answer".equals(questionType)) {
            prompt = constructShortAnswerPrompt(numQuestions);
        } else {
            prompt = constructMultipleChoicePrompt(numQuestions);
        }
        
        // Generate questions using the Gemini API
        String aiResponse = generateQuestions(pdfContent, prompt).block();
        
        // Log the raw response for debugging
        logger.debug("Raw response from AI: {}", aiResponse);
        
        // Parse the AI response to extract valid JSON
        return parseQuestions(aiResponse);
    }
    
    /**
     * Constructs a prompt for multiple-choice questions
     */
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
    
    /**
     * Constructs a prompt for true/false questions
     */
    private String constructTrueFalsePrompt(int numQuestions) {
        return String.format(
                "Generate %d true/false questions based on the content. " +
                "Format each question as JSON in this exact structure: " +
                "[{\"id\": \"q1\", \"text\": \"Question text\", \"choices\": " +
                "[{\"text\": \"True\", \"correct\": true}, " +
                "{\"text\": \"False\", \"correct\": false}]}]",
                numQuestions
        );
    }
    
    /**
     * Constructs a prompt for short answer questions
     */
    private String constructShortAnswerPrompt(int numQuestions) {
        return String.format(
                "Generate %d short answer questions based on the content. " +
                "Format each question as JSON in this exact structure: " +
                "[{\"id\": \"q1\", \"text\": \"Question text\", \"answer\": \"Correct answer\"}]",
                numQuestions
        );
    }
    
    /**
     * Sends a request to the Gemini API to generate questions
     */
    public Mono<String> generateQuestions(String material, String promptInstruction) {
        // Construct the prompt for the API with clearer instructions about JSON formatting
        String fullPrompt = promptInstruction + 
                "\n\nBased on the following text:\n---\n" + material + "\n---\n\n" +
                "IMPORTANT: Respond ONLY with the valid JSON array. Do not include any explanations, markdown, or code blocks.";

        // Create the request body structure
        GeminiRequest.Part part = new GeminiRequest.Part(fullPrompt);
        GeminiRequest.Content content = new GeminiRequest.Content(Collections.singletonList(part));
        GeminiRequest requestBody = new GeminiRequest(Collections.singletonList(content));

        logger.info("Sending request to Gemini API...");

        return this.webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/gemini-1.5-flash:generateContent")
                        .queryParam("key", this.apiKey)
                        .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(response -> {
                    if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                        GeminiResponse.Candidate candidate = response.getCandidates().get(0);
                        if (candidate != null && candidate.getContent() != null && 
                                candidate.getContent().getParts() != null && 
                                !candidate.getContent().getParts().isEmpty()) {
                            GeminiResponse.Part responsePart = candidate.getContent().getParts().get(0);
                            if (responsePart != null && responsePart.getText() != null) {
                                logger.info("Successfully received response from Gemini API.");
                                return responsePart.getText();
                            }
                        }
                    }
                    logger.warn("Received incomplete or empty response from Gemini API.");
                    return "Error: Could not extract valid content from Gemini response.";
                })
                .doOnError(error -> logger.error("Error calling Gemini API: {}", error.getMessage(), error))
                .onErrorResume(error -> Mono.just("Error generating questions: " + error.getMessage()));
    }
    
    // --- JSON Parsing Methods ---
    
    /**
     * Extracts and parses valid JSON from an AI response
     */
    public List<Map<String, Object>> parseQuestions(String aiResponse) throws JsonProcessingException {
        logger.info("Parsing AI response for questions");
        
        // First, try to parse directly in case we got clean JSON
        try {
            return objectMapper.readValue(aiResponse, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            logger.info("Direct JSON parsing failed, attempting to extract JSON from formatted response");
        }
        
        // Extract JSON array from markdown code blocks if present
        String extractedJson = extractJsonFromMarkdown(aiResponse);
        if (extractedJson == null) {
            // Try to clean the response and extract JSON
            extractedJson = cleanAndExtractJson(aiResponse);
        }
        
        if (extractedJson == null) {
            logger.error("Failed to extract valid JSON from AI response");
            throw new JsonProcessingException("Could not extract valid JSON from AI response") {};
        }
        
        logger.info("Successfully extracted JSON from AI response");
        return objectMapper.readValue(extractedJson, new TypeReference<List<Map<String, Object>>>() {});
    }
    
    /**
     * Extracts JSON from markdown code blocks
     */
    private String extractJsonFromMarkdown(String text) {
        // Look for code blocks with JSON content
        Pattern pattern = Pattern.compile("```(?:json)?\\s*\\n?(\\[\\s*\\{.*?\\}\\s*\\])\\s*```", 
                                         Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }
    
    /**
     * Attempts to clean the text and extract valid JSON
     */
    private String cleanAndExtractJson(String text) {
        // Look for anything that looks like a JSON array of objects
        Pattern pattern = Pattern.compile("\\[\\s*\\{.*?\\}\\s*\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group(0);
        }
        
        return null;
    }
    
    // --- Database Operations Methods ---
    
    /**
     * Saves a list of questions to the database
     */
    public List<Question> saveQuestions(List<Map<String, Object>> jsonQuestions) {
        List<Question> questions = new ArrayList<>();
        for (Map<String, Object> jsonQuestion : jsonQuestions) {
            Question question = createFromJson(jsonQuestion);
            questions.add(question);
        }
        return saveAllQuestions(questions);
    }
    
    /**
     * Creates a Question entity from a JSON map
     */
    @SuppressWarnings("unchecked")
    public Question createFromJson(Map<String, Object> jsonQuestion) {
        Question question = new Question();
        
        // Set question text
        question.setQuestionText((String) jsonQuestion.get("text"));
        
        // Process choices if they exist
        if (jsonQuestion.containsKey("choices")) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) jsonQuestion.get("choices");
            
            for (Map<String, Object> choice : choices) {
                String text = (String) choice.get("text");
                Boolean correct = (Boolean) choice.get("correct");
                
                if (text != null && correct != null) {
                    question.addOption(text, correct);
                }
            }
        } 
        // For short answer questions with a single answer
        else if (jsonQuestion.containsKey("answer")) {
            String answer = (String) jsonQuestion.get("answer");
            question.addOption(answer, true);
        }
        
        return question;
    }
    
    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }
    
    public List<Question> saveAllQuestions(List<Question> questions) {
        Iterable<Question> savedQuestions = questionRepository.saveAll(questions);
        List<Question> result = new ArrayList<>();
        savedQuestions.forEach(result::add);
        return result;
    }
    
    public Optional<Question> getQuestionById(UUID id) {
        return questionRepository.findById(id);
    }
    
    public List<Question> getAllQuestions() {
        List<Question> questions = new ArrayList<>();
        questionRepository.findAll().forEach(questions::add);
        return questions;
    }
    
    // --- Inner Classes for Gemini API ---
    
    /**
     * Represents the request body structure for the Gemini API.
     */
    static class GeminiRequest {
        private List<Content> contents;

        public GeminiRequest(List<Content> contents) {
            this.contents = contents;
        }
        public List<Content> getContents() { return contents; }
        public void setContents(List<Content> contents) { this.contents = contents; }

        static class Content {
            private List<Part> parts;
            public Content(List<Part> parts) { this.parts = parts; }
            public List<Part> getParts() { return parts; }
            public void setParts(List<Part> parts) { this.parts = parts; }
        }

        static class Part {
            private String text;
            public Part(String text) { this.text = text; }
            public String getText() { return text; }
            public void setText(String text) { this.text = text; }
        }
    }

    /**
     * Represents the response body structure from the Gemini API.
     */
    static class GeminiResponse {
        private List<Candidate> candidates;

        public List<Candidate> getCandidates() { return candidates; }
        public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }

        static class Candidate {
            private Content content;
            public Content getContent() { return content; }
            public void setContent(Content content) { this.content = content; }
        }

        static class Content {
            private List<Part> parts;
            private String role;
            public List<Part> getParts() { return parts; }
            public void setParts(List<Part> parts) { this.parts = parts; }
            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }
        }

        static class Part {
            private String text;
            public String getText() { return text; }
            public void setText(String text) { this.text = text; }
        }
    }
}