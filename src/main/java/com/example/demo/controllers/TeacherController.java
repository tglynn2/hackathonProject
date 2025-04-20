package com.example.demo.controllers;

import com.example.demo.models.Lobby;
import com.example.demo.models.Question;
import com.example.demo.models.Teacher;
import com.example.demo.services.LobbyService;
import com.example.demo.services.TeacherService;
import com.example.demo.services.UnifiedQuestionService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
@RequestMapping("/teachers")
@RestController
public class TeacherController {

    @Autowired
    TeacherService teacherService;
    
    @Autowired
    LobbyService lobbyService;
    
    @Autowired
    UnifiedQuestionService questionService;

    // Existing methods
    @PostMapping("/create")
    public Teacher createTeacher(@RequestParam(required = true) String name){
        Teacher teacher = new Teacher();
        teacher.setName(name);
        return teacherService.saveTeacher(teacher);
    }

    @GetMapping("/login")
    public Teacher signInTeacher(@RequestParam(required = true) String name, HttpServletRequest request){
        Teacher teacher = teacherService.getTeacherByName(name).orElse(null);
        if(teacher != null){
            request.getSession().setAttribute("id", teacher.getId());
        }
        return teacher;
    }

    @PostMapping("/newLobby")
    public Lobby createLobby(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        UUID id = UUID.fromString(session.getAttribute("id").toString());
        Teacher teacher = teacherService.findById(id).orElse(null);
        Lobby lobby = new Lobby();
        lobby.setTeacher(teacher);
        teacher.setLobby(lobby);
        teacherService.saveTeacher(teacher);
        return lobby;
    }
    
    // New method to add questions to a teacher's lobby
    @PostMapping("/lobby/questions")
    public ResponseEntity<?> addQuestionsToLobby(
            HttpServletRequest request,
            @RequestBody List<Question> questions) {
        
        try {
            // Get teacher ID from session
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("id") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
            }
            
            UUID teacherId = UUID.fromString(session.getAttribute("id").toString());
            
            // Add questions to the teacher's lobby
            Lobby updatedLobby = teacherService.addQuestionsToTeacherLobby(teacherId, questions);
            
            return ResponseEntity.ok(updatedLobby);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding questions: " + e.getMessage());
        }
    }
    
    // Generate questions from PDF for a teacher's lobby
    @PostMapping("/lobby/generate-questions")
    public ResponseEntity<?> generateQuestionsForLobby(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile pdfFile,
            @RequestParam(value = "numQuestions", defaultValue = "10") int numQuestions,
            @RequestParam(value = "questionType", defaultValue = "multiple-choice") String questionType) {
        
        try {
            // Get teacher ID from session
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("id") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
            }
            
            UUID teacherId = UUID.fromString(session.getAttribute("id").toString());
            
            // Extract text from PDF
            String pdfContent = questionService.extractTextFromPDF(pdfFile);
            
            // Generate questions and add to the teacher's lobby
            Lobby updatedLobby = teacherService.generateQuestionsForTeacherLobby(
                teacherId, pdfContent, numQuestions, questionType);
            
            return ResponseEntity.ok(updatedLobby);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating questions: " + e.getMessage());
        }
    }
    
    // Get questions for the current teacher's lobby
    @GetMapping("/lobby/questions")
    public ResponseEntity<?> getLobbyQuestions(HttpServletRequest request) {
        try {
            // Get teacher ID from session
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("id") == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
            }
            
            UUID teacherId = UUID.fromString(session.getAttribute("id").toString());
            
            // Find the teacher
            Teacher teacher = teacherService.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
            
            // Get the teacher's lobby
            Lobby lobby = teacher.getLobby();
            if (lobby == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Teacher does not have a lobby");
            }
            
            // Return the questions
            return ResponseEntity.ok(lobby.getQuestions());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching questions: " + e.getMessage());
        }
    }
}