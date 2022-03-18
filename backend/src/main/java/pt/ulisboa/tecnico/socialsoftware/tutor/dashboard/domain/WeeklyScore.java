package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.WEEKLY_SCORE_ALREADY_CREATED;

@Entity
public class WeeklyScore implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int numberAnswered;

    private int uniquelyAnswered;

    private int percentageCorrect;

    private LocalDate week;

    @ManyToOne
    private Dashboard dashboard;

    @ManyToOne
    private SamePercentage samePercentage;

    public WeeklyScore() {}

    public WeeklyScore(Dashboard dashboard, LocalDate week) {
        setWeek(week);
        setDashboard(dashboard);
        if(dashboard.getWeeklyScores().size() != 0){  //if there are other weekly scores
            for(WeeklyScore w : dashboard.getWeeklyScores()){
                if(!(w.getId().equals(this.getId()))){
                    if(w.getPercentageCorrect() == this.getPercentageCorrect()){ //check if there are other weekly scores with the same percentage
                        if(w.getSamePercentage()==null){
                            w.setSamePercentage(new SamePercentage(w)); //initiate samePercentage of the other weekly score (weeklyscore2) instance if it doesn't exist yet
                        }
                        w.getSamePercentage().addWeeklyScore(this); //add weeklyscore1 to weeklyscore2's weekly score hash set
                        setSamePercentage(new SamePercentage(this)); //initiate samePercentage for weeklyscore1
                        getSamePercentage().addWeeklyScore(w); //add weeklyscore2 to weeklyscore1's weekly score hash set
                    }
                }
            }
        }


    }

    public Integer getId() { return id; }

    public int getNumberAnswered() { return numberAnswered; }

    public void setNumberAnswered(int numberAnswered) {
        this.numberAnswered = numberAnswered;
    }

    public int getUniquelyAnswered() { return uniquelyAnswered; }

    public void setUniquelyAnswered(int uniquelyAnswered) {
        this.uniquelyAnswered = uniquelyAnswered;
    }

    public int getPercentageCorrect() { return percentageCorrect; }

    public void setPercentageCorrect(int percentageCorrect) {
        this.percentageCorrect = percentageCorrect;
    }

    public LocalDate getWeek() { return week; }

    public void setWeek(LocalDate week) {
        this.week = week;
    }

    public Dashboard getDashboard() { return dashboard; }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
        this.dashboard.addWeeklyScore(this);
    }

    public SamePercentage getSamePercentage() {
        return samePercentage;
    }

    public void setSamePercentage(SamePercentage samePercentage) {
        this.samePercentage = samePercentage;
    }

    public void accept(Visitor visitor) {
    }

    public void remove() {
        if(samePercentage != null){
            this.samePercentage.remove(); //remove samePercentage instance
        }
        this.dashboard.getWeeklyScores().remove(this); //remove weekly score from dashboard
        this.dashboard = null; //reset dashboard variable
    }

    @Override
    public String toString() {
        return "WeeklyScore{" +
                "id=" + getId() +
                ", numberAnswered=" + numberAnswered +
                ", uniquelyAnswered=" + uniquelyAnswered +
                ", percentageCorrect=" + percentageCorrect +
                ", week=" + getWeek() +
                "}";
    }
}
