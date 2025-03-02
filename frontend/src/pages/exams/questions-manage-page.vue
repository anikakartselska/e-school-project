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
              <q-btn class="q-mt-lg q-mr-xs" color="primary" label="Запази промените" size="sm"
                     @click="saveUpdateExam()"></q-btn>
              <q-btn class="q-mt-lg q-mr-xs" color="secondary" label="Добави нов въпрос" size="sm"
                     @click="questionCreate()"></q-btn>
              <q-btn class="q-mt-lg q-mr-xs" color="negative" label="Премахни изпита" size="sm"
                     @click="deleteExam()"></q-btn>
            </div>
          </div>
          <q-separator></q-separator>
          <span class="q-pa-md text-negative">
            Общ брой точки на теста: {{ examPoints }}
                </span>
          <q-list v-for="(question,index) in questions">
            <div class="q-px-sm q-mt-sm row">
              <div class="col-3">
                <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN,SchoolRole.TEACHER])" color="primary" dense flat
                       icon="edit"
                       @click="questionUpdate(question,index)">
                </q-btn>
                <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN,SchoolRole.TEACHER])" color="negative" dense flat
                       icon="delete"
                       @click="questionDelete(question,index)">
                </q-btn>
              </div>
              <div>
                <div v-if="defineQuestionType(question) === QuestionType.OPEN_QUESTION">
                  <q-input v-model="question.questionTitle" :label="`${index+1}.Въпрос`" readonly stack-label>
                  </q-input>
                  <q-input v-model="question.questionDescription" label="Описание" readonly stack-label>
                  </q-input>
                  <q-input v-model="question.points" label="Точки" readonly stack-label>
                  </q-input>
                  <div class="q-pt-sm" style="max-width: 300px">
                    <q-input
                            filled
                            label="Отговор"
                            readonly
                            type="textarea"
                    />
                  </div>
                </div>
                <div v-if="defineQuestionType(question)  === QuestionType.CHOICE_QUESTION">
                  <q-input v-model="question.questionTitle" label="Въпрос" readonly stack-label>
                  </q-input>
                  <q-input v-model="question.questionDescription" label="Описание" readonly stack-label>
                  </q-input>
                  <q-input v-model="question.points" label="Точки" readonly stack-label>
                  </q-input>
                  <div v-for="(choice,index) in question.possibleAnswersToIfCorrect">
                    <div class="row">
                      <q-checkbox v-model="question.possibleAnswersToIfCorrect[index].correct"
                                  disable></q-checkbox>
                      <q-input v-model="question.possibleAnswersToIfCorrect[index].text" dense readonly></q-input>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <q-separator/>
          </q-list>
        </q-card>
        <div class="col-3"></div>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts" setup>

import {$computed, $ref} from "vue/macros";
import {defineQuestionType, Question, QuestionType} from "../../model/Questions";
import {useQuasar} from "quasar";
import QuestionEditCreateDialog from "./question-edit-create-dialog.vue";
import {currentUserHasAnyRole} from "../../services/LocalStorageService";
import {SchoolRole} from "../../model/User";
import {confirmActionPromiseDialog} from "../../utils";
import {deleteExamById, getExamById, mergeExam} from "../../services/RequestService";
import {router} from "../../router";

const quasar = useQuasar()


const props = defineProps<{
  periodId: number
  schoolId: number,
  examId: number
}>()

const exam = $ref(await getExamById(props.examId))
const questions = $ref<Question[]>(exam.questions?.questions ? exam.questions?.questions : [])
const questionCreate = () => {
  quasar.dialog({
    component: QuestionEditCreateDialog,
    componentProps: {
      title: "Добави нов въпрос"
    },
  }).onOk(async (payload) => {
    const newQuestion = payload.item as Question
    questions.push(newQuestion)
  })
}

const examPoints = $computed(() => {
  let sum = 0
  questions.forEach(questions => sum = sum + (questions.points ? Number(questions.points) : 0))
  return sum
})
const questionUpdate = (question: Question, questionIndex: number) => {
  quasar.dialog({
    component: QuestionEditCreateDialog,
    componentProps: {
      title: "Редактирай въпрос",
      question: question
    },
  }).onOk(async (payload) => {
    questions[questionIndex] = payload.item as Question
  })
}

const deleteExam = async () => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  await deleteExamById(props.examId).then(r => router.go(-1))
}
const questionDelete = async (question: Question, questionIndex: number) => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  questions.splice(questionIndex, 1)
}

const saveUpdateExam = async () => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  await mergeExam({...exam, questions: {questions: questions}}, props.schoolId, props.periodId)
}


</script>

<style scoped>

</style>