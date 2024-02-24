<template>
  <q-table
          title="Оценки"
          :rows="grades"
          :columns="columns"
          row-key="subject"
          no-data-label="Няма данни в таблицата"
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
                    props.row.subject.teacher.firstName
                    }} {{ props.row.subject.teacher.lastName }}</span><br/>
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
import {SubjectWithEvaluationDTO} from "../../model/SubjectWithEvaluationDTO";
import {calculateAverageGrade, gradeBackgroundColorMap, gradeMap} from "../../services/helper-services/EvaluationService";

const props = defineProps<{
  evaluations: SubjectWithEvaluationDTO[]
}>()
const expanded = $ref(null)
const grades = $ref(props.evaluations)
const columns = [
  {name: 'edit'},
  {
    name: "subject",
    label: "Предмет",
    align: "left",
    field: (row: SubjectWithEvaluationDTO) => row.subject.name,
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