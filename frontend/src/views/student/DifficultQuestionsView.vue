<template>
  <div>
     <v-card class="table">
       <v-card-title>         
            DifficultQuestions            
          </v-card-title>
        
       <v-btn
            color="primary"
            v-on:click = "this.update"
            data-cy="refreshDifficultQuestionsMenuButton"
          >Refresh
        </v-btn>         
      <v-data-table
        :headers="headers"
        :items="difficultQuestions"
        :sort-by="['percentage']"
        sort-desc
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
      >        
        <template v-slot:[`item.action`]="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                v-on="on"
                @click="showStudentViewDialog(item)"
                data-cy="showStudentViewDialog"
                >school</v-icon
              >
            </template>
            <span>Student View</span>
          </v-tooltip>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                v-on="on"
                @click="remove(item)"
                data-cy="deleteDifficultQuestionButton"
                color="red"
                >delete</v-icon
              >
            </template>
            <span>Delete Question</span>
          </v-tooltip>
        </template>
      </v-data-table>
      <student-view-dialog
        v-if="statementQuestion && studentViewDialog"
        v-model="studentViewDialog"
        :statementQuestion="statementQuestion"
        v-on:close-show-question-dialog="onCloseStudentViewDialog"
      />
      
    </v-card>
    
   </div>  
</template>

<script lang= "ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import DifficultQuestion from '@/models/dashboard/DifficultQuestion';
import StudentViewDialog from '@/views/teacher/questions/StudentViewDialog.vue';
import StatementQuestion from '@/models/statement/StatementQuestion';
import Question from '@/models/management/Question';

@Component
({
  components: {
    'student-view-dialog': StudentViewDialog,
  },
})
export default class DifficultQuestionsView extends Vue{

    @Prop() readonly dashboardId!: number;
    difficultQuestions: DifficultQuestion[] = []
    currentQuestion: DifficultQuestion | null = null;
    statementQuestion: StatementQuestion | null = null;
    studentViewDialog: boolean = false

     headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      width: '5px',
      sortable: false,
    },

    {
      text: 'Question',
      value: 'questionsDto.title',
      align: 'left',
      width: '100%',
      sortable: false,
    },

    {
      text: 'Percentage',
      value: 'percentage',
      align: 'center',
      width: '30px',
      sortable: true,
    }

  ]


    async created() {
    await this.$store.dispatch('loading');
    try {
      this.difficultQuestions = await RemoteServices.getDifficultQuestions(this.dashboardId);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async update() {
    await this.$store.dispatch('loading');
    try {
      await RemoteServices.updateDifficultQuestions(this.dashboardId);
      this.difficultQuestions = await RemoteServices.getDifficultQuestions(this.dashboardId);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async remove(difficultQuestion: DifficultQuestion) {
    await this.$store.dispatch('loading');
    try {
      await RemoteServices.removeDifficultQuestions(difficultQuestion.id);
      this.difficultQuestions = this.difficultQuestions.filter(
          (difficultQuestions) => difficultQuestions.id != difficultQuestion.id
        );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async showStudentViewDialog(difficultQuestion: DifficultQuestion) {
    if (difficultQuestion.questionDto.id){
      try {
        this.statementQuestion = await RemoteServices.getStatementQuestion(
          difficultQuestion.questionDto.id
        );
        this.studentViewDialog = true;
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }
}
</script>