package com.example.demo;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Teacher {
    @Id
    private UUID teacherId;
     @OneToMany(mappedBy = "teacher",cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Round> rounds;
    @OneToMany(mappedBy = "teacher",cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Student> students;    
    @OneToOne(cascade = CascadeType.ALL)
    private Lobby lobby;    

}
