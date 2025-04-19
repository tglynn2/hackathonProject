package com.example.demo;


import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface StudentRepository extends CrudRepository<Student, UUID> {


}
