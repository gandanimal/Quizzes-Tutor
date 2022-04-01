package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort

import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service.FailedAnswersSpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.FailedAnswerRepository
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student

import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetFailedAnswersWebServiceIT extends FailedAnswersSpockTest {
    @LocalServerPort
    private int port

    def response
    def quiz
    def quizQuestion

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
        quiz = createQuiz(1)
        quizQuestion = createQuestion(1, quiz)
    }

    def "student gets failed answers"() {
        createdUserLogin(USER_1_USERNAME, USER_1_PASSWORD)

        when:
        response = restClient.get( path:'/students/dashboards/failedAnswer/' + dashboard.getId(), requestContentType: 'application/json')

        then:
        response.status == 200
        and:
        FailedAnswerRepository.findAll().size() == 1

        cleanup:
        failedAnswerRepository.deleteAll();
        dashboardRepository.deleteAll();
        userRepository.deleteAll();
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "teacher can't get student's failed answers"() {
        given:
        demoTeacherLogin()

        when:
        response = restClient.get( path:'/students/dashboards/failedAnswer/' + dashboard.getId(), requestContentType: 'application/json')

        then:
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        cleanup:
        failedAnswerRepository.deleteAll();
        dashboardRepository.deleteAll();
        userRepository.deleteAll();
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "student can't get another student's failed answers"() {
        given:
        demoStudentLogin()

        when:
        response = restClient.get( path:'/students/dashboards/failedAnswer/' + dashboard.getId(), requestContentType: 'application/json')

        then:
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        cleanup:
        failedAnswerRepository.deleteAll();
        dashboardRepository.deleteAll();
        userRepository.deleteAll();
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

}
