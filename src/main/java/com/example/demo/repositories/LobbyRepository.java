package com.example.demo.repositories;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.models.Lobby;

public interface LobbyRepository extends CrudRepository<Lobby,UUID> {

    
    
}
