package com.example.demo.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.example.demo.models.Teacher;

public interface TeacherRepository extends CrudRepository<Teacher,UUID> {

    public Optional<Teacher> getTeacherByName(String name);

    public Optional<Teacher> findById(UUID id);

    
}
