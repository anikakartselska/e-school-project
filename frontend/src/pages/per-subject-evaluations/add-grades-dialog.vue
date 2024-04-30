<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card style="min-width: 800px">
      <q-btn v-close-popup
             class="float-right text-black"
             dense
             flat
             icon="close"
             round/>
      <q-table :columns="columns"
               :pagination="{rowsPerPage:20}"
               :rows="studentEvaluations"
               no-data-label="Няма данни в таблицата"
               row-key="student"
               separator="cell"
               style="width: 800px"
               title="Оценки"
      >
        <template v-slot:body-cell-grades="props">
          <q-td class="text-center">
            <q-btn v-for="currentGrade in Object.keys(Grade)"
                   :class="`q-ma-xs ${gradeBackgroundColorMap.get(currentGrade)}`"
                   :label="gradeMap.get(currentGrade)?.toString()"
                   :disable="finalGrade===true && props.row.grades.filter(it => (it.evaluationValue.finalGrade === true)).length > 0"
                   flat
                   type="a"
                   rounded
                   @click="addGrade(currentGrade,props.row.student)">
              <q-tooltip
                      v-if="finalGrade===true && props.row.grades.filter(it => (it.evaluationValue.finalGrade === true)).length > 0">
                Успехът за текущия ученик е вече оформен
              </q-tooltip>
              <q-tooltip v-else>
                Кликни, за да добавиш оценка
              </q-tooltip>
            </q-btn>
          </q-td>
        </template>
        <template v-slot:body-cell-added-grades="props">
          <q-td class="text-center">
            <q-btn v-for="grade in props.row.grades.filter(it => (it.evaluationValue.finalGrade === finalGrade) && it.semester === semester)"
                   v-if="props.row.student !== undefined"
                   :class="`q-ma-xs ${gradeBackgroundColorMap.get(grade.evaluationValue.grade)}`"
                   :label="gradeMap.get(grade.evaluationValue.grade)?.toString()"
                   flat
                   rounded>
              <q-badge v-if="grade.id == null" align="bottom" color="white" floating
                       style="width:2px; height: 2px ">
                <template v-slot:default>
                  <div class="row">
                    <q-btn class="bg-negative absolute-left" icon="close" round size="5px"
                           @click="removeAddedGrade(grade)"/>
                  </div>
                </template>
              </q-badge>
            </q-btn>
          </q-td>
        </template>
      </q-table>
      <q-card-actions align="right">
        <q-btn color="primary" label="Добави" @click="submit"/>
        <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent, useQuasar} from "quasar";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {$ref} from "vue/macros";
import {gradeBackgroundColorMap, gradeMap} from "../../services/helper-services/EvaluationService";
import {Evaluation, EvaluationType, Grade, GradeValue} from "../../model/Evaluation";
import {StudentView} from "../../model/User";
import {getCurrentUserAsUserView} from "../../services/LocalStorageService";
import {Subject} from "../../model/Subject";
import {Semester} from "../../model/SchoolPeriod";
import {formatToDate} from "../../utils";
import {SchoolLesson} from "../../model/SchoolLesson";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  subject: Subject,
  semester: Semester,
  evaluations: StudentWithEvaluationDTO[],
  finalGrade: boolean,
  lesson?: SchoolLesson | null
}>()
const currentUser = getCurrentUserAsUserView()
const studentEvaluations = $ref([...props.evaluations].map(it => {
  return {
    ...it,
    grades: props.finalGrade == true ? it.grades.filter(it => (<GradeValue>it.evaluationValue).finalGrade == true) : [],
    feedbacks: [],
    absences: []
  }
}))

const submit = () => {
  onDialogOK({
    item: studentEvaluations
  })
}

const addGrade = (grade: Grade, student: StudentView) => {
  const evaluation = <Evaluation>{
    id: null,
    subject: props.subject,
    student: student,
    schoolLessonId: props.lesson?.id,
    evaluationDate: formatToDate(new Date()),
    evaluationType: EvaluationType.GRADE,
    evaluationValue: <GradeValue>{grade: grade, finalGrade: props.finalGrade},
    semester: props.semester,
    createdBy: currentUser
  }
  studentEvaluations.map(it => {
            if (it.student.id == student.id) {
              if (props.finalGrade == true) {
                it.grades = [evaluation]
              } else {
                (<Evaluation[]>it.grades).push(evaluation)
              }
            } else {
              it
            }
          }
  )
}
const removeAddedGrade = (grade: Evaluation) => {
  studentEvaluations.map(it => {
    if (it.student.id == grade.student.id) {
      (<Evaluation[]>it.grades).splice(it.grades.indexOf(grade), 1);
    } else {
      it
    }
  })
}

const columns = [
  {
    name: "numberInClass",
    label: "Номер в клас",
    align: "center",
    field: (row: StudentWithEvaluationDTO) => `${row.student?.numberInClass}`,
    sortable: true
  },
  {
    name: "student",
    label: "Име на ученика",
    align: "center",
    field: (row: StudentWithEvaluationDTO) => row.student ? `${row.student?.firstName} ${row.student?.middleName} ${row.student?.lastName}` : 'Общо',
    sortable: true
  },
  {
    name: "grades",
    align: "center",
    label: "Оценки",
  },
  {
    name: "added-grades",
    align: "center",
    label: props.finalGrade ? "Оформена оценка" : "Добавени оценки",
  },
]
</script>

<style scoped>
.dialog-header {
  display: flex;
  justify-content: space-between;
}
</style>