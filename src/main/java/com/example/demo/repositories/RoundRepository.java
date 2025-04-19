package com.example.demo.repositories;


import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.models.Round;


public interface RoundRepository extends CrudRepository<Round,UUID> {

    
}
