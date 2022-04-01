package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuizAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.DifficultQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.FailedAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DifficultQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.FailedAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DashboardRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.FailedAnswerRepository;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class FailedAnswerService {

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private FailedAnswerRepository failedAnswerRepository;

    @Autowired
    private QuizAnswerRepository quizAnswerRepository;

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public FailedAnswerDto createFailedAnswer(int dashboardId, int questionAnswerId) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));
        QuestionAnswer questionAnswer = questionAnswerRepository.findById(questionAnswerId).orElseThrow(() -> new TutorException(QUESTION_ANSWER_NOT_FOUND, questionAnswerId));

        FailedAnswer failedAnswer = new FailedAnswer(dashboard, questionAnswer, DateHandler.now());
        failedAnswerRepository.save(failedAnswer);

        return new FailedAnswerDto(failedAnswer);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeFailedAnswer(int failedAnswerId) {
        FailedAnswer toRemove = failedAnswerRepository.findById(failedAnswerId).orElseThrow(() -> new TutorException(ErrorMessage.FAILED_ANSWER_NOT_FOUND, failedAnswerId));
        toRemove.remove();
        failedAnswerRepository.delete(toRemove);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<FailedAnswerDto> getFailedAnswers(int dashboardId) {
        List<FailedAnswerDto> failedAnswerDtos = new ArrayList<>();
        List<FailedAnswer> failedAnswers = new ArrayList<>();
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));

        for (FailedAnswer fa : dashboard.getFailedAnswers()) {
            failedAnswers.add(fa);
        }

        for (FailedAnswer a : failedAnswers) {
          failedAnswerDtos.add(new FailedAnswerDto(a));
        }
        return failedAnswerDtos;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateFailedAnswers(int dashboardId, String start, String end) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));
        FailedAnswer failedAnswer;
        Quiz quiz;
        LocalDateTime resultsDate;
        LocalDateTime creationDate;
        LocalDateTime availableDate;
        LocalDateTime conclusionDate;
        LocalDateTime lastChecked = dashboard.getLastCheckFailedAnswers();

        for (QuestionAnswer questionAnswer: questionAnswerRepository.findAll()){
            quiz = questionAnswer.getQuizAnswer().getQuiz();
            resultsDate = quiz.getResultsDate();
            creationDate = questionAnswer.getQuizAnswer().getCreationDate();
            availableDate = quiz.getAvailableDate();
            conclusionDate = quiz.getConclusionDate();

            if (!questionAnswer.isCorrect() && questionAnswer.getQuizAnswer().isCompleted()){
                if (start == null  &&  end == null){
                    if (quiz.getType() == Quiz.QuizType.IN_CLASS){
                        if(DateHandler.now().isAfter(resultsDate)){
                            failedAnswer = new FailedAnswer(dashboard, questionAnswer, DateHandler.now());
                            failedAnswerRepository.save(failedAnswer);
                            dashboard.setLastCheckFailedAnswers(creationDate.minusSeconds(1));
                        }
                        else {
                            dashboard.setLastCheckFailedAnswers(creationDate.minusSeconds(1));
                            continue;
                        }
                    }
                    else if (creationDate.isBefore(lastChecked)){
                        dashboard.setLastCheckFailedAnswers(creationDate.minusSeconds(1));
                        continue;
                    }
                    else if (creationDate.isBefore(LocalDateTime.parse(start, DateTimeFormatter.ISO_DATE_TIME))){
                        continue;
                    }
                    else {
                        try {
                            failedAnswer = new FailedAnswer(dashboard, questionAnswer, DateHandler.now());
                        }catch (TutorException e){
                            continue;
                        }
                        failedAnswerRepository.save(failedAnswer);
                        if(lastChecked.isBefore(creationDate.minusSeconds(1))){
                            dashboard.setLastCheckFailedAnswers(creationDate.minusSeconds(1));
                        }
                    }

                }
                else {
                    try {
                        failedAnswer = new FailedAnswer(dashboard, questionAnswer, DateHandler.now());
                    }catch (TutorException e){
                        continue;
                    }
                    failedAnswerRepository.save(failedAnswer);
                    if(lastChecked.isBefore(creationDate.minusSeconds(1))){
                        dashboard.setLastCheckFailedAnswers(creationDate.minusSeconds(1));
                    }
                }
            }
        }
    }
}
