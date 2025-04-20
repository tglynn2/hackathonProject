package com.example.demo.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.models.Lobby;
import com.example.demo.models.Teacher;
import com.example.demo.repositories.LobbyRepository;
import com.example.demo.repositories.TeacherRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class LobbyService {
      @Autowired 
    LobbyRepository lobbyRepository;

     @PersistenceContext
    private EntityManager entityManager;

    public Lobby saveLobby(Lobby lobby){
        return lobbyRepository.save(lobby);
    }
    public Optional<Lobby> getLobbyByTeacher(Teacher teacher){
        return lobbyRepository.findByTeacher(teacher);
    }

    public Optional<Lobby> findById(UUID id){
        return lobbyRepository.findById(id);
    }
}
