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
            <q-btn v-for="currentAbsence in props.row.absences"
                   :class="`q-ma-xs ${getAbsenceBackgroundColor(currentAbsence)}`"
                   :label="absenceMap.get(currentAbsence.evaluationValue.absence)?.toString()"
                   flat
                   rounded
                   type="a"
                   @click="excuseAbsence(currentAbsence,props.row.student)">
              <q-tooltip v-if="!currentAbsence.evaluationValue.excused">
                Кликни, за да извиниш отсъствие
              </q-tooltip>
              <q-tooltip v-else>
                Кликни, върнеш отсъствието неизвинено
              </q-tooltip>
            </q-btn>
          </q-td>
        </template>
      </q-table>
      <q-card-actions align="right">
        <q-btn color="primary" label="Извини маркираните отсъствия" @click="submit"/>
        <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent, useQuasar} from "quasar";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {$ref} from "vue/macros";
import {absenceMap, getAbsenceBackgroundColor} from "../../services/helper-services/EvaluationService";
import {AbsenceValue, Evaluation} from "../../model/Evaluation";
import {StudentView} from "../../model/User";
import {Subject} from "../../model/Subject";
import {Semester} from "../../model/SchoolPeriod";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  subject: Subject,
  semester: Semester,
  evaluations: StudentWithEvaluationDTO[]
}>()
const studentEvaluations = $ref([...props.evaluations].map(it => {
  return {
    ...it,
    grades: [],
    feedbacks: [],
    absences: it.absences.filter(absence => !(<AbsenceValue>absence.evaluationValue).excused)
  }
}))

const submit = () => {
  onDialogOK({
    item: studentEvaluations
  })
}

const excuseAbsence = (absence: Evaluation, student: StudentView) => {
  studentEvaluations.map(it => {
            if (it.student.id == student.id) {
              it.absences = it.absences.map(currentAbsence =>
                      currentAbsence.id == absence.id ?
                              {
                                ...currentAbsence,
                                evaluationValue: {
                                  ...currentAbsence.evaluationValue,
                                  excused: !(<AbsenceValue>currentAbsence.evaluationValue).excused
                                }
                              } : currentAbsence
              )
            } else {
              it
            }
          }
  )
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