package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.Student
import spock.lang.Unroll

@DataJpaTest
class CreateFailedAnswerTest extends SpockTest {
    def student

    def setup() {

    }

    def "create a failed answer"() {

    }

    def "cannot create failed answer if course execution is different in dashboard and question answer"() {

    }

    def "cannot create failed answer if student is different in dashboard and question answer"() {

    }

    def "cannot create failed answer if answer is correct"() {

    }

    def "invalid argument"() {

    }

    def "create 2 failed answers for the same question, both have each other in same question parameter"() {

    }

}
