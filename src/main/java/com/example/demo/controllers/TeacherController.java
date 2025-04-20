package com.example.demo.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.services.LobbyService;
import com.example.demo.services.TeacherService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.example.demo.models.Lobby;
import com.example.demo.models.Teacher;

@RequestMapping("/teachers")
@RestController
public class TeacherController {

    @Autowired
    TeacherService teacherService;
    @Autowired
    LobbyService lobbyService;

    @PostMapping("/create")
    public Teacher createTeacher(@RequestParam(required = true) String name){
        Teacher teacher = new Teacher();
        teacher.setName(name);
        return teacherService.saveTeacher(teacher);
    }

    @GetMapping("/login")
    public Teacher signInTeacher(@RequestParam(required = true) String name,HttpServletRequest request){
        Teacher teacher =  teacherService.getTeacherByName(name).orElse(null);
        if(teacher != null){
            request.getSession().setAttribute("id", teacher.getId());
        }
        return teacher;
    }

    @PostMapping("/newLobby")
    public Lobby createLobby(HttpServletRequest request){
      HttpSession session = request.getSession(false);
       UUID id  = UUID.fromString(session.getAttribute("id").toString());
        Teacher teacher = teacherService.findById(id).orElse(null);
        Lobby lobby = new Lobby();
        lobby.setTeacher(teacher);
        teacher.setLobby(lobby);
        teacherService.saveTeacher(teacher);
        return lobby;
       
    }
} 
