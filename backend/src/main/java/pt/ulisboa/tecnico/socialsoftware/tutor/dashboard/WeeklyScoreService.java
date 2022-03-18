package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.Dashboard;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.WeeklyScore;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.WeeklyScoreDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.DashboardRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.WeeklyScoreRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository.SamePercentageRepository;

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class WeeklyScoreService {

  @Autowired
  private WeeklyScoreRepository weeklyScoreRepository;

  @Autowired
  private DashboardRepository dashboardRepository;

  @Autowired
  private SamePercentageRepository samePercentageRepository;

  @Transactional(isolation = Isolation.READ_COMMITTED) //avoids dirty reads

  public WeeklyScoreDto createWeeklyScore(Integer dashboardId) {
    if (dashboardId == null) { //if dashboard id is not assigned throw an error
      throw new TutorException(DASHBOARD_NOT_FOUND);
    }

    Dashboard dashboard = dashboardRepository.findById(dashboardId)  //get dashboard from the repository
            .orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId)); //throw error if id not found in repository

    TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY); //define date as last Sunday to create a current week instance
    LocalDate week = DateHandler.now().with(weekSunday).toLocalDate(); //convert to local date

    WeeklyScore weeklyScore = new WeeklyScore(dashboard, week); //create new Weekly score
    weeklyScoreRepository.save(weeklyScore); //save in repository
    weeklyScore.checkSamePercentage();
    if(weeklyScore.getSamePercentage()!= null)
      samePercentageRepository.save(weeklyScore.getSamePercentage());


    return new WeeklyScoreDto(weeklyScore);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED) //avoids dirty reads
  public void removeWeeklyScore(Integer weeklyScoreId){
    if (weeklyScoreId == null) { //if weekly score id is not assigned throw an error
      throw new TutorException(WEEKLY_SCORE_NOT_FOUND);
    }
    WeeklyScore weekScore = weeklyScoreRepository.findById(weeklyScoreId)  //get weekly score from the repository
            .orElseThrow(()-> new TutorException(WEEKLY_SCORE_NOT_FOUND, weeklyScoreId)); //throw error if weekly score ID was not found in repository

    TemporalAdjuster weekSun = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY); //define date as last Sunday to create a current week instance
    LocalDate currentWeek = DateHandler.now().with(weekSun).toLocalDate(); //convert to Local Date

    if (weekScore.getWeek().isEqual(currentWeek)) { //if weekly score being removed was created in the current week it cannot be removed
      throw new TutorException(CANNOT_REMOVE_WEEKLY_SCORE);
    }
    if(weekScore.getSamePercentage() != null)
      samePercentageRepository.delete(weekScore.getSamePercentage());
    weekScore.remove(); //delete weekly score from dashboard
    weeklyScoreRepository.delete(weekScore); //delete weekly score from repository
  }

}
