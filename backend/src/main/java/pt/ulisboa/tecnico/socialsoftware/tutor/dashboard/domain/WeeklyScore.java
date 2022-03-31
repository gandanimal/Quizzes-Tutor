package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class WeeklyScore implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int numberAnswered;

    private int uniquelyAnswered;

    private int percentageCorrect;

    private LocalDate week;

    private boolean closed;

    @ManyToOne
    private Dashboard dashboard;

    @OneToOne(cascade = CascadeType.ALL)
    private SamePercentage samePercentage;

    public WeeklyScore() {}

    public WeeklyScore(Dashboard dashboard, LocalDate week) {
        setWeek(week);
        setSamePercentage(new SamePercentage(this));
        setDashboard(dashboard);
        setClosed(false);

        dashboard.getWeeklyScores().stream().forEach(weeklyScore -> {
            if (weeklyScore.getPercentageCorrect() == this.getPercentageCorrect() && weeklyScore != this) {
                samePercentage.getWeeklyScores().add(weeklyScore);
                weeklyScore.getSamePercentage().getWeeklyScores().add(this);
            }
        });
    }

    public void remove() {
        this.dashboard.getWeeklyScores().remove(this);

        dashboard.getWeeklyScores().stream().filter(weeklyScore -> weeklyScore.getPercentageCorrect() == percentageCorrect && weeklyScore != this).map(WeeklyScore::getSamePercentage)
                .forEach(samePercentage1 -> samePercentage1.getWeeklyScores().remove(this));
        samePercentage.remove();

        this.dashboard = null;
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

    public SamePercentage getSamePercentage() {
        return samePercentage;
    }

    public void setSamePercentage(SamePercentage samePercentage) {
        this.samePercentage = samePercentage;
    }


    public void computeStatistics() {
        Set<QuizAnswer> weeklyQuizAnswers = getWeeklyQuizAnswers();

        Set<QuestionAnswer> publicWeeklyQuestionAnswers = weeklyQuizAnswers.stream()
                .filter(quizAnswer -> quizAnswer.canResultsBePublic(dashboard.getCourseExecution().getId()))
                .flatMap(quizAnswer -> quizAnswer.getQuestionAnswers().stream())
                .collect(Collectors.toSet());

        Set<Question> weeklyQuestionsAnswered = publicWeeklyQuestionAnswers.stream()
                .map(QuestionAnswer::getQuizQuestion)
                .map(QuizQuestion::getQuestion).collect(Collectors.toSet());

        setNumberAnswered((int) weeklyQuestionsAnswered.stream()
                .map(Question::getId).count());
        setUniquelyAnswered((int) weeklyQuestionsAnswered.stream()
                .map(Question::getId).distinct().count());
        setPercentageCorrect(publicWeeklyQuestionAnswers.size() > 0 ? (int) Math.round((publicWeeklyQuestionAnswers.stream().filter(QuestionAnswer::isCorrect).count() /
                (double) publicWeeklyQuestionAnswers.size()) * 100.0) : 0);

        if (DateHandler.now().isAfter(week.plusDays(7).atStartOfDay())) {
            closed = weeklyQuizAnswers.stream()
                    .noneMatch(quizAnswer -> !quizAnswer.canResultsBePublic(dashboard.getCourseExecution().getId()));
        }
    }

    private Set<QuizAnswer> getWeeklyQuizAnswers() {
        return dashboard.getStudent().getQuizAnswers().stream()
                .filter(quizAnswer -> quizAnswer.getQuiz().getCourseExecution() == dashboard.getCourseExecution())
                .filter(this::isAnswerWithinWeek)
                .collect(Collectors.toSet());
    }

    private boolean isAnswerWithinWeek(QuizAnswer quizAnswer) {
        TemporalAdjuster weekSaturday = TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY);

        LocalDate answerDate = quizAnswer.getAnswerDate().toLocalDate();
        return (answerDate.isEqual(this.week) || answerDate.isEqual(this.week.with(weekSaturday)) ||
                (answerDate.isAfter(this.week) && answerDate.isBefore(this.week.with(weekSaturday))));
    }

    public void setClosed(boolean close) {
        TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
        LocalDate currentWeek = DateHandler.now().with(weekSunday).toLocalDate();
        if (close && week.isEqual(currentWeek)) {
            throw new TutorException(CANNOT_CLOSE_CURRENT_WEEK, DateHandler.toISOString(week.atStartOfDay()));
        }

        this.closed = close;
    }

    public Dashboard getDashboard() { return dashboard; }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
        this.dashboard.addWeeklyScore(this);
    }

    public void accept(Visitor visitor) {
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
