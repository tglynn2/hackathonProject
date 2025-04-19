package com.example.demo.models;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID questionId;


    @ManyToOne
    @JoinColumn(name="l_id")
    @JsonBackReference
    private Lobby lobby;

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
