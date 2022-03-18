package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import org.springframework.beans.factory.annotation.Autowired;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.WeeklyScoreRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
public class SamePercentage implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "samePercentage", orphanRemoval = true)
    private Set<WeeklyScore> weeklyScores = new HashSet<>();

    @OneToOne
    private WeeklyScore originWeeklyScore;

    public SamePercentage(){
    }

    public SamePercentage(WeeklyScore weeklyScore){setOriginWeeklyScore(weeklyScore);}

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
        if (weeklyScore.getId() == null) { //if weekly score id is not assigned throw an error
            throw new TutorException(WEEKLY_SCORE_NOT_FOUND);
        }
        if (weeklyScore.getId().equals(originWeeklyScore.getId())){ //check if weekly score given is the same as current instance
            throw new TutorException(CANNOT_ADD_SELF_TO_SAME_PERCENTAGE);
        }
        if (weeklyScores.stream().anyMatch(weeklyScore1 -> weeklyScore1.getId().equals(weeklyScore.getId()))) { //check if weekly score is already added to list
            throw new TutorException(WEEKLY_SCORE_ALREADY_ADDED);
        }
        weeklyScores.add(weeklyScore);
    }

    public void removeWeeklyScore(WeeklyScore weeklyScore){
        if (weeklyScore.getId() == null) { //if weekly score id is not assigned throw an error
            throw new TutorException(WEEKLY_SCORE_NOT_FOUND);
        }
        if (weeklyScore.getId().equals(originWeeklyScore.getId())){ //check if weekly score given is the same as current instance
            throw new TutorException(CANNOT_ADD_SELF_TO_SAME_PERCENTAGE);
        }
        if (weeklyScores.stream().noneMatch(weeklyScore1 -> weeklyScore1.getId().equals(weeklyScore.getId()))){  //check if weekly score given is in the weeklyscores hash set
            throw new TutorException(WEEKLY_SCORE_NOT_FOUND_SAME_PERCENTAGE, weeklyScore.getId());
        }
        weeklyScores.remove(weeklyScore); //remove weekly score from weeklyscores hash set
    }

    public void accept(Visitor visitor) {
    }
    public void remove(){
        for( WeeklyScore w : weeklyScores){  //go through weekly scores hash set and remove the original weekly score of the instance
            w.getSamePercentage().removeWeeklyScore(originWeeklyScore);
        }
        originWeeklyScore.setSamePercentage(null); //remove same percentage from weeklyscore
        originWeeklyScore = null; //reset weekly score in instance
    }
}
