package com.example.demo.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.models.Lobby;
import com.example.demo.models.Student;
import com.example.demo.models.Teacher;
import com.example.demo.services.LobbyService;
import com.example.demo.services.StudentService;
import com.example.demo.services.TeacherService;


@RequestMapping("/students")
@RestController
public class StudentController {

    @Autowired
    StudentService studentService;
    @Autowired 
    TeacherService teacherService;
    @Autowired
    LobbyService lobbyService;

    @PostMapping("/{lobbyId}")
    public Student addStudentToLobby(
        @PathVariable("lobbyId") UUID lobbyId,
        @RequestParam(required = true) String name){
            Lobby lobby = lobbyService.findById(lobbyId).orElse(null);
            Teacher teacher = lobby.getTeacher();
            Student student = new Student();
            student.setName(name);
            student.setLobby(lobby);
            student.setTeacher(teacher);
            lobby.getStudents().add(student);
            teacher.getStudents().add(student);
            return studentService.saveStudent(student);


        
    }
}














