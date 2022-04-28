import { QuestionFactory } from "@/services/QuestionHelpers";
import Question from "../management/Question";

export default class DifficultQuestion {
  id!: number;
  percentage!: number;
  questionDto!: Question;

  constructor(jsonObj?: DifficultQuestion) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.questionDto = jsonObj.questionDto;
      this.percentage = jsonObj.percentage;
    }
  }
}