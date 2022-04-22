import { ISOtoString } from '@/services/ConvertDateService';
import { QuestionAnswer } from '@/models/management/QuestionAnswer'

export default class FailedAnswer {
    id!: number;
    collected!: string;
    answered!: string;
    questionAnswerDto!: QuestionAnswer;
    content!: string;

    constructor(jsonObj?: FailedAnswer) {
        if (jsonObj) {
            this.id = jsonObj.id;
            if(jsonObj.collected)
                this.collected = ISOtoString(jsonObj.collected);
            if(jsonObj.answered) {
                this.answered = "Yes";
            }
            else if(!jsonObj.answered) {
                    this.answered = "No";
            }
            this.questionAnswerDto = jsonObj.questionAnswerDto;
            this.content = this.questionAnswerDto.question.content;
        }
    }
}