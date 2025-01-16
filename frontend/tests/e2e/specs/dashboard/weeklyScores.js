describe('Weekly Scores', () => {
    const date = new Date().toString();
    beforeEach(() => {
        //create quiz
        cy.deleteQuestionsAndAnswers();
        cy.demoTeacherLogin();
        cy.createQuestion(
            'Weekly Score Question 1 ' + date,
            'Question',
            'Option',
            'Option',
            'ChooseThisWrong',
            'Correct'
        );
        cy.createQuestion(
            'Weekly Score Question 2 ' + date,
            'Question',
            'Option',
            'Option',
            'ChooseThisWrong',
            'Correct'
        );
        cy.createQuizzWith2Questions(
            'Weekly Score Title ' + date,
            'Weekly Score Question 1 ' + date,
            'Weekly Score Question 2 ' + date
        );
        cy.contains('Logout').click();
    });

    afterEach(() => {
        cy.deleteWeeklyScores();
        cy.deleteFailedAnswers();
        cy.deleteQuestionsAndAnswers();
    });

    it('student accesses weekly scores', () => {
        cy.intercept('GET', '**/students/dashboards/executions/*').as(
            'getDashboard'
        );

        cy.demoStudentLogin();
        cy.solveQuizz('Weekly Score Title ' + date, 2);
        cy.get('[data-cy="dashboardMenuButton"]').click();
        cy.wait('@getDashboard');
        cy.createWeeklyScore();
        cy.intercept('GET', '**/students/dashboards/*/weeklyscores').as(
            'getWeeklyScores'
        );
        cy.intercept('PUT', '**/students/dashboards/*/weeklyscores').as(
            'updateWeeklyScores'
        );
        cy.intercept('DELETE', '**/students/weeklyscores/*').as(
            'deleteWeeklyScores'
        );
        cy.get('[data-cy="weeklyScoresMenuButton"]').click();
        cy.wait('@getWeeklyScores');
        cy.get('[data-cy="refreshWeeklyScoreButton"]').click();
        cy.wait('@updateWeeklyScores');
        cy.get('[data-cy="deleteWeeklyScoreButton"]').click({ multiple: true });
        cy.wait('@deleteWeeklyScores');
        cy.get('[data-cy="deleteWeeklyScoreButton"]').click();
        cy.closeErrorMessage();
        cy.contains('Logout').click();
        cy.deleteWeeklyScores();
        Cypress.on('uncaught:exception', () => {
            // returning false here prevents Cypress from
            // failing the test
            return false;
        });
    });
});
