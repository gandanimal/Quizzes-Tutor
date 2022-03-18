package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.SamePercentage
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

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.WEEKLY_SCORE_NOT_FOUND

@DataJpaTest
class RemoveWeeklyScoreTest extends SpockTest {
    def student
    def dashboard

    def setup() {
        createExternalCourseAndExecution()

        student = new Student(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, false, AuthUser.Type.EXTERNAL)
        student.addCourse(externalCourseExecution)
        userRepository.save(student)

        dashboard = new Dashboard(externalCourseExecution, student)
        dashboardRepository.save(dashboard)
    }

    def "Remove previous Weekly Score"() {
        given:
        TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
        LocalDate week = DateHandler.now().minusDays(30).with(weekSunday).toLocalDate();
        WeeklyScore weeklyScore = new WeeklyScore(dashboard, week)
        weeklyScoreRepository.save(weeklyScore)

        when:
        weeklyScoreService.removeWeeklyScore(weeklyScore.getId())

        then:
        weeklyScoreRepository.count() == 0L
        and:
        def dashboard = dashboardRepository.getById(dashboard.getId())
        dashboard.getWeeklyScores().size() == 0
    }

    def "Cannot remove current Weekly Score"() {
        given:
        TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
        LocalDate week = DateHandler.now().with(weekSunday).toLocalDate();
        WeeklyScore weeklyScore = new WeeklyScore(dashboard, week)
        weeklyScoreRepository.save(weeklyScore)

        when:
        weeklyScoreService.removeWeeklyScore(weeklyScore.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_REMOVE_WEEKLY_SCORE
        and:
        weeklyScoreRepository.count() == 1
    }


    def "Remove Weekly Score with same percentage (0%)"() {
        given:
        TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
        LocalDate week = DateHandler.now().minusDays(30).with(weekSunday).toLocalDate();
        LocalDate week2 = DateHandler.now().with(weekSunday).toLocalDate();
        WeeklyScore weeklyScore1 = new WeeklyScore(dashboard, week)
        weeklyScoreRepository.save(weeklyScore1)
        WeeklyScore weeklyScore2 = new WeeklyScore(dashboard, week2)
        weeklyScoreRepository.save(weeklyScore2)
        SamePercentage samePercentage1 = new SamePercentage(weeklyScore1)
        SamePercentage samePercentage2 = new SamePercentage(weeklyScore2)
        samePercentage1.addWeeklyScore(weeklyScore2)
        samePercentage2.addWeeklyScore(weeklyScore1)
        samePercentageRepository.save(samePercentage1)
        samePercentageRepository.save(samePercentage2)


        when:
        weeklyScoreService.removeWeeklyScore(weeklyScore2.getId())

        then:
        weeklyScoreRepository.count() == 1
        samePercentageRepository.count() == 1
        and:
        def weeklyScore = weeklyScoreRepository.getById(weeklyScore1.getId())
        weeklyScore.getSamePercentage().getWeeklyScores().size() == 0
        def samePercentage = samePercentageRepository.getById(weeklyScore.getSamePercentage.getId())
        samePercentage.getWeeklyScores().size() == 0
    }


    @Unroll
    def "Cannot remove Weekly Score with invalid weeklyScore=#weeklyScoreId"() {
        when:
        weeklyScoreService.removeWeeklyScore(weeklyScoreId)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        weeklyScoreId || errorMessage
        null          || WEEKLY_SCORE_NOT_FOUND
        100           || WEEKLY_SCORE_NOT_FOUND
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}