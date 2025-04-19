package com.example.demo;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;

@Entity
public class Question {
    @Id
    private UUID questionId;
    @ManyToMany
    @JoinTable(
        name = "lobby_questions",
        joinColumns = @JoinColumn(name = "questionId"),
        inverseJoinColumns = @JoinColumn(name = "lobbyId")
    )
    private List<Lobby> lobbies;

    @ManyToMany
    @JoinTable(
        name = "round_questions",
        joinColumns = @JoinColumn(name = "questionId"),
        inverseJoinColumns = @JoinColumn(name = "roundId")
    )
    private List<Round> rounds;

    List<String> choices;
    String correctAnswer;
    String questionText;

    
}
