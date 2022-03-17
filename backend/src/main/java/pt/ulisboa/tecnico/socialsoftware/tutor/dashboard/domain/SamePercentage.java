package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class SamePercentage implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany
    private Set<WeeklyScore> weeklyScores = new HashSet<>();

    @OneToOne
    private WeeklyScore originWeeklyScore;

    public SamePercentage(){
    }

    public SamePercentage(WeeklyScore weeklyScore){
        setOriginWeeklyScore(weeklyScore);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setOriginWeeklyScore(WeeklyScore originWeeklyScore) {
        this.originWeeklyScore = originWeeklyScore;
    }

    public WeeklyScore getOriginWeeklyScore() {
        return originWeeklyScore;
    }

    public Set<WeeklyScore> getWeeklyScores() {
        return weeklyScores;
    }

    public void addWeeklyScore(WeeklyScore weeklyScore){
        if (weeklyScore.getId().equals(originWeeklyScore.getId())){
            throw new TutorException(ErrorMessage.CANNOT_ADD_SELF_TO_SAME_PERCENTAGE);
        }
        if (weeklyScores.stream().anyMatch(weeklyScore1 -> weeklyScore1.getId().equals(weeklyScore.getId()))) {
            throw new TutorException(ErrorMessage.WEEKLY_SCORE_ALREADY_ADDED);
        }
        weeklyScores.add(weeklyScore);
    }

    public void removeWeeklyScore(WeeklyScore weeklyScore){
        //to implement
    }

    public void accept(Visitor visitor) {
    }
}
