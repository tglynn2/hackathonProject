package com.example.demo.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
public class Lobby {
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private UUID id;
@OneToOne(mappedBy = "lobby", cascade = CascadeType.ALL)
@JsonBackReference
private Teacher teacher;
@OneToMany(mappedBy = "lobby",cascade = CascadeType.ALL)
@JsonManagedReference
private List<Student> students;

@OneToMany(mappedBy = "lobby",cascade = CascadeType.ALL)
@JsonManagedReference
private List<Question> questions;

public Lobby(){}

public UUID getId() {
    return id;
}

public void setLobbyId(UUID id) {
    this.id = id;
}

public Teacher getTeacher() {
    return teacher;
}

public void setTeacher(Teacher teacher) {
    this.teacher = teacher;
}

public List<Student> getStudents() {
    return students;
}

public void setStudents(List<Student> students) {
    this.students = students;
}

public List<Question> getQuestions() {
    return questions;
}

public void setQuestions(List<Question> questions) {
    this.questions = questions;
}











    @Enumerated(EnumType.STRING)
    private GameState currentState = GameState.WAITING_FOR_PLAYERS;

    private int currentQuestionIndex = 10;


    @Transient
    private Map<UUID, String> currentAnswers = new ConcurrentHashMap<>();
    //player id as key and answer


    //this somwehere
    //if ( currentAnswers.size = 4) assuming 4 players, we need a player amount variable
    //{
     //   transition ( ALL_ANSWERED)
   // }








    public enum Event {
        START_GAME,
        TIMER_EXPIRED,
        ALL_ANSWERED,
        NEXT_QUESTION
    }

    //needed
    // a timer for how long the question is up
    //how are getting the questions, are they in an array
    //where do we pull answers from


    public enum GameState {
        WAITING_FOR_PLAYERS,
        SHOW_QUESTION,
        COLLECT_ANSWERS,
        MOVE_HORSES,
        FINISHED
    }






    public GameState transition(Event e) {
        switch (currentState) {
            case WAITING_FOR_PLAYERS:

                if (e == Event.START_GAME) {

                    currentState = GameState.SHOW_QUESTION;
                }
                break;

            case SHOW_QUESTION:
                // Either time ran out or everyone answered
                if (e == Event.TIMER_EXPIRED || e == Event.ALL_ANSWERED) {
                    currentState = GameState.COLLECT_ANSWERS;
                }
                break;

            case COLLECT_ANSWERS:
                // Now that all answers are in, advance to moving horses

                if (e == Event.ALL_ANSWERED) {
                    currentState = GameState.MOVE_HORSES;
                    //   moveHorses();     method to move horses
                    currentAnswers.clear();    //empty cue
                }
                break;

            case MOVE_HORSES:
                //change question
                if (e == Event.NEXT_QUESTION) {
                    if (currentQuestionIndex + 1 < questions.size()) {
                        currentQuestionIndex++;
                        currentState = GameState.SHOW_QUESTION;
                    } else {
                        currentState = GameState.FINISHED;
                    }
                }
                break;

            case FINISHED:

                break;
        }

        return currentState;
    }




    int tempPoints = 5;


    private void moveHorses() {
        Question q = questions.get(currentQuestionIndex);

        for (Student s : students) {
            String answer = currentAnswers.get(s.getId());


            boolean correct = q.isCorrect(answer);

            if (correct) {

                s.setScore(s.getScore() + tempPoints);
            }
        }
        currentAnswers.clear();
    }


















}



