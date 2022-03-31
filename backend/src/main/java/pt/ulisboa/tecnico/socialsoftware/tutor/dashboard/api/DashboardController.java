package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DashboardService;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.DifficultQuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto.DashboardDto;

import java.security.Principal;

@RestController
public class DashboardController {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DifficultQuestionService difficultQuestionService;

    DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/students/dashboards/executions/{courseExecutionId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#courseExecutionId, 'EXECUTION.ACCESS')")
    public DashboardDto getDashboard(Principal principal, @PathVariable int courseExecutionId) {
        int studentId = ((AuthUser) ((Authentication) principal).getPrincipal()).getUser().getId();

        return this.dashboardService.getDashboard(courseExecutionId, studentId);
    }

    @DeleteMapping("/students/difficultQuestions/{difficultQuestionId}")
    @PreAuthorize("hasRole('ROLE_STUDENT') and hasPermission(#difficultQuestionId, 'DIFFICULTQUESTION.ACCESS')")
    public void removeDifficultQuestions(@PathVariable int difficultQuestionId) {
        this.difficultQuestionService.removeDifficultQuestion(difficultQuestionId);
    }

}