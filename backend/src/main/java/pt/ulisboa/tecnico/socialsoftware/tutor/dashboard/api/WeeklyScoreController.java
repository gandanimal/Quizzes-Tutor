package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.WeeklyScoreService;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.WeeklyScoreDto;

import java.security.Principal;
import java.util.List;

@RestController
public class WeeklyScoreController {

    @Autowired
    private WeeklyScoreService weeklyScoreService;

    WeeklyScoreController(WeeklyScoreService weeklyScoreService) {
        this.weeklyScoreService = weeklyScoreService;
    }
    //get Rest
    @GetMapping("/students/dashboards/weeklyScores/{dashboardId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#dashboardId, 'DASHBOARD.ACCESS')")
    public List<WeeklyScoreDto> getWeeklyScore(Principal principal, @PathVariable int dashboardId){
        return this.weeklyScoreService.getWeeklyScores(dashboardId);
    }
    //Delete Rest
    @DeleteMapping("/students/weeklyScores/{weeklyScoreId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#weeklyScoreId, 'WEEKLY_SCORE.ACCESS')")
    public void removeWeeklyScore(Principal principal, @PathVariable int weeklyScoreId){
        this.weeklyScoreService.removeWeeklyScore(weeklyScoreId);
    }
    //Update Rest
}
