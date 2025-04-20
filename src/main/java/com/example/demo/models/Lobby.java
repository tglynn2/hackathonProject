package com.example.demo.models;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
private UUID id;
@OneToOne(mappedBy = "lobby", cascade = CascadeType.ALL)
@JsonBackReference
private Teacher teacher;
@OneToMany(mappedBy = "lobby",cascade = CascadeType.ALL)
@JsonManagedReference
private List<Student> students;

@OneToMany(mappedBy = "lobby",cascade = CascadeType.ALL)
@JsonManagedReference
private List<Question> questions;

public Lobby(){}

public UUID getId() {
    return id;
}

public void setLobbyId(UUID id) {
    this.id = id;
}

public Teacher getTeacher() {
    return teacher;
}

public void setTeacher(Teacher teacher) {
    this.teacher = teacher;
}

public List<Student> getStudents() {
    return students;
}

public void setStudents(List<Student> students) {
    this.students = students;
}

public List<Question> getQuestions() {
    return questions;
}

public void setQuestions(List<Question> questions) {
    this.questions = questions;
}

}
