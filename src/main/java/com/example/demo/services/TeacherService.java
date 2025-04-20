package com.example.demo.services;

import com.example.demo.models.Lobby;
import com.example.demo.models.Question;
import com.example.demo.models.Teacher;
import com.example.demo.repositories.LobbyRepository;
import com.example.demo.repositories.QuestionRepository;
import com.example.demo.repositories.TeacherRepository;
import com.example.demo.services.UnifiedQuestionService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TeacherService {
    @Autowired 
    TeacherRepository teacherRepository;
    
    @Autowired
    QuestionRepository questionRepository;
    
    @Autowired
    LobbyRepository lobbyRepository;
    
    @Autowired
    UnifiedQuestionService unifiedQuestionService;

    @PersistenceContext
    private EntityManager entityManager;

    // Existing methods
    public Teacher saveTeacher(Teacher teacher){
        return teacherRepository.save(teacher);
    }
    
    public Optional<Teacher> getTeacherByName(String name){
        return teacherRepository.getTeacherByName(name);
    }
    
    public Optional<Teacher> findById(UUID id){
        return teacherRepository.findById(id);
    }
    
    // New method for adding questions to a teacher's lobby
    @Transactional
    public Lobby addQuestionsToTeacherLobby(UUID teacherId, List<Question> questions) {
        // Find the teacher
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
        
        // Get the teacher's lobby
        Lobby lobby = teacher.getLobby();
        if (lobby == null) {
            throw new IllegalStateException("Teacher does not have a lobby");
        }
        
        // Initialize questions list if needed
        if (lobby.getQuestions() == null) {
            lobby.setQuestions(new ArrayList<>());
        }
        
        // Set the lobby reference for each question
        for (Question question : questions) {
            question.setLobby(lobby);
        }
        
        // Add questions to the lobby
        lobby.getQuestions().addAll(questions);
        
        // Save the questions and lobby
        questionRepository.saveAll(questions);
        return lobbyRepository.save(lobby);
    }
    
    // Method to generate questions from PDF for a teacher's lobby
    @Transactional
    public Lobby generateQuestionsForTeacherLobby(UUID teacherId, 
                                                String pdfContent, 
                                                int numQuestions, 
                                                String questionType) throws Exception {
        // Find the teacher
        Teacher teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new EntityNotFoundException("Teacher not found"));
        
        // Get the teacher's lobby
        Lobby lobby = teacher.getLobby();
        if (lobby == null) {
            throw new IllegalStateException("Teacher does not have a lobby");
        }
        
        // Generate questions
        List<Map<String, Object>> jsonQuestions = 
            unifiedQuestionService.generateQuestionsFromPDF(pdfContent, numQuestions, questionType);
        
        // Convert to Question entities
        List<Question> questions = new ArrayList<>();
        for (Map<String, Object> jsonQuestion : jsonQuestions) {
            Question question = unifiedQuestionService.createFromJson(jsonQuestion);
            question.setLobby(lobby);
            questions.add(question);
        }
        
        // Initialize questions list if needed
        if (lobby.getQuestions() == null) {
            lobby.setQuestions(new ArrayList<>());
        }
        
        // Add to lobby
        lobby.getQuestions().addAll(questions);
        
        // Save all
        questionRepository.saveAll(questions);
        return lobbyRepository.save(lobby);
    }
}