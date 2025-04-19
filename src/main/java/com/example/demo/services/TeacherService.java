package com.example.demo.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.models.Teacher;
import com.example.demo.repositories.TeacherRepository;

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
    public Optional<Teacher> getTeacherByName(String name){
        return teacherRepository.getTeacherByName(name);
    }
}
