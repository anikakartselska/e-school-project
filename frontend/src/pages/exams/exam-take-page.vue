<template>
  <q-page class="q-pa-sm bg-sms">
    <div class="row">
      <div class="col-3"></div>
      <div class="col-6">
        <q-card>
          <div class="row">
            <span class="q-pa-lg text-h5">{{ exam.examNote }}</span>
            <q-space></q-space>
            <div>
              <q-btn v-if="!examAnswer" class="q-mt-lg q-mr-xs" color="primary" label="Предай изпита"
                     size="sm" @click="submitExam()"></q-btn>
            </div>
          </div>
          <q-separator></q-separator>
          <span v-if="!examAnswer" class="q-pa-md text-negative">
            Общ брой точки на теста: {{ examPoints }}
                </span>
          <div v-if="examAnswer" class="q-pa-md text-bold">
            Вече сте предали вашият изпит<br>
            Статуса на изпита е: {{ examAnswer.graded ? "ОЦЕНЕН" : "НЕОЦЕНЕН" }}
          </div>
          <div v-else>
            <q-list v-for="(question,index) in questions">
              <div class="q-px-sm q-mt-sm row">
                <div class="col-1 text-primary">
                  Точки {{ question.points }}
                </div>
                <div>
                  <div v-if="defineQuestionType(question) === QuestionType.OPEN_QUESTION">
                  <span class="text-bold">
                      {{ index + 1 + '. ' + question.questionTitle }}
                  </span>
                    <br>
                    <span class="text-italic">{{ question.questionDescription }}</span>
                    <div class="q-pt-sm" style="max-width: 300px">
                      <q-input
                              v-model="answers[index].openQuestionAnswer"
                              filled
                              label="Отговор"
                              type="textarea"
                      />
                    </div>
                  </div>
                  <div v-if="defineQuestionType(question)  === QuestionType.CHOICE_QUESTION">
                  <span class="text-bold">
                      {{ index + 1 + '. ' + question.questionTitle }}
                  </span>
                    <br>
                    <span class="text-italic">{{ question.questionDescription }}</span>
                    <q-option-group v-model="answers[index].questionAnswers"
                                    :options="transferToOptionsList(question.possibleAnswersToIfCorrect)"
                                    type="checkbox"
                                    @update:model-value="value=> handleOneOption(value,question.possibleAnswersToIfCorrect,index)">
                    </q-option-group>
                  </div>
                </div>
              </div>
              <q-separator/>
            </q-list>
          </div>
        </q-card>
        <div class="col-3"></div>
      </div>
    </div>
    {{ answers }}
  </q-page>
</template>

<script lang="ts" setup>

import {$computed, $ref} from "vue/macros";
import {
    defineQuestionType,
    isChoiceQuestion,
    PossibleAnswersToIfCorrect,
    Question,
    QuestionType
} from "../../model/Questions";
import {useQuasar} from "quasar";
import {getExamAnswersByExamIdAndSubmittedById, getExamById, mergeExamAnswers} from "../../services/RequestService";
import {Answer, ChoiceQuestionAnswer, OpenQuestionAnswer} from "../../model/Answers";
import {getCurrentUserAsUserView} from "../../services/LocalStorageService";
import {ExamAnswers} from "../../model/ExamAnswers";

const quasar = useQuasar()


const props = defineProps<{
  periodId: number
  schoolId: number,
  examId: number
}>()
const currentUser = getCurrentUserAsUserView()
let examAnswer = $ref(await getExamAnswersByExamIdAndSubmittedById(props.examId, currentUser.id))
const newExamAnswer = <ExamAnswers>{submittedBy: currentUser,}
const exam = $ref(await getExamById(props.examId))
const questions = $ref<Question[]>(exam.questions?.questions ? shuffle(exam.questions?.questions) : [])
const answers = $ref<Answer[]>(questions.map(quest => {
          if (isChoiceQuestion(quest)) {
            return <ChoiceQuestionAnswer>{questionUUID: quest.questionUUID, points: 0, questionAnswers: []}
          } else {
            return <OpenQuestionAnswer>{questionUUID: quest.questionUUID, points: 0}
          }
        }
))

const examPoints = $computed(() => {
  let sum = 0
  questions.forEach(questions => sum = sum + (questions.points ? Number(questions.points) : 0))
  return sum
})

const submitExam = async () => {
  await mergeExamAnswers({...newExamAnswer, answers: {answers: answers}}, props.schoolId, props.periodId, props.examId)
          .then(r => examAnswer = r)
}

const transferToOptionsList = (possibleAnswersToIfCorrect: PossibleAnswersToIfCorrect[]) => {
  return possibleAnswersToIfCorrect.map((opt, i) => {
    return {label: opt.text, value: opt.uuid}
  })
}


const handleOneOption = (questionAnswers: string[], possibleAnswersToIfCorrect: PossibleAnswersToIfCorrect[], index: number) => {
  const answersLength = possibleAnswersToIfCorrect.filter((opt) => opt.correct).length
  debugger
  if (questionAnswers.length > answersLength) {
    answers[index].questionAnswers = answers[index].questionAnswers.slice(1, questionAnswers.length)
  }
}

function shuffle<T>(array: T[]): T[] {
  let currentIndex = array.length, randomIndex;

  // While there remain elements to shuffle.
  while (currentIndex != 0) {

    // Pick a remaining element.
    randomIndex = Math.floor(Math.random() * currentIndex);
    currentIndex--;

    // And swap it with the current element.
    [array[currentIndex], array[randomIndex]] = [
      array[randomIndex], array[currentIndex]];
  }

  return array;
}
</script>

<style scoped>

</style>