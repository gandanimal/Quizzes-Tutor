<template>
  <div class="container">
    <v-card class="table" color="rgba(255,255,255, 0.8)">
      <v-row>
        <v-col>
          <h2 class="v-card__title" style="margin-left: 30px">Weekly Scores</h2>
        </v-col>
        <v-col>
          <v-btn
            style="margin-top: 10px"
            color="primary"
            v-on:click="this.refresh"
            >Refresh
          </v-btn>
        </v-col>
      </v-row>
      <v-data-table
        :headers="headers"
        :items="weeklyScores"
        :sort-by="['week']"
        :items-per-page="10"
        class="elevation-1"
      >
        <template v-slot:[`item.action`]="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                v-on="on"
                data-cy="deleteQWeeklyScoreButton"
                @click="remove(item)"
                color="red"
                >delete
              </v-icon>
            </template>
            <span> remove Weekly Score</span>
          </v-tooltip>
        </template>
      </v-data-table>
    </v-card>
  </div>
</template>



<script lang="ts">
import WeeklyScore from '@/models/dashboard/WeeklyScore';
import RemoteServices from '@/services/RemoteServices';
import { Component, Prop, Vue } from 'vue-property-decorator';

@Component
export default class WeeklyScoresView extends Vue {
  @Prop() readonly dashboardId!: number;
  weeklyScores: WeeklyScore[] = [];

  data() {
    return {
      headers: [
        {
          text: 'Actions',
          align: 'start',
          sortable: false,
          value: 'action',
        },
        { text: 'Week', value: 'week' },
        { text: 'Number Answered', value: 'numbersAnswered' },
        { text: 'Uniquely Answered', value: 'uniquelyAnswered' },
        { text: 'Percentage Correct', value: 'percentageCorrect' },
      ],
    };
  }

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.weeklyScores = await RemoteServices.getWeeklyScores(
        this.dashboardId
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async refresh() {
    await this.$store.dispatch('loading');
    try {
      await RemoteServices.updateWeeklyScores(this.dashboardId);
      this.weeklyScores = await RemoteServices.getWeeklyScores(
        this.dashboardId
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async remove(deleteWeeklyScore: WeeklyScore) {
    await this.$store.dispatch('loading');
    if (confirm('Are you sure you want to delete this question?')) {
      try {
        await RemoteServices.deleteWeeklyScores(deleteWeeklyScore.id);
        this.weeklyScores = await RemoteServices.getWeeklyScores(
          this.dashboardId
        );
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
      await this.$store.dispatch('clearLoading');
    }
  }
}
</script>
