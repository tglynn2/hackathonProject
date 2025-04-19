package com.example.demo;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Student {
    @Id
    private UUID studentId;
    private int score;
    private String name;
    @ManyToOne
    @JoinColumn(name="t_id")
    @JsonBackReference
    private Teacher teacher;
    @ManyToOne
    @JoinColumn(name="l_id")
    @JsonBackReference
    private Lobby lobby;


}
