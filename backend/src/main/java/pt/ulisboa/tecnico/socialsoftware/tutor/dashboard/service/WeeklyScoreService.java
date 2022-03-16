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

}
