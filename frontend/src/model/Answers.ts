export interface Answers {
    answers: Answer[]
}

export type Answer = ChoiceQuestionAnswer | OpenQuestionAnswer


export class ChoiceQuestionAnswer {
    questionAnswers: string[] = []
    points: number | null
    questionUUID: string

    constructor(questionAnswers: string[],
                points: number | null,
                questionUUID: string
    ) {
        this.questionAnswers = questionAnswers
        this.points = points
        this.questionUUID = questionUUID

    }
}

export class OpenQuestionAnswer {
    openQuestionAnswer: string | null
    points: number | null
    questionUUID: string

    constructor(openQuestionAnswer: string | null,
                points: number | null,
                questionUUID: string
    ) {
        this.openQuestionAnswer = openQuestionAnswer
        this.points = points
        this.questionUUID = questionUUID

    }
}

// export enum QuestionType {
//     OPEN_QUESTION = "OPEN_QUESTION",
//     CHOICE_QUESTION = "CHOICE_QUESTION"
// }
//
// export const translationOfQuestionType = {
//     OPEN_QUESTION: 'Въпрос с отворен отговор',
//     CHOICE_QUESTION: "Въпрос със затворен отговор"
// }
// export const isChoiceQuestion = (question: Question): question is ChoiceQuestion =>
//         question instanceof ChoiceQuestion || (question && 'possibleAnswersToIfCorrect' in question)
//
// export const isOpenQuestion = (question: Question): question is OpenQuestion =>
//         question instanceof OpenQuestion || (question && !('possibleAnswersToIfCorrect' in question))
