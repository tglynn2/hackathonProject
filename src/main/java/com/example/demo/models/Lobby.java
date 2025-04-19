package com.example.demo.models;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Lobby {
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private UUID lobbyId;
@OneToOne(mappedBy = "lobby", cascade = CascadeType.ALL)
private Teacher teacher;
@OneToMany(mappedBy = "lobby",cascade = CascadeType.ALL)
@JsonManagedReference
private List<Student> students;
}
