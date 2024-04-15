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
    <template v-slot:body-cell-absences="props">
      <q-td class="text-center">
        <q-btn v-for="absence in props.row.absences.filter(it=>it.semester === semester)"
               v-if="props.row.subject !== undefined"
               :class="`q-ma-xs ${getAbsenceBackgroundColor(absence)}`"
               :label="absenceMap.get(absence.evaluationValue.absence)"
               flat
               rounded>
          <q-popup-proxy>
            <q-banner>
              Въведен от:<span class="text-primary">{{
                absence.createdBy.firstName
              }} {{ absence.createdBy.lastName }}</span><br/>
              Дата:<span class="text-primary">{{
                absence.evaluationDate
              }}</span><br/>
            </q-banner>
          </q-popup-proxy>
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
    field: (row: SubjectWithEvaluationDTO) => row.subject?.name,
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

</script>

<style scoped>

</style>