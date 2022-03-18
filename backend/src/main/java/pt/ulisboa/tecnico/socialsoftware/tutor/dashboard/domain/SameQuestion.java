package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.domain.FailedAnswer;
import java.util.List;
import java.util.ArrayList;

import javax.persistence.*;


@Entity
public class SameQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private FailedAnswer failedAnswer;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sameQuestion", orphanRemoval = true)
    private List<FailedAnswer> answers;

    public SameQuestion(FailedAnswer answer) {
        failedAnswer = answer;
        answers = new ArrayList<>();
    }

    public void setSameQuestion (FailedAnswer equal) {
        answers.add(equal);
    }

    public void removeSameQuestion (FailedAnswer equal) {
        answers.remove(equal);
    }
}
