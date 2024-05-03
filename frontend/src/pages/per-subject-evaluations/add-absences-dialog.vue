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
               title="Отсъствия"
      >
        <template v-slot:body-cell-absences="props">
          <q-td class="text-center">
            <q-btn v-for="currentAbsence in absencesOptions"
                   :class="`q-ma-xs ${getAbsenceValueBackgroundColor(currentAbsence)}`"
                   :label="absenceMap.get(currentAbsence.absence)?.toString()"
                   flat
                   rounded
                   type="a"
                   @click="addAbsence(currentAbsence,props.row.student)">
              <q-tooltip>
                Кликни, за да добавиш отсъствие
              </q-tooltip>
            </q-btn>
          </q-td>
        </template>
        <template v-slot:body-cell-added-absences="props">
          <q-td class="text-center">
            <q-btn v-for="absence in props.row.absences.filter(it => it.semester === semester)"
                   v-if="props.row.student !== undefined"
                   :class="`q-ma-xs ${getAbsenceBackgroundColor(absence)}`"
                   :label="absenceMap.get(absence.evaluationValue.absence)?.toString()"
                   flat
                   rounded>
              <q-badge v-if="absence.id == null" align="bottom" color="white" floating
                       style="width:2px; height: 2px ">
                <template v-slot:default>
                  <div class="row">
                    <q-btn class="bg-negative absolute-left" icon="close" round size="5px"
                           @click="removeAddedAbsence(absence)"/>
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
import {date, useDialogPluginComponent, useQuasar} from "quasar";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {$ref} from "vue/macros";
import {
    absenceMap,
    absencesOptions,
    getAbsenceBackgroundColor,
    getAbsenceValueBackgroundColor
} from "../../services/helper-services/EvaluationService";
import {AbsenceValue, Evaluation, EvaluationType} from "../../model/Evaluation";
import {StudentView} from "../../model/User";
import {getCurrentUserAsUserView} from "../../services/LocalStorageService";
import {Subject} from "../../model/Subject";
import {Semester} from "../../model/SchoolPeriod";
import {SchoolLesson} from "../../model/SchoolLesson";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  subject: Subject,
  semester: Semester,
  evaluations: StudentWithEvaluationDTO[],
  lesson: SchoolLesson | null
}>()
const currentUser = getCurrentUserAsUserView()
const studentEvaluations = $ref([...props.evaluations].map(it => {
  return {
    ...it,
    grades: [],
    feedbacks: [],
    absences: []
  }
}))

const submit = () => {
  onDialogOK({
    item: studentEvaluations
  })
}

const addAbsence = (absenceValue: AbsenceValue, student: StudentView) => {
  const evaluation = <Evaluation>{
    id: null,
    subject: props.subject,
    student: student,
    schoolLessonId: props.lesson?.id,
    evaluationType: EvaluationType.ABSENCE,
    evaluationValue: absenceValue,
    semester: props.semester,
    createdBy: currentUser
  }
  studentEvaluations.map(it => {
            if (it.student.id == student.id) {
              (<Evaluation[]>it.absences).push(evaluation)
            } else {
              it
            }
          }
  )
}
const removeAddedAbsence = (absence: Evaluation) => {
  studentEvaluations.map(it => {
    if (it.student.id == absence.student.id) {
      (<Evaluation[]>it.absences).splice(it.absences.indexOf(absence), 1);
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
    name: "absences",
    align: "center",
    label: "Отсъствия",
  },
  {
    name: "added-absences",
    align: "center",
    label: "Добавени отсъствия",
  },
]
</script>

<style scoped>
.dialog-header {
  display: flex;
  justify-content: space-between;
}
</style>