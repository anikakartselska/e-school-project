<template>
  <q-table
          :columns="columns"
          :pagination="{rowsPerPage:20}"
          :row-key="getRowKey"
          :rows="grades"
          :visible-columns="visibleColumns"
          no-data-label="Няма данни в таблицата"
          separator="cell"
          title="Оценки"
  >
    <template v-slot:top-right>
      <q-btn v-if="semester !== Semester.YEARLY"
             color="primary"
             icon="add_circle_outline"
             label="Добави оценки за повече ученици"
             outline
             @click="addNewGrades(false)"
      />
      <q-btn class="q-ml-sm"
             color="negative"
             icon="add_circle_outline"
             label="Оформи успех"
             outline
             @click="addNewGrades(true)"
      />
    </template>
    <template v-slot:body-cell-grades="props">
      <q-td class="text-center">
        <q-btn v-for="grade in props.row?.grades?.filter(it => !(it.evaluationValue.finalGrade === true) && it.semester === semester)"
               v-if="props.row?.student?.firstName !== undefined"
               :class="`q-ma-xs ${gradeBackgroundColorMap.get(grade.evaluationValue.grade)}`"
               :label="gradeMap.get(grade.evaluationValue.grade)?.toString()"
               flat
               rounded>
          <q-popup-proxy>
            <q-banner>
              Въведен от:<span class="text-primary">{{
                grade.createdBy.firstName
              }} {{ grade.createdBy.lastName }}</span><br/>
              Дата:<span class="text-primary">{{
                grade.evaluationDate
              }}</span><br/>
            </q-banner>
          </q-popup-proxy>
        </q-btn>
      </q-td>
    </template>
    <template v-slot:body-cell-average="props">
      <q-td class="text-center">
        <q-btn v-if="!isNaN(calculateAverageGrade(props.row?.grades?.filter(it => !(it.evaluationValue.finalGrade === true) && it.semester === semester)))"
               :class="`q-ma-xs ${getAverageGradeColorClass(calculateAverageGrade(props.row?.grades?.filter(it => !(it.evaluationValue.finalGrade === true) && it.semester === semester)))}`"
               :label="calculateAverageGrade(props.row?.grades?.filter(it => !(it.evaluationValue.finalGrade === true) && it.semester === semester)) ? calculateAverageGrade((props.row?.grades?.filter(it => !(it.evaluationValue.finalGrade === true && it.semester === semester)))) : ''"
               flat
               rounded>
          <q-tooltip>
            Средноаритметична оценка
          </q-tooltip>
        </q-btn>
      </q-td>
    </template>
    <template v-slot:body-cell-finalGrade="props">
      <q-td class="text-center">
        <q-btn v-for="grade in props.row?.grades?.filter(it=> it.evaluationValue.finalGrade === true && it.semester === semester)"
               v-if="props.row?.student?.firstName !== undefined"
               :class="`q-ma-xs ${gradeBackgroundColorMap.get(grade.evaluationValue.grade)}`"
               :label="gradeMap.get(grade.evaluationValue.grade)?.toString()"
               flat
               rounded>
          <q-popup-proxy>
            <q-banner>
              Въведен от:<span class="text-primary">{{
                grade.createdBy.firstName
              }} {{ grade.createdBy.lastName }}</span><br/>
              Дата:<span class="text-primary">{{
                grade.evaluationDate
              }}</span><br/>
            </q-banner>
          </q-popup-proxy>
        </q-btn>
        <q-btn v-else-if="!isNaN(calculateAverageGrade(props.row?.grades?.filter(it => it.evaluationValue.finalGrade === true && it.semester === semester),true))"
               :class="`q-ma-xs ${getAverageGradeColorClass(calculateAverageGrade(props.row?.grades?.filter(it => it.evaluationValue.finalGrade === true && it.semester === semester),true))}`"
               :label="calculateAverageGrade(props.row?.grades?.filter(it => it.evaluationValue.finalGrade === true && it.semester === semester),true) ? calculateAverageGrade((props.row?.grades?.filter(it => it.evaluationValue.finalGrade === true && it.semester === semester)),true) : ''"
               flat
               rounded>
          <q-tooltip>
            Средноаритметична оценка
          </q-tooltip>
        </q-btn>
      </q-td>
    </template>
  </q-table>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {
    calculateAverageGrade,
    getAverageGradeColorClass,
    gradeBackgroundColorMap,
    gradeMap,
} from "../../services/helper-services/EvaluationService";
import {Semester} from "../../model/SchoolPeriod";
import {Evaluation} from "../../model/Evaluation";
import {useQuasar} from "quasar";
import AddGradesDialog from "./add-grades-dialog.vue";
import {StudentView} from "../../model/User";
import {Subject} from "../../model/Subject";
import {saveEvaluations} from "../../services/RequestService";
import {periodId, schoolId} from "../../model/constants";
import {SchoolLesson} from "../../model/SchoolLesson";
import {commentPromiseDialog} from "../../utils";

const props = defineProps<{
  evaluations: StudentWithEvaluationDTO[],
  semester: Semester,
  subject: Subject,
  lesson?: SchoolLesson | null
}>()
const quasar = useQuasar()

const grades: StudentWithEvaluationDTO[] = $ref(props.evaluations ? [...props.evaluations] : [])
grades.push(
        {
          grades: props.evaluations?.map(it => it.grades).flat(1)?.filter((it: Evaluation) => it.semester == props.semester || props.semester == Semester.YEARLY),
          student: <StudentView><unknown>{id: 10000},
          absences: [],
          feedbacks: []
        })

const addNewGrades = async (finalGrade: boolean) => quasar.dialog({
  component: AddGradesDialog,
  componentProps: {
    evaluations: props.evaluations,
    subject: props.subject,
    semester: props.semester,
    finalGrade: finalGrade,
    lesson: props.lesson
  },
}).onOk(async (payload) => {
    const comment = await commentPromiseDialog()
    await saveEvaluations(payload.item, periodId.value, schoolId.value, comment).then(e => {
        const newlyAddedGrades = e.data
        grades.forEach(studentGrades => {
                    const newlyAddedGradesForCurrentStudent = newlyAddedGrades.find(v => v.student.id == studentGrades.student.id)?.grades
                    if (studentGrades.student.id == 10000) {
                        studentGrades.grades = studentGrades.grades.concat(newlyAddedGrades.map(it => it.grades).flat(1))
                    }
                    studentGrades.grades = studentGrades.grades.concat(newlyAddedGradesForCurrentStudent ? newlyAddedGradesForCurrentStudent : [])
                }
        )
    }
  )
})
const getRowKey = (row) => {
  return row?.student ? row?.student : '1000'
}
const columns = [
  {
    name: "numberInClass",
    label: "Номер в клас",
    align: "center",
    field: (row: StudentWithEvaluationDTO) => row?.student?.numberInClass != undefined ? `${row?.student?.numberInClass}` : '',
    sortable: true
  },
  {
    name: "student",
    label: "Име на ученика",
    align: "center",
    field: (row: StudentWithEvaluationDTO) => row?.student?.firstName != undefined ? `${row?.student?.firstName} ${row?.student?.middleName} ${row?.student?.lastName}` : 'Общо',
    sortable: true
  },
  {
    name: "grades",
    align: "center",
    label: "Оценки",
  },
  {
    name: "average",
    align: "center",
    label: "Средно аритметично"
  },
  {
    name: "finalGrade",
    align: "center",
    label: "Оформена оценка"
  }
]
const visibleColumns = [...columns].filter(it => props.semester !== Semester.YEARLY || (it.name != 'grades' && it.name != 'average')).map(it => it.name)

</script>

<style scoped>

</style>