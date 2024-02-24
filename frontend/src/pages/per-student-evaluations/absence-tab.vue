<template>
  <q-table
          title="Отсъствия"
          :rows="absences"
          :columns="columns"
          :visible-columns="visibleColumns"
          row-key="subject"
          no-data-label="Няма данни в таблицата"
  >
    <template v-slot:body-cell-absences="props">
      <q-td>
        <q-btn v-for="absence in props.row.absences"
               flat rounded
               :class="`q-ma-xs ${absenceBackgroundColorMap.get(absence.evaluationValue.absence)}`"
               :label="absenceMap.get(absence.evaluationValue.absence)">
          <q-popup-proxy>
            <q-banner>
              Въведен от:<span class="text-primary">{{
                props.row.subject.teacher.firstName
              }} {{ props.row.subject.teacher.lastName }}</span><br/>
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
  </q-table>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {SubjectWithEvaluationDTO} from "../../model/SubjectWithEvaluationDTO";
import {
  absenceBackgroundColorMap,
  absenceMap,
  calculateAbsencesSum
} from "../../services/helper-services/EvaluationService";

const props = defineProps<{
  evaluations: SubjectWithEvaluationDTO[]
}>()
const absences = $ref(props.evaluations)
const columns = [
  {name: 'edit'},
  {
    name: "subject",
    label: "Предмет",
    align: "left",
    field: (row: SubjectWithEvaluationDTO) => row.subject.name,
  },
  {
    name: "absences",
    align: "left",
    label: "Отсъствия",
  },
  {
    name: "total",
    align: "left",
    label: "Общо",
  }
]
</script>

<style scoped>

</style>