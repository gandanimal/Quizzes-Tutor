describe('Difficult Questions', () =>{
    beforeEach(() => {
        cy.deleteQuestionsAndAnswers();

        cy.demoTeacherLogin();
        cy.createQuestion(
            'Question Difficult Question 1 Sat Apr 02 2022 19:42:58 GMT+0100 (Western European Summer Time)',
            'Question',
            'Option',
            'Option',
            'ChooseThisWrong',
            'Correct'
        );
        cy.createQuestion(
            'Question Difficult Question 2 Sat Apr 02 2022 19:42:58 GMT+0100 (Western European Summer Time)',
            'Question',
            'Option',
            'Option',
            'ChooseThisWrong',
            'Correct'
        );
        cy.createQuizzWith2Questions(
            'Difficult Title Sat Apr 02 2022 19:42:58 GMT+0100 (Western European Summer Time)',
            'Question Difficult Question 1 Sat Apr 02 2022 19:42:58 GMT+0100 (Western European Summer Time)',
            'Question Difficult Question 2 Sat Apr 02 2022 19:42:58 GMT+0100 (Western European Summer Time)'
        );
        cy.contains('Logout').click();
    });

    afterEach(() => {
        cy.deleteDifficultQuestions();
        cy.deleteQuestionsAndAnswers();
    });

    it('student creates discussion', () => {
        cy.intercept('GET', '**/students/dashboards/executions/*').as(
            'getDashboard'
        );
        cy.intercept('GET', '**/students/dashboards/difficultQuestion/*').as(
            'getDifficultQuestions'
        );
        cy.intercept('PUT', '**/students/dashboards/difficultQuestion/*').as(
            'updateDifficultQuestions'
        );
        cy.intercept('DELETE', '**/students/difficultQuestions/*').as(
            'deleteDifficultQuestion'
        );

        cy.demoStudentLogin();


        cy.solveDQuizz('Difficult Title Sat Apr 02 2022 19:42:58 GMT+0100 (Western European Summer Time)', 2);

        cy.get('[data-cy="dashboardMenuButton"]').click();
        cy.wait('@getDashboard');

        cy.get('[data-cy="difficultQuestionsMenuButton"]').click();
        cy.wait('@getDifficultQuestions');

        cy.get('[data-cy="refreshDifficultQuestionsMenuButton"]').click();
        cy.wait('@updateDifficultQuestions');

        cy.get('[data-cy="showStudentViewDialog"]').should('have.length.at.least', 1).eq(0).click({ force: true });
        cy.get('[data-cy="closeButton"]').click();

        cy.get('[data-cy="deleteDifficultQuestionButton"]').should('have.length.at.least', 1).eq(0).click();
        cy.wait('@deleteDifficultQuestion');

        cy.get('[data-cy="deleteDifficultQuestionButton"]').should('have.length.at.least', 1).eq(0).click();
        cy.wait('@deleteDifficultQuestion');


        cy.contains('Logout').click();
        Cypress.on('uncaught:exception', (err, runnable) =>{
            return false;
        });
    });
});