package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class TeacherService {
      @Autowired 
    TeacherRepository teacherRepository;

     @PersistenceContext
    private EntityManager entityManager;

    public Teacher saveTeacher(Teacher teacher){
        return teacherRepository.save(teacher);
    }
}
