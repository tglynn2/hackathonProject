package com.example.demo.repositories;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.models.Lobby;
import com.example.demo.models.Teacher;

public interface LobbyRepository extends CrudRepository<Lobby,UUID> {

    public Optional<Lobby> findByTeacher(Teacher teacher);

    public Optional<Lobby> findById(UUID id);
    
    
}
