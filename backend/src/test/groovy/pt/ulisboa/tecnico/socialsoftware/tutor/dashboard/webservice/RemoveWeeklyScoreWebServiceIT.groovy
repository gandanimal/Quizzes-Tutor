package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.WeeklyScore
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoveWeeklyScoreWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def response
    def courseExecution
    def weeklyScore
    def dashboard
    def student

    def setup() {
        given:
        restClient = new RESTClient("http://localhost:" + port)
        and:
        createExternalCourseAndExecution()
        and:
        student = new Student(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, false, AuthUser.Type.EXTERNAL)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(externalCourseExecution)
        userRepository.save(student)
        and:
        dashboard = new Dashboard(externalCourseExecution, student)
        dashboardRepository.save(dashboard)
        and:
        TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
        LocalDate week = DateHandler.now().minusDays(30).with(weekSunday).toLocalDate();
        weeklyScore = new WeeklyScore(dashboard, week)
        weeklyScoreRepository.save(weeklyScore)
    }


    def "student removes weekly score"() {
        given: 'a demo student'
        createdUserLogin(USER_1_USERNAME, USER_1_PASSWORD)

        when: 'web service is invoked'
        response = restClient.delete(
                path: '/students/weeklyScores/' + weeklyScore.getId(),
                requestContentType: 'application/json'
        )

        then: "check response status"
        response.status == 200
        and: "the weeklyScore was removed from the database"
        weeklyScoreRepository.findById(weeklyScore.getId()).isEmpty()

        cleanup:
        weeklyScoreRepository.deleteAll()
        dashboardRepository.deleteAll()
        userRepository.deleteAll()
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
    }


    def "teacher can't remove student's weekly score from dashboard"() {
        given: 'a teacher student'
        demoTeacherLogin()

        when: 'web service is invoked'
        response = restClient.delete(
                path: '/students/weeklyScores/' + weeklyScore.getId(),
                requestContentType: 'application/json'
        )

        then: "request replies 403 - teacher does not have access"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: "weeklyScore is not deleted from database"
        weeklyScoreRepository.findAll().get(0).getId() == weeklyScore.getId()

        cleanup:
        weeklyScoreRepository.deleteAll()
        dashboardRepository.deleteAll()
        userRepository.deleteAll()
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
    }

    def "student can't remove another student's weekly score from dashboard"() {
        given: 'a teacher student'
        demoNewStudentLogin()

        when: 'web service is invoked'
        response = restClient.delete(
                path: '/students/weeklyScores/' + weeklyScore.getId(),
                requestContentType: 'application/json'
        )

        then: "request replies 403 - new student does not have access"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN
        and: "weeklyScore is not deleted from database"
        weeklyScoreRepository.findAll().get(0).getId() == weeklyScore.getId()

        cleanup:
        weeklyScoreRepository.deleteAll()
        dashboardRepository.deleteAll()
        userRepository.deleteAll()
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
    }

}