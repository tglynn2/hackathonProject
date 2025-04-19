package com.example.demo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class StudentService {
    @Autowired
    StudentRepository studentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public Student saveStudent(Student student){return studentRepository.save(student);}


}
