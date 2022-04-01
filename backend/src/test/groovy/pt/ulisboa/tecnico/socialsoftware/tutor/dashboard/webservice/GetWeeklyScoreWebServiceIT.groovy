package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetWeeklyScoreWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def response

    def authUserDto
    def courseExecutionDto
    def dashboardDto

    def setup() {
        given:
        restClient = new RESTClient("http://localhost:" + port)
        and:
        courseExecutionDto = courseService.getDemoCourse()
        authUserDto = authUserService.demoStudentAuth(false).getUser()
        dashboardDto = dashboardService.getDashboard(courseExecutionDto.getCourseExecutionId(), authUserDto.getId())
        weeklyScoreService.createWeeklyScore(dashboardDto.getId())
    }

    def "demo student gets weekly scores"() {
        given: 'a demo student'
        demoStudentLogin()

        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/dashboards/weeklyScores/' + dashboardDto.getId(),
                requestContentType: 'application/json'
        )

        then: "the request returns 200 - success"
        response.status == 200
        and: "has value"
        response.data.id != null
        and: 'it is in the database'
        weeklyScoreRepository.findAll().size() == 1

        cleanup:
        weeklyScoreRepository.deleteAll()
    }

    def "demo teacher does not have access"() {
        given: 'demo teacher'
        demoTeacherLogin()

        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/dashboards/weeklyScores/' + dashboardDto.getId(),
                requestContentType: 'application/json'
        )

        then: "request returns 403 because teacher does not have access to a student dashboard"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        cleanup:
        weeklyScoreRepository.deleteAll()


    }

    def "new demo student does not have access"() {
        given: 'a different new demo student'
        demoNewStudentLogin()

        when: 'the web service is invoked'
        response = restClient.get(
                path: '/students/dashboards/weeklyScores/' + dashboardDto.getId(),
                requestContentType: 'application/json'
        )

        then: "request returns 403 because uses does not have access to another student dashboard"
        def error = thrown(HttpResponseException)
        error.response.status == HttpStatus.SC_FORBIDDEN

        cleanup:
        weeklyScoreRepository.deleteAll()
    }

}