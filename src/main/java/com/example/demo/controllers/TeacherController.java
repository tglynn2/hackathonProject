package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.services.TeacherService;

import jakarta.servlet.http.HttpServletRequest;

import com.example.demo.models.Teacher;

@RequestMapping("/teachers")
@RestController
public class TeacherController {

    @Autowired
    TeacherService teacherService;

    @PostMapping
    public Teacher createTeacher(@RequestParam(required = true) String name){
        Teacher teacher = new Teacher();
        teacher.setName(name);
        return teacherService.saveTeacher(teacher);
    }

    @GetMapping
    public Teacher signInTeacher(@RequestParam(required = true) String name,HttpServletRequest request){
        Teacher teacher =  teacherService.getTeacherByName(name).orElse(null);
        if(teacher != null){
            request.getSession().setAttribute("id", teacher.getTeacherId());
        }
        return teacher;
    }
} 
