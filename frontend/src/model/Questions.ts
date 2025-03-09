export interface Questions {
    questions: Question[]
}

export type Question = ChoiceQuestion | OpenQuestion


export class ChoiceQuestion {
    questionUUID: string
    questionTitle: string
    questionDescription: string | null
    points: number | null
    possibleAnswersToIfCorrect: PossibleAnswersToIfCorrect[]

    picture: string | null

    constructor(questionUUID: string,
                questionTitle: string,
                questionDescription: string | null,
                points: number | null,
                possibleAnswersToIfCorrect: PossibleAnswersToIfCorrect[],
                picture: string | null
    ) {
        this.questionUUID = questionUUID
        this.questionTitle = questionTitle
        this.questionDescription = questionDescription
        this.points = points
        this.possibleAnswersToIfCorrect = possibleAnswersToIfCorrect
        this.picture = picture
    }
}

export interface PossibleAnswersToIfCorrect {
    text: string
    uuid: string
    correct: boolean | false
}

export class OpenQuestion {
    questionUUID: string
    questionTitle: string
    questionDescription: string | null
    points: number | null
    picture: string | null

    constructor(questionUUID: string,
                questionTitle: string,
                questionDescription: string | null,
                points: number | null,
                picture: string | null
    ) {
        this.questionUUID = questionUUID
        this.questionTitle = questionTitle
        this.questionDescription = questionDescription
        this.points = points
        this.picture = picture
    }
}

export enum QuestionType {
    OPEN_QUESTION = "OPEN_QUESTION",
    CHOICE_QUESTION = "CHOICE_QUESTION"
}

export const translationOfQuestionType = {
    OPEN_QUESTION: 'Въпрос с отворен отговор',
    CHOICE_QUESTION: "Въпрос със затворен отговор"
}
export const isChoiceQuestion = (question: Question): question is ChoiceQuestion =>
        question instanceof ChoiceQuestion || (question && 'possibleAnswersToIfCorrect' in question)

export const isOpenQuestion = (question: Question): question is OpenQuestion =>
        question instanceof OpenQuestion || (question && !('possibleAnswersToIfCorrect' in question))

export const defineQuestionType = (question: Question) => {
    if (isOpenQuestion(question)) {
        return QuestionType.OPEN_QUESTION
    } else if (isChoiceQuestion(question)) {
        return QuestionType.CHOICE_QUESTION
    } else {
        return null
    }
}