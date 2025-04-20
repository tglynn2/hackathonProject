package com.example.demo.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.models.Lobby;
import com.example.demo.models.Question;
import com.example.demo.models.Student;
import com.example.demo.repositories.StudentRepository;

@Service
public class GameService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private LobbyService lobbyService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Progress increment for correct answer (0-100 scale)
    private static final int PROGRESS_INCREMENT = 10;
    
    // Track active games by lobbyId
    private Map<UUID, Boolean> activeGames = new HashMap<>();
    
    // Track which questions students have answered
    private Map<UUID, Map<UUID, List<UUID>>> lobbyStudentQuestions = new HashMap<>();
    
    /**
     * Start a game for a specific lobby
     */
    public void startGame(UUID lobbyId) {
        // Mark game as active
        activeGames.put(lobbyId, true);
        
        // Reset all student scores to 0
        Optional<Lobby> lobbyOpt = lobbyService.findById(lobbyId);
        if (lobbyOpt.isPresent()) {
            Lobby lobby = lobbyOpt.get();
            if (lobby.getStudents() != null) {
                for (Student student : lobby.getStudents()) {
                    student.setScore(0);
                    studentRepository.save(student);
                }
            }
            
            // Initialize question tracking
            lobbyStudentQuestions.put(lobbyId, new HashMap<>());
            
            // Broadcast game start
            broadcastGameState(lobbyId);
        }
    }
    
    /**
     * Process a student's answer to a question
     */
    public void processAnswer(UUID lobbyId, UUID studentId, UUID questionId, String answer) {
        // Check if game is active
        if (!Boolean.TRUE.equals(activeGames.get(lobbyId))) {
            return;
        }
        
        // Get the student
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (!studentOpt.isPresent()) {
            return;
        }
        
        Student student = studentOpt.get();
        
        // Get the lobby and question
        Optional<Lobby> lobbyOpt = lobbyService.findById(lobbyId);
        if (!lobbyOpt.isPresent()) {
            return;
        }
        
        Lobby lobby = lobbyOpt.get();
        Question question = null;
        
        // Find the question
        if (lobby.getQuestions() != null) {
            for (Question q : lobby.getQuestions()) {
                if (q.getQuestionId().equals(questionId)) {
                    question = q;
                    break;
                }
            }
        }
        
        if (question == null) {
            return;
        }
        
        // Track that the student answered this question
        Map<UUID, List<UUID>> studentQuestions = lobbyStudentQuestions.get(lobbyId);
        if (studentQuestions == null) {
            studentQuestions = new HashMap<>();
            lobbyStudentQuestions.put(lobbyId, studentQuestions);
        }
        
        List<UUID> answeredQuestions = studentQuestions.get(studentId);
        if (answeredQuestions == null) {
            answeredQuestions = new ArrayList<>();
            studentQuestions.put(studentId, answeredQuestions);
        }
        
        // Don't allow answering the same question twice
        if (answeredQuestions.contains(questionId)) {
            return;
        }
        
        answeredQuestions.add(questionId);
        
        // Check if the answer is correct
        boolean correct = false;
        if (question.getOptions() != null) {
            Boolean isCorrect = question.getOptions().get(answer);
            correct = Boolean.TRUE.equals(isCorrect);
        }
        
        // Update score if correct
        if (correct) {
            int newScore = Math.min(100, student.getScore() + PROGRESS_INCREMENT);
            student.setScore(newScore);
            studentRepository.save(student);
            
            // Check if student has won
            if (newScore >= 100) {
                endGame(lobbyId, studentId);
            }
        }
        
        // Broadcast updated game state
        broadcastGameState(lobbyId);
    }
    
    /**
     * End the game and announce winner
     */
    public void endGame(UUID lobbyId, UUID winnerId) {
        activeGames.put(lobbyId, false);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GAME_OVER");
        payload.put("lobbyId", lobbyId);
        payload.put("winnerId", winnerId);
        
        // Get winner name
        Optional<Student> winnerOpt = studentRepository.findById(winnerId);
        if (winnerOpt.isPresent()) {
            payload.put("winnerName", winnerOpt.get().getName());
        }
        
        messagingTemplate.convertAndSend("/topic/game/" + lobbyId, payload);
    }
    
    /**
     * Get the next question for a student that they haven't answered yet
     */
    public Question getNextQuestion(UUID lobbyId, UUID studentId) {
        Optional<Lobby> lobbyOpt = lobbyService.findById(lobbyId);
        if (!lobbyOpt.isPresent() || !Boolean.TRUE.equals(activeGames.get(lobbyId))) {
            return null;
        }
        
        Lobby lobby = lobbyOpt.get();
        if (lobby.getQuestions() == null || lobby.getQuestions().isEmpty()) {
            return null;
        }
        
        // Get questions student has already answered
        Map<UUID, List<UUID>> studentQuestions = lobbyStudentQuestions.get(lobbyId);
        List<UUID> answeredQuestions = studentQuestions != null ? 
                                        studentQuestions.get(studentId) : null;
        
        if (answeredQuestions == null) {
            answeredQuestions = new ArrayList<>();
        }
        
        // Find first unanswered question
        for (Question question : lobby.getQuestions()) {
            if (!answeredQuestions.contains(question.getQuestionId())) {
                return question;
            }
        }
        
        return null; // All questions answered
    }
    
    /**
     * Broadcast current game state to all clients
     */
    public void broadcastGameState(UUID lobbyId) {
        Optional<Lobby> lobbyOpt = lobbyService.findById(lobbyId);
        if (!lobbyOpt.isPresent()) {
            return;
        }
        
        Lobby lobby = lobbyOpt.get();
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GAME_STATE");
        payload.put("lobbyId", lobbyId);
        payload.put("active", Boolean.TRUE.equals(activeGames.get(lobbyId)));
        
        if (lobby.getStudents() != null) {
            List<Map<String, Object>> playerData = new ArrayList<>();
            
            for (Student student : lobby.getStudents()) {
                Map<String, Object> player = new HashMap<>();
                player.put("id", student.getId());
                player.put("name", student.getName());
                player.put("progress", student.getScore());
                
                playerData.add(player);
            }
            
            payload.put("players", playerData);
        }
        
        messagingTemplate.convertAndSend("/topic/game/" + lobbyId, payload);
    }
    
    /**
     * Check if a game is currently active
     */
    public boolean isGameActive(UUID lobbyId) {
        return Boolean.TRUE.equals(activeGames.get(lobbyId));
    }
}