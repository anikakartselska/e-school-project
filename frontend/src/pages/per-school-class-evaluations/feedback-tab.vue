<template>
  <q-table
          title="Отзиви"
          :rows="feedbacks"
          :columns="columns"
          row-key="subject"
          no-data-label="Няма данни в таблицата"
  >
    <template v-slot:body-cell-feedbacks="props">
      <q-td>
        <q-btn v-for="feedback in props.row.feedbacks"
               flat rounded
               :icon="feedbacksMap.get(feedback.evaluationValue.feedback)===true? 'thumb_up_alt' : 'thumb_down_alt'"
               :class="feedbacksMap.get(feedback.evaluationValue.feedback)===true? `text-green-14` : `text-red-14`"
        >
          <q-popup-proxy>
            <q-banner>
              Въведен от:<span class="text-primary">{{
                currentSubject.teacher.firstName
              }} {{ currentSubject.teacher.lastName }}</span><br/>
              Дата:<span class="text-primary">{{
                feedback.evaluationDate
              }}</span><br/>
            </q-banner>
          </q-popup-proxy>
        </q-btn>
      </q-td>
    </template>
  </q-table>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {feedbacksMap} from "../../services/helper-services/EvaluationService";
import {Subject} from "../../model/Subject";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {watch} from "vue";

const props = defineProps<{
  subject: Subject
  evaluations: StudentWithEvaluationDTO[]
}>()
let feedbacks = $ref(props.evaluations)
let currentSubject = $ref(props.subject)

watch(props, () => {
  feedbacks = props.evaluations
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
    name: "feedbacks",
    align: "left",
    label: "Отзиви"
  },
  {
    name: "total",
    align: "left",
    label: "Общо"
  }
]
</script>

<style scoped>

</style>