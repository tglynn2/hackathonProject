package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class TeacherService {
      @Autowired 
    TeacherRepository teacherRepository;

     @PersistenceContext
    private EntityManager entityManager;

    public Teacher saveTeacher(Teacher teacher){
        return teacherRepository.save(teacher);
    }
}
