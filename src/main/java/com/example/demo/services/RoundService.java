package com.example.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.models.Round;
import com.example.demo.repositories.RoundRepository;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class RoundService {
      @Autowired 
    RoundRepository roundRepository;

     @PersistenceContext
    private EntityManager entityManager;

    public Round saveRound(Round round){
        return roundRepository.save(round);
    }
}
