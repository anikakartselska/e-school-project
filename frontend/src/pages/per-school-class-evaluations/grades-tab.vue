<template>
  <q-table
          title="Оценки"
          :rows="grades"
          :columns="columns"
          row-key="student"
          no-data-label="Няма данни в таблицата"
          :pagination="{rowsPerPage:20}"
  >
    <template v-slot:body-cell-grades="props">
      <q-td>
        <q-btn v-for="grade in props.row.grades"
               flat rounded
               :class="`q-ma-xs ${gradeBackgroundColorMap.get(grade.evaluationValue.grade)}`"
               :label="gradeMap.get(grade.evaluationValue.grade)">
          <q-popup-proxy>
            <q-banner>
              Въведен от:<span class="text-primary">{{
                currentSubject.teacher.firstName
              }} {{ currentSubject.teacher.lastName }}</span><br/>
              Дата:<span class="text-primary">{{
                grade.evaluationDate
              }}</span><br/>
            </q-banner>
          </q-popup-proxy>
        </q-btn>
      </q-td>

    </template>
    <template v-slot:body-cell-average="props">
      <q-td>
        <div>
          {{ calculateAverageGrade(props.row.grades) }}
        </div>
      </q-td>
    </template>
  </q-table>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {
    calculateAverageGrade,
    gradeBackgroundColorMap,
    gradeMap
} from "../../services/helper-services/EvaluationService";
import {Subject} from "../../model/Subject";
import {watch} from "vue";

const props = defineProps<{
  subject: Subject
  evaluations: StudentWithEvaluationDTO[]
}>()
let grades = $ref(props.evaluations)
let currentSubject = $ref(props.subject)

watch(props, () => {
  grades = props.evaluations
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
    name: "grades",
    align: "left",
    label: "Оценки"
  },
  {
    name: "average",
    align: "left",
    label: "Средно аритметично"
  }
]
</script>

<style scoped>

</style>