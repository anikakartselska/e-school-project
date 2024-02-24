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
                props.row.subject.teacher.firstName
              }} {{ props.row.subject.teacher.lastName }}</span><br/>
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
import {SubjectWithEvaluationDTO} from "../../model/SubjectWithEvaluationDTO";
import {feedbacksMap} from "../../services/helper-services/EvaluationService";

const props = defineProps<{
  evaluations: SubjectWithEvaluationDTO[]
}>()
const feedbacks = $ref(props.evaluations)
const columns = [
  {name: 'edit'},
  {
    name: "subject",
    label: "Предмет",
    align: "left",
    field: (row: SubjectWithEvaluationDTO) => row.subject?.name,
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