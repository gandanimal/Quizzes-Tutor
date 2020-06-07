package pt.ulisboa.tecnico.socialsoftware.tutor.answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.CorrectAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.QuizAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlExport;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.OptionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.statement.dto.StatementQuizDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class AnswerService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private AnswersXmlImport xmlImporter;

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public QuizAnswerDto createQuizAnswer(Integer userId, Integer quizId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new TutorException(USER_NOT_FOUND, userId));

        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new TutorException(QUIZ_NOT_FOUND, quizId));

        QuizAnswer quizAnswer = new QuizAnswer(user, quiz);
        quizAnswerRepository.save(quizAnswer);

        return new QuizAnswerDto(quizAnswer);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<CorrectAnswerDto> concludeQuiz(User user, StatementQuizDto statementQuizDto) {
        QuizAnswer quizAnswer = quizAnswerRepository.findQuizAnswer(statementQuizDto.getId(), user.getId())
                .orElseThrow(() -> new TutorException(QUIZ_ANSWER_NOT_FOUND, statementQuizDto.getId()));

        if (quizAnswer.getQuiz().getAvailableDate() != null && quizAnswer.getQuiz().getAvailableDate().isAfter(DateHandler.now())) {
            throw new TutorException(QUIZ_NOT_YET_AVAILABLE);
        }

        if (quizAnswer.getQuiz().getConclusionDate() != null && quizAnswer.getQuiz().getConclusionDate().isBefore(DateHandler.now())) {
            throw new TutorException(QUIZ_NO_LONGER_AVAILABLE);
        }

        if (!quizAnswer.isCompleted()) {
            quizAnswer.setAnswerDate(DateHandler.now());
            quizAnswer.setCompleted(true);

            for (QuestionAnswer questionAnswer : quizAnswer.getQuestionAnswers()) {
                writeQuestionAnswer(questionAnswer, statementQuizDto);
            }
        }

        // In class quiz when student submits before resultsDate
        if (quizAnswer.getQuiz().getResultsDate() != null &&
            quizAnswer.getQuiz().getType().equals(Quiz.QuizType.IN_CLASS) &&
            DateHandler.now().isBefore(quizAnswer.getQuiz().getResultsDate())) {

            return new ArrayList<>();
        }

        return quizAnswer.getQuestionAnswers().stream()
                .sorted(Comparator.comparing(QuestionAnswer::getSequence))
                .map(CorrectAnswerDto::new)
                .collect(Collectors.toList());
    }

    private void writeQuestionAnswer(QuestionAnswer questionAnswer, StatementQuizDto statementQuizDto) {
//        QuestionAnswer questionAnswer = questionAnswerRepository.findById(statementAnswerDto.getQuestionAnswerId())
//                .orElseThrow(() -> new TutorException(QUESTION_ANSWER_NOT_FOUND, statementAnswerDto.getQuestionAnswerId()));

        StatementAnswerDto statementAnswerDto = statementQuizDto.getAnswers().stream()
                .filter(statementAnswerDto1 -> statementAnswerDto1.getQuestionAnswerId().equals(questionAnswer.getId()))
                .findAny()
                .orElseThrow(() -> new TutorException(QUESTION_ANSWER_NOT_FOUND, questionAnswer.getId()));

        if (statementAnswerDto.getOptionId() != null) {

            // TO DO: Assess performance
                Option option = questionAnswer.getQuizQuestion().getQuestion().getOptions().stream()
                        .filter(option1 -> option1.getId().equals(statementAnswerDto.getOptionId()))
                        .findAny()
                        .orElseThrow(() -> new TutorException(QUESTION_OPTION_MISMATCH, statementAnswerDto.getOptionId()));

//            Option option = optionRepository.findById(statementAnswerDto.getOptionId())
//                    .orElseThrow(() -> new TutorException(OPTION_NOT_FOUND, statementAnswerDto.getOptionId()));
//            if (isNotQuestionOption(questionAnswer.getQuizQuestion(), option)) {
//                throw new TutorException(QUESTION_OPTION_MISMATCH, questionAnswer.getQuizQuestion().getQuestion().getId(), option.getId());
//            }

            if (questionAnswer.getOption() != null) {
                questionAnswer.getOption().getQuestionAnswers().remove(questionAnswer);
            }

            questionAnswer.setOption(option);
            questionAnswer.setTimeTaken(statementAnswerDto.getTimeTaken());
        } else {
            questionAnswer.setOption(null);
            questionAnswer.setTimeTaken(statementAnswerDto.getTimeTaken());
        }
    }


    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void submitAnswer(User user, StatementAnswerDto answer) {
        QuestionAnswer questionAnswer = questionAnswerRepository.findById(answer.getQuestionAnswerId())
                .orElseThrow(() -> new TutorException(QUESTION_ANSWER_NOT_FOUND, answer.getQuestionAnswerId()));

        QuizAnswer quizAnswer = questionAnswer.getQuizAnswer();

        if (isNotAssignedStudent(user, quizAnswer)) {
            throw new TutorException(QUIZ_USER_MISMATCH, String.valueOf(quizAnswer.getQuiz().getId()), user.getUsername());
        }

        if (quizAnswer.getQuiz().getConclusionDate() != null && quizAnswer.getQuiz().getConclusionDate().isBefore(DateHandler.now())) {
            throw new TutorException(QUIZ_NO_LONGER_AVAILABLE);
        }

        if (quizAnswer.getQuiz().getAvailableDate() != null && quizAnswer.getQuiz().getAvailableDate().isAfter(DateHandler.now())) {
            throw new TutorException(QUIZ_NOT_YET_AVAILABLE);
        }

        if (!quizAnswer.isCompleted()) {
            if (answer.getOptionId() != null) {

                // TO DO: Assess performance
//                Option option = questionAnswer.getQuizQuestion().getQuestion().getOptions().stream()
//                        .filter(option1 -> option1.getId().equals(answer.getOptionId()))
//                        .findAny()
//                        .orElseThrow(() -> new TutorException(OPTION_NOT_FOUND, answer.getOptionId()));

                Option option = optionRepository.findById(answer.getOptionId())
                        .orElseThrow(() -> new TutorException(OPTION_NOT_FOUND, answer.getOptionId()));

                if (isNotQuestionOption(questionAnswer.getQuizQuestion(), option)) {
                    throw new TutorException(QUESTION_OPTION_MISMATCH, questionAnswer.getQuizQuestion().getQuestion().getId(), option.getId());
                }

                if (questionAnswer.getOption() != null) {
                    questionAnswer.getOption().getQuestionAnswers().remove(questionAnswer);
                }

                questionAnswer.setOption(option);
                questionAnswer.setTimeTaken(answer.getTimeTaken());
                quizAnswer.setAnswerDate(DateHandler.now());
            } else {
                questionAnswer.setOption(null);
                questionAnswer.setTimeTaken(answer.getTimeTaken());
                quizAnswer.setAnswerDate(DateHandler.now());
            }
        }
    }

    private boolean isNotQuestionOption(QuizQuestion quizQuestion, Option option) {
        return !option.getQuestion().getId().equals(quizQuestion.getQuestion().getId());
    }

    private boolean isNotAssignedStudent(User user, QuizAnswer quizAnswer) {
        return !user.getId().equals(quizAnswer.getUser().getId());
    }

    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String exportAnswers() {
        AnswersXmlExport xmlExport = new AnswersXmlExport();

        return xmlExport.export(quizAnswerRepository.findAll());
    }


    @Retryable(
      value = { SQLException.class },
      backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void importAnswers(String answersXml) {
        xmlImporter.importAnswers(answersXml);
    }

    @Retryable(
            value = { SQLException.class },
            backoff = @Backoff(delay = 5000))
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteQuizAnswer(QuizAnswer quizAnswer) {
        List<QuestionAnswer> questionAnswers = new ArrayList<>(quizAnswer.getQuestionAnswers());
        questionAnswers.forEach(questionAnswer ->
        {
            questionAnswer.remove();
            questionAnswerRepository.delete(questionAnswer);
        });
        quizAnswer.remove();
        quizAnswerRepository.delete(quizAnswer);
    }
}
