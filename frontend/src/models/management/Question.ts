import Option from "@/models/management/Option";
import Image from "@/models/management/Image";
import { Topic } from "@/models/management/Topic";

export class Question {
  id: number | null = null;
  title: string = "";
  active: boolean = true;
  numberOfAnswers!: number;
  difficulty!: number;
  content: string = "";

  image: Image | null = null;
  sequence: number | null = null;

  options: Option[] = [new Option(), new Option(), new Option(), new Option()];
  topics: Topic[] = [];

  constructor(jsonObj?: Question) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.title = jsonObj.title;
      this.active = jsonObj.active;
      this.numberOfAnswers = jsonObj.numberOfAnswers;
      this.difficulty = jsonObj.difficulty;
      this.content = jsonObj.content;
      this.image = jsonObj.image;

      this.options = jsonObj.options.map(
        (option: Option) => new Option(option)
      );

      this.topics = jsonObj.topics.map((topic: Topic) => new Topic(topic));
    }
  }
}
