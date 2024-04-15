<template>
  <q-table
          title="Отсъствия"
          :rows="absences"
          :columns="columns"
          :visible-columns="visibleColumns"
          row-key="subject"
          no-data-label="Няма данни в таблицата"
          :pagination="{rowsPerPage:20}"
  >
    <template v-slot:top-right>
      <q-btn v-if="!addingMode" icon="add" @click="addingMode = !addingMode"/>
    </template>
    <template v-slot:body-cell-absences="props">
      <q-td>
        <q-btn v-for="absence in props.row.absences"
               flat rounded
               :class="`q-ma-xs ${absenceBackgroundColorMap.get(absence.evaluationValue.absence)}`"
               :label="absenceMap.get(absence.evaluationValue.absence)">
          <q-popup-proxy>
            <q-banner>
              Въведен от:<span class="text-primary">{{
                currentSubject.teacher.firstName
              }} {{ currentSubject.teacher.lastName }}</span><br/>
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
        <div>
          {{ calculateAbsencesSum(props.row.absences) }}
        </div>
      </q-td>
    </template>
    <template v-slot:body-cell-addingValues="props">
      <q-td>
        <q-btn-group push>
          <q-btn v-for="absence in absenceOptions" :label="absence"/>
        </q-btn-group>
      </q-td>
    </template>
  </q-table>
</template>

<script lang="ts" setup>
import {$computed, $ref} from "vue/macros";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {
    absenceBackgroundColorMap,
    absenceMap,
    calculateAbsencesSum
} from "../../services/helper-services/EvaluationService";
import {Subject} from "../../model/Subject";
import {watch} from "vue";
import {Absence} from "../../model/Evaluation";

const props = defineProps<{
  subject: Subject
  evaluations: StudentWithEvaluationDTO[]
}>()
let absences = $ref(props.evaluations)
let currentSubject = $ref(props.subject)
const addingMode = $ref(false)
const absenceOptions = Object.keys(Absence)
const numberInClassToNewAbsences = new Map<string, Absence[]>(new Map())
const testList = $ref([])
props.evaluations.forEach(evaluation =>
        numberInClassToNewAbsences.set(evaluation.student.numberInClass, [])
)
watch(props, () => {
  absences = props.evaluations
  currentSubject = props.subject
})

const columns = [
  {name: 'edit'},
  {
    name: "numberInClass",
    label: "Номер в клас",
    align: "left",
    field: (row: StudentWithEvaluationDTO) => row.student.numberInClass,
    sortable: true
  },
  {
    name: "student",
    label: "Ученик",
    align: "left",
    field: (row: StudentWithEvaluationDTO) => `${row.student.firstName} ${row.student.lastName}`,
    sortable: true
  },
  {
    name: "absences",
    align: "left",
    label: "Отсъствия"
  },
  {
    name: "total",
    align: "left",
    label: "Общо"
  },
  {
    name: "addingValues"
  }
]
const visibleColumns = $computed(() => {
  if (!addingMode) {
    return ["numberInClass", "student", "absences", "total"]
  } else {
    return ["numberInClass", "student", "addingValues"]
  }
})
</script>

<style scoped>

</style>