package com.example.demo;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

public interface LobbyRepository extends CrudRepository<Lobby,UUID> {
    
}
