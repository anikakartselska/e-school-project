<template>
  <q-page class="q-pa-sm bg-sms">
    <div class="row">
      <div class="col-3"></div>
      <div class="col-6">
        <q-card>
          <q-btn class="text-primary" dense flat icon="chevron_left" @click="goBack">Назад</q-btn>
          <div class="row">
            <span class="q-pa-lg text-h5">{{ exam.examNote }}</span>
            <q-space></q-space>
            <div>
              <q-btn v-if="!examAnswer.graded && !examAnswer.cancelled" class="q-mt-lg q-mr-xs" color="negative"
                     label="Анулиране на изпита"
                     size="sm" @click="cancelExamAnswer()"></q-btn>
              <q-btn v-if="!examAnswer.graded && !examAnswer.cancelled" class="q-mt-lg q-mr-xs" color="secondary"
                     label="Завърши проверката"
                     size="sm" @click="submitExamCheck()"></q-btn>

              <div v-if="examAnswer.graded && !examAnswer.inputtedGrade">
                <q-btn class="q-mt-lg q-mr-xs" color="secondary"
                       label="Въведи оценка"
                       size="sm" @click="inputGrade()"></q-btn>
                <div v-if="examAnswer.graded" class="q-ml-lg text-bold text-primary">
                  Оценка ({{ examAnswer.grade }})
                </div>
              </div>
              <div v-if="examAnswer.graded && examAnswer.inputtedGrade"
                   class="text-bold text-primary q-mr-lg q-mt-lg">
                Оценка ({{ examAnswer.grade }})
              </div>
              <div v-if="examAnswer.cancelled"
                   class="text-bold text-negative q-mr-lg q-mt-lg">
                АНУЛИРАН
              </div>
            </div>
          </div>
          <q-separator></q-separator>
          <span class="q-pa-md text-negative">
            Общ брой точки на теста: {{ examPoints }}
                </span>
          <div class="q-pa-md text-bold">
            Брой точки на текущия опит: {{ currentTakeExamPoints }}
          </div>
          <div>
            <q-list v-for="(question,index) in questions">
              <div class="q-px-sm q-mt-sm row">
                <div class="col-3 text-primary q-pr-lg">
                  <q-input v-model="examAnswer.answers.answers[index].points" :disable="examAnswer.graded"
                           :prefix="`Точки`"
                           :rules="[val=>val !== null && val <= question.points || 'Невалидни точки']"
                           :suffix="`/${question.points} макс`"
                           dense
                           type="number"
                  ></q-input>
                </div>
                <div>
                  <div v-if="defineQuestionType(question) === QuestionType.OPEN_QUESTION">
                  <span class="text-bold">
                      {{ index + 1 + '. ' + question.questionTitle }}
                  </span>
                    <br>
                    <span class="text-italic">{{ question.questionDescription }}</span>
                    <div class="q-pt-sm" style="max-width: 500px">
                      <q-input
                              v-model="examAnswer.answers.answers[index].openQuestionAnswer"
                              filled
                              label="Отговор"
                              readonly
                              type="textarea"
                      />

                    </div>
                    <div>
                      <q-avatar v-if="question.picture" class="q-ma-sm" font-size="400px" size="400px"
                                square text-color="white">
                        <q-img
                                :src="imageUrl(questionUUIDToFile[question.questionUUID])"
                                fit="contain"
                                ratio="1"
                                spinner-color="white"
                        ></q-img>
                      </q-avatar>
                    </div>
                  </div>
                  <div v-if="defineQuestionType(question) === QuestionType.CHOICE_QUESTION">
                  <span class="text-bold">
                      {{ index + 1 + '. ' + question.questionTitle }}
                  </span>
                    <br>
                    <span class="text-italic">{{ question.questionDescription }}</span>
                    <q-option-group v-model="examAnswer.answers.answers[index].questionAnswers"
                                    :options="transferToOptionsList(question.possibleAnswersToIfCorrect)"
                                    disable
                                    type="checkbox"
                    >
                      <template v-slot:label="opt">
                        <div class="row items-center">
                             <span :class="question.possibleAnswersToIfCorrect.find(it=> it.uuid === opt.value)?.correct ? `text-secondary`:``">{{
                                 opt.label
                               }}</span>
                          <q-icon :name="opt.icon" class="q-ml-sm" color="teal" size="1.5em"/>
                        </div>
                      </template>
                    </q-option-group>
                    <div>
                      <q-avatar v-if="question.picture" class="q-ma-sm" font-size="400px" size="400px"
                                square text-color="white">
                        <q-img
                                :src="imageUrl(questionUUIDToFile[question.questionUUID])"
                                fit="contain"
                                ratio="1"
                                spinner-color="white"
                        ></q-img>
                      </q-avatar>
                    </div>
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
  </q-page>
</template>

<script lang="ts" setup>

import {$computed, $ref} from "vue/macros";
import {defineQuestionType, PossibleAnswersToIfCorrect, Question, QuestionType} from "../../model/Questions";
import {useQuasar} from "quasar";
import {getExamAnswersById, getExamById, mergeExamAnswers} from "../../services/RequestService";
import {getCurrentUserAsUserView} from "../../services/LocalStorageService";
import {confirmActionPromiseDialog, notifyForError} from "../../utils";
import {useRouter} from "vue-router";

const quasar = useQuasar()
const router = useRouter()

const props = defineProps<{
  periodId: number
  schoolId: number,
  examId: number,
  examAnswerId: number,
}>()
const currentUser = getCurrentUserAsUserView()
let examAnswer = $ref(await getExamAnswersById(props.examAnswerId))
const exam = $ref(await getExamById(props.examId))
const examAnswerIds = examAnswer.answers?.answers ? <string[]>examAnswer.answers?.answers?.map(it => it.questionUUID) : <string[]>[]
let questions = $ref<Question[]>((exam.questions?.questions ? exam.questions?.questions : []).sort((a, b) => examAnswerIds.indexOf(a.questionUUID) - examAnswerIds.indexOf(b.questionUUID)))
const questionUUIDToFile = $ref({})
questions.forEach(question =>
        questionUUIDToFile[question.questionUUID] = question.picture ? base64ToImageFile(question.picture, question.questionUUID) : null
)

const imageUrl = (file: File) => {
  return file ? window.URL.createObjectURL(file) : ''
}

function base64ToImageFile(base64String: string, fileName: string): File {
  const arr = base64String.split(",");
  const mimeType = arr[0].match(/:(.*?);/)?.[1] || "image/png";
  const byteCharacters = atob(arr[1]); // Decode Base64
  const byteNumbers = new Uint8Array(byteCharacters.length);

  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }

  const blob = new Blob([byteNumbers], {type: mimeType});
  return new File([blob], fileName, {type: mimeType});
}

const goBack = async () => {
  await router.go(-1)
}
const examPoints = $computed(() => {
  let sum = 0
  questions.forEach(questions => sum = sum + (questions.points ? Number(questions.points) : 0))
  return sum
})

const currentTakeExamPoints = $computed(() => {
  let sum = 0
  examAnswer.answers?.answers?.forEach(answer => sum = sum + (answer.points ? Number(answer.points) : 0))
  return sum
})
const submitExamCheck = async () => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  if (!exam.gradingScale || examPoints < +exam.gradingScale?.interval6.endingPoints) {
    notifyForError("Грешка при завършване на проверката", "Скалата за оценяване е невалидна")
  } else {
    let grade = 2
    if (+exam.gradingScale.interval3.startingPoints <= currentTakeExamPoints && +exam.gradingScale.interval3.endingPoints >= currentTakeExamPoints) {
      grade = 3
    } else if (+exam.gradingScale.interval4.startingPoints <= currentTakeExamPoints && +exam.gradingScale.interval4.endingPoints >= currentTakeExamPoints) {
      grade = 4
    } else if (+exam.gradingScale.interval5.startingPoints <= currentTakeExamPoints && +exam.gradingScale.interval5.endingPoints >= currentTakeExamPoints) {
      grade = 5
    } else if (+exam.gradingScale.interval6.startingPoints <= currentTakeExamPoints && +exam.gradingScale.interval6.endingPoints >= currentTakeExamPoints) {
      grade = 6
    }
    await mergeExamAnswers({
      ...examAnswer,
      graded: true,
      grade: grade.toString()
    }, props.schoolId, props.periodId, props.examId)
            .then(r => examAnswer = r)
  }
}

const inputGrade = async () => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  await mergeExamAnswers({
    ...examAnswer,
    inputtedGrade: true
  }, props.schoolId, props.periodId, props.examId)
          .then(r => examAnswer = r)
}

const cancelExamAnswer = async () => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")

  await mergeExamAnswers({
    ...examAnswer,
    cancelled: true
  }, props.schoolId, props.periodId, props.examId)
          .then(r => examAnswer = r)
}
const transferToOptionsList = (possibleAnswersToIfCorrect: PossibleAnswersToIfCorrect[]) => {
  return possibleAnswersToIfCorrect.map((opt, i) => {
    return {label: opt.text, value: opt.uuid}
  })
}

const gradeExam = () => {
  questions.forEach((question, index) => {

    if (defineQuestionType(question) === QuestionType.CHOICE_QUESTION) {
      if (examAnswer?.answers?.answers[index]?.questionAnswers?.every(answer =>
              question.possibleAnswersToIfCorrect.find(it => it.uuid == answer).correct
      ) && question.possibleAnswersToIfCorrect.filter(it => it.correct).length == examAnswer?.answers?.answers[index]?.questionAnswers.length) {
        examAnswer!!.answers!!.answers[index]!!.points = question.points
      }
    }
  })
}

gradeExam()

</script>

<style scoped>

</style>