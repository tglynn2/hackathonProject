package com.example.demo.models;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
public class GameSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @OneToOne
    private Lobby lobby;
    
    @Enumerated(EnumType.STRING)
    private GameState status = GameState.WAITING;
    
    // Track which questions each student has answered
    @ElementCollection
    @CollectionTable(name = "student_answered_questions", 
                   joinColumns = @JoinColumn(name = "game_session_id"))
    @MapKeyColumn(name = "student_id")
    private Map<UUID, List<UUID>> studentAnsweredQuestions = new HashMap<>();
    
    // Store the winner when game is completed
    private UUID winnerStudentId;
    
    public enum GameState {
        WAITING, ACTIVE, COMPLETED
    }
    
    // Default constructor
    public GameSession() {
    }
    
    // Constructor with lobby
    public GameSession(Lobby lobby) {
        this.lobby = lobby;
        this.status = GameState.WAITING;
    }
    
    // Getters and setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public Lobby getLobby() {
        return lobby;
    }
    
    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }
    
    public GameState getStatus() {
        return status;
    }
    
    public void setStatus(GameState status) {
        this.status = status;
    }
    
    public Map<UUID, List<UUID>> getStudentAnsweredQuestions() {
        return studentAnsweredQuestions;
    }
    
    public void setStudentAnsweredQuestions(Map<UUID, List<UUID>> studentAnsweredQuestions) {
        this.studentAnsweredQuestions = studentAnsweredQuestions;
    }
    
    public UUID getWinnerStudentId() {
        return winnerStudentId;
    }
    
    public void setWinnerStudentId(UUID winnerStudentId) {
        this.winnerStudentId = winnerStudentId;
    }
    
    // Methods to track student progress
    public void recordAnswer(UUID studentId, UUID questionId, boolean correct) {
        // Record the question as answered
        studentAnsweredQuestions.computeIfAbsent(studentId, k -> new ArrayList<>())
                               .add(questionId);
    }
    
    // Check if a student has answered a specific question
    public boolean hasStudentAnsweredQuestion(UUID studentId, UUID questionId) {
        List<UUID> answeredQuestions = studentAnsweredQuestions.get(studentId);
        return answeredQuestions != null && answeredQuestions.contains(questionId);
    }
    
    // Get all questions a student has answered
    public List<UUID> getStudentAnsweredQuestionIds(UUID studentId) {
        return studentAnsweredQuestions.getOrDefault(studentId, new ArrayList<>());
    }
    
    // Get number of questions a student has answered
    public int getStudentAnsweredQuestionsCount(UUID studentId) {
        List<UUID> answeredQuestions = studentAnsweredQuestions.get(studentId);
        return answeredQuestions != null ? answeredQuestions.size() : 0;
    }
    
    // Set game as completed with a winner
    public void completeGame(UUID winnerId) {
        this.status = GameState.COMPLETED;
        this.winnerStudentId = winnerId;
    }
    
    // Start the game
    public void startGame() {
        this.status = GameState.ACTIVE;
    }
    
    // Check if game is active
    public boolean isActive() {
        return this.status == GameState.ACTIVE;
    }
    
    // Check if game is completed
    public boolean isCompleted() {
        return this.status == GameState.COMPLETED;
    }
    
    @Override
    public String toString() {
        return "GameSession{" +
                "id=" + id +
                ", status=" + status +
                ", lobbyId=" + (lobby != null ? lobby.getId() : null) +
                ", studentsAnswered=" + studentAnsweredQuestions.size() +
                ", winnerStudentId=" + winnerStudentId +
                '}';
    }
}