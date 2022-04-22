<template>
  <v-card>
    <v-card-title>Failed Answers
        <v-spacer></v-spacer>
        <v-btn
            color="primary"
            v-on:click="this.update"
          >Refresh</v-btn>
    </v-card-title>
    <v-data-table
      :headers="headers"
      :items="failedAnswers"
      :sort-by="['collected']"
      :items-per-page="15"
      :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
    >
      <template v-slot:[`item.action`]="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              class="mr-2 action-button"
              v-on="on"
              data-cy="deleteQuestionButton"
              @click="deleteFailedAnswer(item)"
              color="red"
            >delete</v-icon> 
          </template>
          <span>Delete Failed Answer</span>
        </v-tooltip>
      </template>
    </v-data-table>
  </v-card>
    
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import FailedAnswers from '@/models/dashboard/FailedAnswer';

@Component
export default class FailedAnswersView extends Vue {
  @Prop() readonly dashboardId!: number;
  failedAnswers: FailedAnswers[] = [];

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
      value: 'content', 
      width: '50%', 
      align: 'left', 
      sortable: false,
    },
    {
      text: 'Answered',
      value: 'answered',
      width: '5px',
      align: 'center',
      sortable: false,
    },
    {
      text: 'Collected',
      value: 'collected',
      width: '150px',
      align: 'center',
    },
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.failedAnswers = await RemoteServices.getFailedAnswers(this.dashboardId);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

    async update() {
    await this.$store.dispatch('loading');
    try {
      await RemoteServices.updateFailedAnswers(this.dashboardId);
      this.failedAnswers = await RemoteServices.getFailedAnswers(this.dashboardId);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async deleteFailedAnswer(deleteFailedAnswer: FailedAnswers) {
    await this.$store.dispatch('loading');
    try{
      await RemoteServices.deleteFailedAnswers(deleteFailedAnswer.id);
    } catch (error) {
        await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearloading');
  }

}
</script>