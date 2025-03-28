<template>
  <q-page class="q-pa-sm bg-sms">
    <div class="row">
      <div class="col-3"></div>
      <div class="col-6">
        <q-card>
            <q-btn class="text-primary" dense flat icon="chevron_left" @click="goBack">Назад</q-btn>
            <q-separator/>
            <div class="row">
                <div class="col-7">
                    <div class="row">
                        <div class="q-pl-lg text-h5">{{ exam.examNote }}</div>
                        <q-btn color="primary" dense flat icon="edit" size="sm" @click="editExam()"></q-btn>
                    </div>
                    <div class="q-pl-lg"><b>От:</b> {{ dateTimeToBulgarianLocaleString(exam.startTimeOfExam) }}</div>
                    <div class="q-pl-lg">
                        <b>До:</b> {{ dateTimeToBulgarianLocaleString(exam.endTimeOfExam) }}
                    </div>
                </div>
                <div class="col">
                    Позволи на учениците да виждат <br> отговорите си след проверка на изпита
                    <q-checkbox v-model="exam.lookAtExamAfterGrading" left-label
                                @click="saveExamWithoutUpdatingQuestions">
                    </q-checkbox>
                </div>
            </div>
            <div class="q-pl-sm">
                <q-btn class="q-mt-lg q-mr-xs" color="primary" label="Запази промените" size="sm"
                       :disable="disableEditing"
                       @click="saveUpdateExam()"></q-btn>
                <q-btn class="q-mt-lg q-mr-xs" color="secondary" label="Добави нов въпрос" size="sm"
                       :disable="disableEditing"
                       @click="questionCreate()"></q-btn>
                <q-btn class="q-mt-lg q-mr-xs" color="negative" label="Премахни изпита" size="sm"
                       :disable="disableEditing"
                       @click="deleteExam()"></q-btn>
                <q-btn class="q-mt-lg q-mr-xs" color="teal" label="Опити" size="sm"
                       @click="openTakesPage()"></q-btn>
                <q-btn class="q-mt-lg q-mr-xs" color="accent" label="Скала за оценяване" size="sm"
                       @click="openGradingScalePage()"></q-btn>
            </div>
            <q-separator class="q-mt-sm"></q-separator>
            <span class="q-pa-md text-negative">
            Общ брой точки на теста: {{ examPoints }}
                </span>
            <q-list v-for="(question,index) in questions">
                <div class="q-px-sm q-mt-sm row">
                    <div class="col-3">
                        <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN,SchoolRole.TEACHER])" :disable="disableEditing" color="primary"
                               dense
                               flat
                               icon="edit"
                               @click="questionUpdate(question,index)">
                </q-btn>
                <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN,SchoolRole.TEACHER])" color="negative" dense flat
                       icon="delete"
                       :disable="disableEditing"
                       @click="questionDelete(question,index)">
                </q-btn>
              </div>
                    <div>
                        <div v-if="defineQuestionType(question) === QuestionType.OPEN_QUESTION">
                            <q-input v-model="question.questionTitle" :label="`${index+1}.Въпрос`" autogrow readonly
                                     stack-label>
                            </q-input>
                            <q-input v-model="question.questionDescription" autogrow label="Описание" readonly
                                     stack-label>
                            </q-input>
                            <q-input v-model="question.points" autogrow label="Точки" readonly stack-label>
                            </q-input>
                            <div class="q-pt-sm" style="max-width: 500px">
                                <q-input
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
                                            :src="imageUrl(questionUUIDToFile[question.questionUUID],question.questionUUID)"
                                            fit="contain"
                                            ratio="1"
                                            spinner-color="white"
                                    ></q-img>
                                </q-avatar>
                            </div>
                        </div>
                        <div v-if="defineQuestionType(question)  === QuestionType.CHOICE_QUESTION">
                            <q-input v-model="question.questionTitle" autogrow label="Въпрос" readonly stack-label>
                            </q-input>
                            <q-input v-model="question.questionDescription" autogrow label="Описание" readonly
                                     stack-label>
                            </q-input>
                            <q-input v-model="question.points" autogrow label="Точки" readonly stack-label>
                            </q-input>
                            <div v-for="(choice,index) in question.possibleAnswersToIfCorrect">
                                <div class="row">
                                    <q-checkbox v-model="question.possibleAnswersToIfCorrect[index].correct"
                                                disable></q-checkbox>
                                    <q-input v-model="question.possibleAnswersToIfCorrect[index].text" autogrow dense
                                             readonly></q-input>
                                </div>
                            </div>
                            <div>
                                <q-avatar v-if="question.picture" class="q-ma-sm" font-size="400px" size="400px"
                                          square text-color="white">
                                    <q-img
                                            :src="imageUrl(questionUUIDToFile[question.questionUUID],question.questionUUID)"
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
import {confirmActionPromiseDialog, dateTimeToBulgarianLocaleString} from "../../utils";
import {deleteExamById, getAssignmentForExam, getExamById, mergeExam} from "../../services/RequestService";
import {router} from "../../router";
import GradingScaleCreateEdit from "./grading-scale-create-edit.vue";
import {GradingScale} from "../../model/GradingScale";
import AddExamDialog from "./add-exam-dialog.vue";


const quasar = useQuasar()

const props = defineProps<{
    periodId: number
    schoolId: number,
    examId: number,
    schoolClassId: number
}>()

let exam = $ref(await getExamById(props.examId))

const currentDate = new Date();
const startTimeOfExam = new Date(exam.startTimeOfExam)

const disableEditing = currentDate > startTimeOfExam


let assignment = $ref(await getAssignmentForExam(props.schoolId, props.periodId, props.examId))
let questions = $ref<Question[]>(exam.questions?.questions ? exam.questions?.questions : [])
const questionUUIDToFile = $ref({})
questions.forEach(question =>
        questionUUIDToFile[question.questionUUID] = question.picture ? base64ToImageFile(question.picture, question.questionUUID) : null
)

const cachedImageUrls = new Map<string, string>();
const imageUrl = (file: File, uuid: string) => {
    if (!file) return '';
    if (!cachedImageUrls.has(uuid)) {
        cachedImageUrls.set(uuid, window.URL.createObjectURL(file));
    }
    return cachedImageUrls.get(uuid);
};

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

const questionCreate = () => {
    quasar.dialog({
        component: QuestionEditCreateDialog,
        componentProps: {
            title: "Добави нов въпрос"
        },
    }).onOk(async (payload) => {
        const newQuestion = payload.item as Question
        questions.push(newQuestion)
        questionUUIDToFile[newQuestion.questionUUID] = newQuestion.picture ? base64ToImageFile(newQuestion.picture, newQuestion.questionUUID) : null
    })
}

const openGradingScalePage = () => {
  quasar.dialog({
    component: GradingScaleCreateEdit,
    componentProps: {
      gradingScale: exam.gradingScale,
      maximumPoints: examPoints
    },
  }).onOk(async (payload) => {
    const gradingScale = payload.item as GradingScale
    debugger
    await mergeExam({...exam, gradingScale: <GradingScale>gradingScale}, props.schoolId, props.periodId).then(r =>
            exam = r
    )

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
      questionUUIDToFile[payload.item.questionUUID] = payload.item.picture ? base64ToImageFile(payload.item.picture, payload.item.questionUUID) : null
  })
}

const editExam = () => {
    quasar.dialog({
        component: AddExamDialog,
        componentProps: {
            exam: exam
        }
    }).onOk(async (payload) => {

        await mergeExam(payload.item.exam, props.schoolId, props.periodId).then(async e => {
            exam = e
        })
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

const openTakesPage = async () => {
    await router.push(`/exam-takes-page/${props.periodId}/${props.schoolId}/${props.examId}/${props.schoolClassId}`)
}

const saveUpdateExam = async () => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
    await mergeExam({...exam, questions: {questions: questions}}, props.schoolId, props.periodId)
}

const saveExamWithoutUpdatingQuestions = async () => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
    await mergeExam({...exam}, props.schoolId, props.periodId)
}

const goBack = async () => {
    await router.go(-1)
}

</script>

<style scoped>

</style>