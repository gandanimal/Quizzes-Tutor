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
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Service
public class WeeklyScoreService {

  @Autowired
  private WeeklyScoreRepository weeklyScoreRepository;

  @Autowired
  private DashboardRepository dashboardRepository;


  @Transactional(isolation = Isolation.READ_COMMITTED)
  public WeeklyScoreDto createWeeklyScore(Integer dashboardId) {
    if (dashboardId == null) {
      throw new TutorException(DASHBOARD_NOT_FOUND);
    }

    Dashboard dashboard = dashboardRepository.findById(dashboardId)
            .orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));

    TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
    LocalDate week = DateHandler.now().with(weekSunday).toLocalDate();

    WeeklyScore weeklyScore = new WeeklyScore(dashboard, week);
    weeklyScoreRepository.save(weeklyScore);
    return new WeeklyScoreDto(weeklyScore);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void removeWeeklyScore(Integer weeklyScoreId) {
    if (weeklyScoreId == null) {
      throw new TutorException(WEEKLY_SCORE_NOT_FOUND);
    }

    WeeklyScore weeklyScore = weeklyScoreRepository.findById(weeklyScoreId)
            .orElseThrow(() -> new TutorException(WEEKLY_SCORE_NOT_FOUND, weeklyScoreId));

    TemporalAdjuster weekSunday = TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY);
    LocalDate currentWeek = DateHandler.now().with(weekSunday).toLocalDate();

    if (weeklyScore.getWeek().isEqual(currentWeek)) {
      throw new TutorException(CANNOT_REMOVE_WEEKLY_SCORE);
    }

    weeklyScore.remove();
    weeklyScoreRepository.delete(weeklyScore);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public List<WeeklyScoreDto> getWeeklyScores(Integer dashboardId) {
    List<WeeklyScoreDto> weeklyScoreDtos = new ArrayList<>();
    if (dashboardId == null) {
      throw new TutorException(DASHBOARD_NOT_FOUND);
    }

    Dashboard dashboard = dashboardRepository.findById(dashboardId)
            .orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));

    List<WeeklyScore> weeklyScoreList = new ArrayList<>(dashboard.getWeeklyScores());

    weeklyScoreList.sort(Comparator.comparing(WeeklyScore::getWeek).reversed());

    for(WeeklyScore w : weeklyScoreList)
      weeklyScoreDtos.add(new WeeklyScoreDto(w));

    return weeklyScoreDtos;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public void updateWeeklyScore(Integer dashboardId){
    if (dashboardId == null) {
      throw new TutorException(DASHBOARD_NOT_FOUND);
    }
    if (weeklyScoreRepository.count()==0L){
      createWeeklyScore(dashboardId);
    }
    Dashboard dashboard = dashboardRepository.findById(dashboardId)
            .orElseThrow(() -> new TutorException(DASHBOARD_NOT_FOUND, dashboardId));
    List<WeeklyScore> weeklyScoreList = new ArrayList<>(dashboard.getWeeklyScores());
    for(WeeklyScore weeklyScore : weeklyScoreList){
      weeklyScore.computeStatistics();
    }


    LocalDateTime now = DateHandler.now();
    dashboard.setLastCheckWeeklyScores(now);

  }


}
