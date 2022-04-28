import { ISOtoString } from '@/services/ConvertDateService';

export default class WeeklyScore {
    id!: number;
    numberAnswered!: number;
    uniquelyAnswered!: number;
    percentageCorrect!: number;
    week!: string;

    constructor(jsonObj?: WeeklyScore) {
        if (jsonObj) {
            this.id = jsonObj.id;
            if (jsonObj.numberAnswered != null)
                this.numberAnswered = jsonObj.numberAnswered;
            if (jsonObj.uniquelyAnswered != null)
                this.uniquelyAnswered = jsonObj.uniquelyAnswered;
            if (jsonObj.percentageCorrect != null)
                this.percentageCorrect = jsonObj.percentageCorrect;
            if (jsonObj.week != null)
                this.week = ISOtoString(jsonObj.week);

        }
    }
}
