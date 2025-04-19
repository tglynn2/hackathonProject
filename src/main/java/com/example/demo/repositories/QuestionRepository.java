package com.example.demo.repositories;


import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.models.Question;


public interface QuestionRepository extends CrudRepository<Question,UUID> {

    
}
