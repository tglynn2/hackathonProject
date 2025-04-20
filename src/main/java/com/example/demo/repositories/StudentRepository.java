package com.example.demo.repositories;


import org.springframework.data.repository.CrudRepository;

import com.example.demo.models.Student;

import java.util.UUID;

public interface StudentRepository extends CrudRepository<Student, UUID> {


}
