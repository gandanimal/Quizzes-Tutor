package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import spock.lang.Unroll

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DASHBOARD_NOT_FOUND

@DataJpaTest
class CreateWeeklyScoreTest extends SpockTest {
    def student
    def dashboard;

    def setup(){
        createExternalCourseAndExecution() //create course

        student = new Student(USERNAME1, false) //create student
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
        def weeklyscore1 = weeklyScoreService.createWeeklyScore(dashboard.getId()) //create weeklyscore, initiates percentagecorrect to 0

        when:
        def weeklyscore2 = weeklyScoreService.createWeeklyScore(dashboard.getId()) //create another weeklyscore with same percentagecorrect=0

        then:
        weeklyscore1.getSamePercentage().getId() != null //check if samePercentage were created
        weeklyscore2.getSamePercentage().getId() != null
        weeklyscore1.getPercentageCorrect() == weeklyscore2.getPercentageCorrect() //check if percentage is the same in both
        samePercentageRepository.count() == 2L    //check if they were added to repository
        weeklyscore1.getSamePercentage().getOriginWeeklyScore == weeklyscore1 //check if weeklyscore in samepercentage is correctly assigned
        weeklyscore2.getSamePercentage().getOriginWeeklyScore == weeklyscore2
        weeklyscore1.getSamePercentage().getWeeklyScores(0) == weeklyscore2 //check if weeklyscore list in samepercentage points to the other weeklyscore
        weeklyscore2.getSamePercentage().getWeeklyScores(0) == weeklyscore1
        samePercentageRepository.findAll().get(0) == weeklyscore1 //check if repository has correctly stored the weeklyscores
        samePercentageRepository.findAll().get(1) == weeklyscore2
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