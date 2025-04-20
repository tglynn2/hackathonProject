package com.example.demo.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;

import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;


    @ManyToOne
    @JoinColumn(name="l_id")
    @JsonBackReference
    private Lobby lobby;
    private UUID questionId;
    
    @Column(length = 1000)
    private String questionText;
    
    @ElementCollection
    @CollectionTable(name = "question_options", 
                    joinColumns = @JoinColumn(name = "question_id"))
    @MapKeyColumn(name = "option_text")
    @Column(name = "is_correct")
    private Map<String, Boolean> options = new HashMap<>();
    
    // Replace the ManyToMany with ManyToOne
    @ManyToOne
    @JoinColumn(name = "lobby_id")
    @JsonBackReference
    private Lobby lobby;
    
    @ManyToMany
    @JoinTable(
        name = "round_questions",
        joinColumns = @JoinColumn(name = "questionId"),
        inverseJoinColumns = @JoinColumn(name = "roundId")
    )
    private List<Round> rounds = new ArrayList<>();

    // Default constructor
    public Question() {
    }
    
    // Constructor with questionText and options
    public Question(String questionText, Map<String, Boolean> options) {
        this.questionText = questionText;
        this.options = options;
    }

    // Getters and setters
    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Map<String, Boolean> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Boolean> options) {
        this.options = options;
    }
    
    // Add a single option
    public void addOption(String optionText, boolean isCorrect) {
        this.options.put(optionText, isCorrect);
    }
    
    // Get correct option(s)
    public List<String> getCorrectOptions() {
        List<String> correctOptions = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : options.entrySet()) {
            if (entry.getValue()) {
                correctOptions.add(entry.getKey());
            }
        }
        return correctOptions;
    }

    // Add getter and setter for lobby
    public Lobby getLobby() {
        return lobby;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }
    
    public void addRound(Round round) {
        if (this.rounds == null) {
            this.rounds = new ArrayList<>();
        }
        this.rounds.add(round);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Question{id=").append(questionId)
          .append(", text='").append(questionText).append('\'')
          .append(", options={");
        
        for (Map.Entry<String, Boolean> entry : options.entrySet()) {
            sb.append("'").append(entry.getKey()).append("'")
              .append(":").append(entry.getValue()).append(", ");
        }
        if (!options.isEmpty()) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append("}}");
        
        return sb.toString();
    }
}