package com.example.demo.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.repositories.StudentRepository;


@Service
public class StudentService {
    @Autowired
    StudentRepository studentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Student saveStudent(Student student){return studentRepository.save(student);}


}
