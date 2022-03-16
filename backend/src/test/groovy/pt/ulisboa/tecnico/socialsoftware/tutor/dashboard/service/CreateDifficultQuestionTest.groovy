package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import spock.lang.Unroll
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler


import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.CANNOT_CREATE_DIFFICULT_QUESTION
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DASHBOARD_NOT_FOUND
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_NOT_FOUND

@DataJpaTest
class CreateDifficultQuestionTest extends SpockTest {

    def setup() {
    }

    def "create difficult question with percentage #percentage"() {
    }

    def "cannot create two difficult questions for the same question"() {
    }

    def "cannot create a difficult question that does not belong to the student course"() {
    }

    def "cannot create difficult question with invalid percentage"() {
    }

    def "cannot create difficult question with invalid dashboard id"() {
    }

    def "cannot create difficult question with invalid question id"() {
    }
}