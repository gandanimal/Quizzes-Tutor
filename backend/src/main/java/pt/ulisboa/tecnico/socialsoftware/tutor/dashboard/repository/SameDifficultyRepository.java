package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.SameDifficulty;
import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.SameQuestion;

@Repository
@Transactional
public interface SameDifficultyRepository extends JpaRepository<SameDifficulty, Integer> {
}