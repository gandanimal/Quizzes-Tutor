package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.DifficultQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DifficultQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DashboardRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DifficultQuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class DifficultQuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private DifficultQuestionRepository difficultQuestionRepository;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public DifficultQuestionDto createDifficultQuestion(int dashboardId, int questionId, int percentage) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, questionId));

        DifficultQuestion difficultQuestion = new DifficultQuestion(dashboard, question, percentage);
        difficultQuestionRepository.save(difficultQuestion);

        return new DifficultQuestionDto(difficultQuestion);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void removeDifficultQuestion(int difficultQuestionId) {
        DifficultQuestion difficultQuestion = difficultQuestionRepository.findById(difficultQuestionId).orElseThrow(() -> new TutorException(DIFFICULT_QUESTION_NOT_FOUND, difficultQuestionId));

        if(difficultQuestion.isRemoved()){
            throw new TutorException(ErrorMessage.CANNOT_REMOVE_DIFFICULT_QUESTION);
        }else{
            difficultQuestion.setRemoved(true);
            difficultQuestion.setRemovedDate(DateHandler.now());
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<DifficultQuestionDto> getDifficultQuestions(int dashboardId) {
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));
        List < DifficultQuestionDto > dq = new ArrayList< DifficultQuestionDto >();
        List<DifficultQuestion> difficultQuestionList = new ArrayList<>(dashboard.getDifficultQuestions());
        for (DifficultQuestion d : difficultQuestionList){
            if(!d.isRemoved()){
                dq.add(new DifficultQuestionDto(d));
            }
        }
        return dq;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateDifficultQuestions(int dashboardId){
        Dashboard dashboard = dashboardRepository.findById(dashboardId).orElseThrow(() -> new TutorException(ErrorMessage.DASHBOARD_NOT_FOUND, dashboardId));

        for (Question q: questionRepository.findQuestions(dashboard.getCourseExecution().getId())) {
            int numberOfCorrect=0;
            int numberOfAnswers=0;

            for(QuizQuestion QQ: q.getQuizQuestions()){
                for(QuestionAnswer QA: QQ.getQuestionAnswers()) {
                    if (QA.getQuizAnswer().getAnswerDate().isAfter(DateHandler.now().minusDays(7))){
                        numberOfAnswers++;
                        if (QA.isCorrect()) {
                            numberOfCorrect++;
                        }
                    }
                }
            }
            q.setNumberOfAnswers(numberOfAnswers);
            q.setNumberOfCorrect(numberOfCorrect);
        }

        for (DifficultQuestion dq: difficultQuestionRepository.findAll()) {
            if(dq.getQuestion().getDifficulty()!=null){
                dq.update();
            }
            if(!dq.isRemoved()){
                removeDifficultQuestion(dq.getId());
            }
            dq.remove();
        }

        for (Question q: questionRepository.findQuestions(dashboard.getCourseExecution().getId())) {
            int id = q.getId();
            int percentage=100;
            boolean dqisCreated = false;

            if (q.getDifficulty()!=null){
                percentage=q.getDifficulty();
            }

            //List<DifficultQuestion> list = difficultQuestionRepository.findAll();
            //for (DifficultQuestion dq: list){
            //    if (dq.getQuestion().getId() == id) {
            //        dqisCreated = true;
            //        break;
            //    }
            //}

            if (!(dqisCreated)){
                if (percentage<=24) {
                    DifficultQuestion difficultQuestion = new DifficultQuestion(dashboard, q, percentage);
                    difficultQuestionRepository.save(difficultQuestion);
                }
            }
        }

        LocalDateTime now = DateHandler.now();
        dashboard.setLastCheckDifficultQuestions(now);
    }
}