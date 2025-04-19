package com.example.demo.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Service to interact with the Google Gemini API for question generation.
 */
@Service
public class GeminiQuestionGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiQuestionGeneratorService.class);

    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;

    /**
     * Constructor for dependency injection.
     *
     * @param webClientBuilder Builder to create WebClient instances.
     * @param apiKey           Google Gemini API Key (from application properties).
     * @param apiUrl           Google Gemini API URL (from application properties).
     */
    public GeminiQuestionGeneratorService(
            WebClient.Builder webClientBuilder,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.url}") String apiUrl) {
        this.webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    /**
     * Generates questions based on the provided material using the Gemini API.
     *
     * @param material The text material to generate questions from.
     * @param promptInstruction The specific instruction for question generation
     * (e.g., "Generate 5 multiple-choice questions with answers").
     * @return A Mono containing the generated questions as a String, or an error signal.
     */
    public Mono<String> generateQuestions(String material, String promptInstruction) {
        // Construct the prompt for the API
        String fullPrompt = promptInstruction + "\n\nBased on the following text:\n---\n" + material + "\n---";

        // Create the request body structure
        GeminiRequest.Part part = new GeminiRequest.Part(fullPrompt);
        GeminiRequest.Content content = new GeminiRequest.Content(Collections.singletonList(part));
        GeminiRequest requestBody = new GeminiRequest(Collections.singletonList(content));

        logger.info("Sending request to Gemini API...");

        return this.webClient.post()
                // The specific model and action path are appended here
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/gemini-1.5-flash:generateContent") // Path relative to base URL
                        .queryParam("key", this.apiKey)
                        .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse.class) // Convert response body to GeminiResponse POJO
                .map(response -> {
                    // Extract the text from the response
                    // Add null checks for robustness
                    if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                        GeminiResponse.Candidate candidate = response.getCandidates().get(0);
                        if (candidate != null && candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
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

    // --- Static Inner Classes for Request/Response POJOs ---

    /**
     * Represents the request body structure for the Gemini API.
     */
    static class GeminiRequest {
        private List<Content> contents;

        // Constructor, Getters, Setters
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
     * Note: This is simplified; the actual API might have more fields (e.g., safetyRatings).
     */
    static class GeminiResponse {
        private List<Candidate> candidates;

        // Getters, Setters
        public List<Candidate> getCandidates() { return candidates; }
        public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }

        static class Candidate {
            private Content content;
            // Getters, Setters
            public Content getContent() { return content; }
            public void setContent(Content content) { this.content = content; }
        }

        static class Content {
            private List<Part> parts;
            private String role; // e.g., "model"
            // Getters, Setters
            public List<Part> getParts() { return parts; }
            public void setParts(List<Part> parts) { this.parts = parts; }
            public String getRole() { return role; }
            public void setRole(String role) { this.role = role; }
        }

        static class Part {
            private String text;
            // Getters, Setters
            public String getText() { return text; }
            public void setText(String text) { this.text = text; }
        }
    }
}
