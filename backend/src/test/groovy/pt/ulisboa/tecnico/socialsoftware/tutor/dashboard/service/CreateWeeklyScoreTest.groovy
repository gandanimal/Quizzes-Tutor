package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.WeeklyScore
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import spock.lang.Unroll

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DASHBOARD_NOT_FOUND

@DataJpaTest
class CreateWeeklyScoreTest extends SpockTest {
    def student
    def dashboard;

    def setup(){
        createExternalCourseAndExecution() //create course

        student = new Student(USER_1_USERNAME, false) //create student
        student.addCourse(externalCourseExecution) //add student to course
        userRepository.save(student) //save student in user repository
        dashboard = new Dashboard(externalCourseExecution, student) // create new dashboard
        dashboardRepository.save(dashboard) //save dashboard in repository
    }

    def "Create WeeklyScore"(){
        when:
        weeklyScoreService.createWeeklyScore(dashboard.getId()) //when a weekly score is created

        then:
        weeklyScoreRepository.count() == 1L //check if number of Weekly Scores in repository increased
        def result = weeklyScoreRepository.findAll().get(0) // get the results
        result.getId() != null //check if result has been assigned an ID
        result.getDashboard().getId() == dashboard.getId() //check if result dashboard is the same as current dashboard
        result.getNumberAnswered() == 0 //check if the number of answered questions/uniquely answered/percentage of correct answers is set to default 0
        result.getUniquelyAnswered() == 0
        result.getPercentageCorrect() == 0
        and: // and check if current dashboard contains the same result as the weekly Score Repository
        def dashboard = dashboardRepository.getById(dashboard.getId())
        dashboard.getWeeklyScores().contains(result)
    }


    def "Create two WeeklyScores that have the Same Percentage of correct answers"(){
        given:
        def weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY) //define date as sunday
        def week = DateHandler.now().minusDays(30).with(weekSunday).toLocalDate() //define week as 30 days ago
        def weeklyScore3 = new WeeklyScore(dashboard,week)//create another weeklyScore with same percentagecorrect=0 on a different week
        weeklyScoreRepository.save(weeklyScore3) //save in repository



        when:
        weeklyScoreService.createWeeklyScore(dashboard.getId()) //create weeklyScore

        then:
        weeklyScoreRepository.count() == 2L
        def weeklyScore2 = weeklyScoreRepository.findAll().get(1)
        def weeklyScore1 = weeklyScoreRepository.findAll().get(0)
        weeklyScore1.getSamePercentage().getId() != null //check if samePercentage were created
        weeklyScore2.getSamePercentage().getId() != null
        weeklyScore1.getPercentageCorrect() == weeklyScore2.getPercentageCorrect() //check if percentage is the same in both
        samePercentageRepository.count() == 2L    //check if they were added to repository
        weeklyScore1.getSamePercentage().getOriginWeeklyScore() == weeklyScore1 //check if weeklyScore in samepercentage is correctly assigned
        weeklyScore2.getSamePercentage().getOriginWeeklyScore() == weeklyScore2
        weeklyScore1.getSamePercentage().getWeeklyScores().getAt(0) == weeklyScore2 //check if weeklyScore list in samepercentage points to the other weeklyScore
        weeklyScore2.getSamePercentage().getWeeklyScores().getAt(0) == weeklyScore1
        samePercentageRepository.findAll().get(1) == weeklyScore1.getSamePercentage() //check if repository has correctly stored the weeklyScores
        samePercentageRepository.findAll().get(0) == weeklyScore2.getSamePercentage()
    }

    def "Cannot create multiple WeeklyScore for the same week"(){

        given: //already existing Weekly Score
        weeklyScoreService.createWeeklyScore(dashboard.getId())

        when: //another weekly scores is created in the same week
        weeklyScoreService.createWeeklyScore(dashboard.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.WEEKLY_SCORE_ALREADY_CREATED //check if error is correctly thrown
        weeklyScoreRepository.count() == 1L //check if the number of Weekly scores hasn't changed
    }
    @Unroll
    def "Cannot create WeeklyScore with invalid dashboard=#dashboardId"(){
        when: //when weekly score is created with a certain invalid dashboardID
        weeklyScoreService.createWeeklyScore(dashboardId)

        then:
        def exception = thrown(TutorException) //define exception that is supposed to be thrown
        exception.getErrorMessage() == errorMessage //check if exception thrown is the correct exception
        weeklyScoreRepository.count() == 0L //check if the weekly score repository hasn't inscreased

        where: //define values to be tested: null and 100 and respective error message that should be thrown
        dashboardId || errorMessage
        null        || DASHBOARD_NOT_FOUND
        100         || DASHBOARD_NOT_FOUND
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}