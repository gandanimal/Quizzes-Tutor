describe('Student Walkthrough', () => {
    const date = new Date().toString();
    beforeEach(() => {
      //create quiz
      cy.demoTeacherLogin();
      cy.createQuestion(
        'Question Failed Answer 1 ' + date,
        'Question',
        'Option',
        'Option',
        'ChooseThisWrong',
        'Correct'
      );
      cy.createQuestion(
        'Question Failed Answer 2 ' + date,
        'Question',
        'Option',
        'Option',
        'ChooseThisWrong',
        'Correct'
      );
      cy.createQuizzWith2Questions(
        'Failed Answers Title ' + date,
        'Question Failed Answer 1 ' + date,
        'Question Failed Answer 2 ' + date,
      );
      cy.contains('Logout').click();
    });

    it('FailedAnswer', () => {
      cy.demoStudentLogin();
      cy.solveQuizz('Failed Answers Title ' + date, 2, 'ChooseThisWrong');

      cy.intercept('GET', '**/students/dashboards/executions/*').as('getDashboard');
      cy.get('[data-cy="dashboardMenuButton"]').click();
      cy.wait('@getDashboard');

      cy.intercept('GET', '**/students/dashboards/*/failedanswers').as('getFailedAnswers');
      cy.get('[data-cy="FailedAnswersButton"]').click();
      cy.wait('@getFailedAnswers');
      
      cy.get('[data-cy="RefreshFailedAnswersButton"]').click();
      cy.wait('@getFailedAnswers');

      /*
      cy.get('[data-cy="DeleteFailedAnswerButton"]').click();
      cy.wait('@getFailedAnswers');*/

      cy.contains('Logout').click();
      Cypress.on('uncaught:exception', (err, runnable) => {
        // returning false here prevents Cypress from
        // failing the test
        return false;
      });
    });
  });