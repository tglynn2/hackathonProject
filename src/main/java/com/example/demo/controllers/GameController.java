package com.example.demo.controllers;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.models.Question;
import com.example.demo.services.GameService;

@Controller
public class GameController {
    
    @Autowired
    private GameService gameService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Start a game for a lobby
     */
    @PostMapping("/game/start/{lobbyId}")
    @ResponseBody
    public Map<String, Object> startGame(@PathVariable UUID lobbyId) {
        gameService.startGame(lobbyId);
        return Map.of("status", "started", "lobbyId", lobbyId);
    }
    
    /**
     * Handle WebSocket message for answering questions
     */
    @MessageMapping("/game/answer")
    public void handleAnswer(@Payload Map<String, Object> payload) {
        UUID lobbyId = UUID.fromString((String) payload.get("lobbyId"));
        UUID studentId = UUID.fromString((String) payload.get("studentId"));
        UUID questionId = UUID.fromString((String) payload.get("questionId"));
        String answer = (String) payload.get("answer");
        
        gameService.processAnswer(lobbyId, studentId, questionId, answer);
    }
    
    /**
     * Get the next question for a student
     */
    @GetMapping("/game/question/{lobbyId}/{studentId}")
    @ResponseBody
    public Question getNextQuestion(
            @PathVariable UUID lobbyId, 
            @PathVariable UUID studentId) {
        return gameService.getNextQuestion(lobbyId, studentId);
    }
    
    /**
     * Check game status
     */
    @GetMapping("/game/status/{lobbyId}")
    @ResponseBody
    public Map<String, Object> getGameStatus(@PathVariable UUID lobbyId) {
        boolean active = gameService.isGameActive(lobbyId);
        return Map.of("active", active, "lobbyId", lobbyId);
    }
}