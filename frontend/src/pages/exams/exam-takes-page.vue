<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-btn class="text-primary" dense flat icon="chevron_left" @click="goBack">Назад</q-btn>
      <q-table
              :columns="columns"
              :pagination="{rowsPerPage:20}"
              :rows="takes"
              class="my-sticky-header-table"
              no-data-label="Няма данни в таблицата"
              no-results-label="Няма резултати от вашето търсене"
              row-key="id"
              rows-per-page-label="Редове на страница"
              title="Учебни предмети, седмичен и годишен брой на учебните часове"
      >
        <template v-slot:top-left>
            <div class="text-h6">Опити за изпитването</div>
          <div class="text-h6">
            <span class="text-primary">
                {{ exam.examNote }}
            </span>
          </div>
        </template>
          <template v-slot:top-right>
              <div class="q-pr-xs">
                  <q-btn v-if="takes.find(it=> it.inputtedGrade === false)" color="negative"
                         label="Нанеси липсващи оценки"
                         outline rounded @click="inputGrades">
                  </q-btn>
                  <!--          </div>-->
                  <!--          <div class="q-pr-xs">-->
                  <!--            <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN])" color="primary" icon="save" label="Запази промените"-->
                  <!--                   outline rounded @click="save()">-->
                  <!--            </q-btn>-->
                  <!--          </div>-->
                  <!--          <div>-->
                  <!--            <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN])" color="negative" icon="restart_alt"-->
                  <!--                   label="Върни промените"-->
                  <!--                   outline rounded @click="reset()">-->
                  <!--            </q-btn>-->
              </div>
          </template>
        <template v-slot:body-cell-edit="props">
          <q-td>
            <q-btn dense flat icon="open_in_new" @click="openExamGradePage(props.row)">
            </q-btn>
          </q-td>
        </template>
      </q-table>
    </q-card>
    <br>
    <q-card>
      <q-table
              :columns="studentColumns"
              :pagination="{rowsPerPage:20}"
              :rows="studentsThatDidnTakeExam"
              class="my-sticky-header-table"
              color="red"
              no-data-label="Няма данни в таблицата"
              no-results-label="Няма резултати от вашето търсене"
              row-key="id"
              rows-per-page-label="Редове на страница"
              title="Учебни предмети, седмичен и годишен брой на учебните часове"
      >
        <template v-slot:top-left>
          <div class="text-h6">Непредали ученици</div>
        </template>
      </q-table>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {useRouter} from "vue-router";
import {useQuasar} from "quasar";
import {
    getAllStudentsFromSchoolClass,
    getExamAnswersForExamId,
    getExamById,
    inputGradesOnExamAnswers
} from "../../services/RequestService";
import {$computed, $ref} from "vue/macros";
import {ExamAnswers} from "../../model/ExamAnswers";
import {dateTimeToBulgarianLocaleString} from "../../utils";
import {StudentView} from "../../model/User";
import {Answer} from "../../model/Answers";


const props = defineProps<{
  periodId: number
  schoolId: number,
  examId: number,
  schoolClassId: number
}>()
const exam = await getExamById(props.examId)
const router = useRouter()
const quasar = useQuasar()
let takes = $ref(await getExamAnswersForExamId(props.examId))
const studentsFromSchoolClass = await getAllStudentsFromSchoolClass(props.schoolClassId, props.periodId)
const studentsThatDidnTakeExam = studentsFromSchoolClass.filter(student => takes.find(take => take.submittedBy.id == student.id) == null)
const openExamGradePage = async (examAnswer: ExamAnswers) => {
  await router.push(`/exam-grade-page/${props.periodId}/${props.schoolId}/${props.examId}/${examAnswer.id}`)
}

const inputGrades = async () => {
    await inputGradesOnExamAnswers(takes, props.schoolId, props.periodId).then(r =>
            takes = takes.map(take => {
                        const foundUpdatedTake = r.find(updatedTake => updatedTake.id == take.id)
                        return foundUpdatedTake ? foundUpdatedTake : take
                    }
            )
    )
}

const sumPoints = (answers: Answer[]) => {
  let sum = 0
  debugger
  answers.forEach(answer => sum = sum + (answer.points ? Number(answer.points) : 0))
  return sum
}

const examPoints = $computed(() => {
  let sum = 0
  exam.questions.questions.forEach(questions => sum = sum + (questions.points ? Number(questions.points) : 0))
  return sum
})

const goBack = async () => {
    await router.go(-1)
}
const columns = $computed(() => [
    {
        name: 'edit',
        headerClasses: 'q-table--col-auto-width'
    },
    {
        name: "submittedBy",
        label: "Предадено от",
        align: "left",
        field: (row: ExamAnswers) => row.submittedBy.firstName + ' ' + row.submittedBy.lastName,
    sortable: true
  },
  {
    name: "submittedOn",
    label: "Предадено на",
    align: "left",
    field: (row: ExamAnswers) => dateTimeToBulgarianLocaleString(row.submittedOn),
    sortable: true
  },
  {
    name: "graded",
    label: "Оценено",
    align: "left",
    field: (row: ExamAnswers) => row.graded ? 'ДА' : 'НЕ',
    sortable: true
  },
  {
    name: "points",
    label: "Точки",
    align: "left",
    field: (row: ExamAnswers) => `${sumPoints(row.answers!!.answers)}/${examPoints}`,
    sortable: true
  },
  {
    name: "inputtedGrade",
    label: "Въведена оценка",
    align: "left",
    field: (row: ExamAnswers) => row.inputtedGrade ? 'ДА' : 'НЕ',
    sortable: true
  },
  {
    name: "grade",
    label: "Оценка",
    align: "left",
    field: (row: ExamAnswers) => row.grade,
    sortable: true
  },
])

const studentColumns = [
  {name: 'edit'},
  {
    name: "firstName",
    align: "left",
    label: "Име",
    field: (row: StudentView) => row.firstName,
    sortable: true
  },
  {
    name: "middleName",
    align: "left",
    label: "Презиме",
    field: (row: StudentView) => row.middleName,
    sortable: true
  },
  {
    name: "lastName",
    align: "left",
    label: "Фамилия",
    field: (row: StudentView) => row.lastName,
    sortable: true
  },
  {
    name: "username",
    align: "left",
    label: "Потребителско име",
    field: (row: StudentView) => row.username,
    sortable: true
  },
  {
    name: "numberInClass",
    align: "left",
    label: "Номер в клас",
    field: (row: StudentView) => row.numberInClass,
    sortable: true
  },
  {
    name: "email",
    label: "Имейл",
    align: "left",
    field: (row: StudentView) => row.email,
    sortable: true
  }
]
</script>

<style lang="sass">
.my-sticky-header-table
  /* height or max-height is important */
  height: 310px

  .q-table__top,
  .q-table__bottom,
  thead tr:first-child th
    /* bg color is important for th; just specify one */
    background-color: rgba(0, 180, 255, 0.29)

  thead tr th
    position: sticky
    z-index: 1

  thead tr:first-child th
    top: 0

  /* this is when the loading indicator appears */













  &.q-table--loading thead tr:last-child th
    /* height of all previous header rows */
    top: 48px

  /* prevent scrolling behind sticky top row on focus */













  tbody
    /* height of all previous header rows */
    scroll-margin-top: 48px
</style>