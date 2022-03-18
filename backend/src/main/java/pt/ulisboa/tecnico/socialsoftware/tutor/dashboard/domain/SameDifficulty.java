package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;

import java.util.Set;
import javax.persistence.Entity;
import java.util.HashSet;
import javax.persistence.*;

@Entity
public class SameDifficulty implements DomainEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Integer id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "percentage", orphanRemoval = true) 
    private Set<DifficultQuestion> difficultQuestions = new HashSet<>();

    @OneToOne
    private DifficultQuestion difficultQuestion;

    public SameDifficulty(){
    }

    public SameDifficulty(DifficultQuestion difficultQuestion){
        this.difficultQuestion = difficultQuestion;
    }

    public Integer getId() {
        return id;
    }

    public Set<DifficultQuestion> getDifficultQuestions() {
        return this.difficultQuestions;
    }

    public void addDifficultQuestion(DifficultQuestion difficultQuestion) {
        if (difficultQuestions.stream()
                .anyMatch(difficultQuestion1 -> difficultQuestion1.getQuestion().getId() == difficultQuestion.getQuestion().getId())) {
            throw new TutorException(ErrorMessage.DIFFICULT_QUESTION_ALREADY_CREATED);
        }
        difficultQuestions.add(difficultQuestion);
    }

    public void remove(){
        for (DifficultQuestion d: difficultQuestions){
            d.getSameDifficulty().difficultQuestions.remove(d);
        }
        difficultQuestion = null;

    }

    public void accept(Visitor visitor) {
    // TODO Auto-generated method stub
    }
}