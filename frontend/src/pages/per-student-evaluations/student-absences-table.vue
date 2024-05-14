<template>
  <q-table
          :columns="columns"
          :pagination="{rowsPerPage:20}"
          :rows="absences"
          :visible-columns="visibleColumns"
          no-data-label="Няма данни в таблицата"
          row-key="subject"
          separator="cell"
          title="Отсъствия"
  >
    <template v-slot:header-cell-total="props">
      <q-th>
        <div class="row">
          <div class="col text-center">
            Извинени
          </div>
          <q-separator vertical/>
          <div class="col-4 text-center">
            Неизвинени
          </div>
          <q-separator vertical/>
          <div class="col-4 text-center">
            Общо
          </div>
        </div>
      </q-th>
    </template>
    <template v-slot:body-cell-absences="props">
      <q-td class="text-center">
        <q-btn v-for="absence in props.row.absences.filter(it=>it.semester === semester)"
               v-if="props.row.subject !== undefined"
               :class="`q-ma-xs ${getAbsenceBackgroundColor(absence)}`"
               :label="absenceMap.get(absence.evaluationValue.absence)"
               flat
               rounded
               @click="updateEvaluationDialog(absence)">
          <q-tooltip>Кликни за повече информация</q-tooltip>
        </q-btn>
      </q-td>
    </template>
    <template v-slot:body-cell-total="props">
      <q-td>
        <div class="col row">
          <div class="col text-center">
            <q-btn :class="`q-ma-xs bg-green-3`"
                   :label="calculateAbsencesSum(props.row.absences.filter(it=>semester === Semester.YEARLY || it.semester === semester),true)"
                   flat
                   rounded>
            </q-btn>
          </div>
          <q-separator vertical/>
          <div class="col-4 text-center">
            <q-btn :class="`q-ma-xs bg-red-3`"
                   :label="calculateAbsencesSum(props.row.absences.filter(it=>semester === Semester.YEARLY || it.semester === semester),false)"
                   flat
                   rounded>
            </q-btn>
          </div>
          <q-separator vertical/>
          <div class="col-4 text-center">
            <q-btn :class="`q-ma-xs bg-blue-3`"
                   :label="calculateAbsencesSum(props.row.absences.filter(it=>semester === Semester.YEARLY || it.semester === semester))"
                   flat
                   rounded>
            </q-btn>
          </div>
        </div>
      </q-td>
    </template>
  </q-table>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {SubjectWithEvaluationDTO} from "../../model/SubjectWithEvaluationDTO";
import {
    absenceMap,
    calculateAbsencesSum,
    getAbsenceBackgroundColor,
} from "../../services/helper-services/EvaluationService";
import {Semester} from "../../model/SchoolPeriod";
import {Evaluation} from "../../model/Evaluation";
import EvaluationDialog from "../school-class/dialogs/evaluation-delete-update-dialog.vue";
import {periodId, schoolId} from "../../model/constants";
import {useQuasar} from "quasar";

const props = defineProps<{
  evaluations: SubjectWithEvaluationDTO[],
  semester: Semester
}>()
const absences = $ref([...props.evaluations,
  {
    absences: props.evaluations.map(it => it.absences).flat(1).filter((it: Evaluation) => it.semester == props.semester || props.semester == Semester.YEARLY),
    subject: undefined,
    feedbacks: [],
    grades: []
  }])

const columns = [
  {
    name: "subject",
    label: "Предмет",
    align: "center",
    field: (row: SubjectWithEvaluationDTO) => row.subject?.name ? row.subject?.name : 'Общо',
    sortable: true
  },
  {
    name: "absences",
    align: "center",
    label: "Отсъствия",
  },
  {
    name: "total",
    align: "center",
    label: "Общо"
  }
]
const visibleColumns = [...columns].filter(it => props.semester !== Semester.YEARLY || it.name != 'absences').map(it => it.name)

const quasar = useQuasar()
const updateEvaluationDialog = (evaluation: Evaluation) => {
  quasar.dialog({
    component: EvaluationDialog,
    componentProps: {
      evaluation: evaluation,
      periodId: periodId.value,
      schoolId: schoolId.value,
      readonly: true
    },
  })
}
</script>

<style scoped>

</style>