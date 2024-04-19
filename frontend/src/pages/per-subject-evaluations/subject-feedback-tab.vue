<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-expansion-item v-model="expansionItem[Semester.FIRST]"
                        header-class="text-primary"
                        icon="book"
                        label="Първи срок">
        <student-feedbacks-table :evaluations="evaluations" :semester="Semester.FIRST" :subject="subject"/>
      </q-expansion-item>
      <q-expansion-item v-model="expansionItem[Semester.SECOND]"
                        header-class="text-primary"
                        icon="book"
                        label="Втори срок">
        <student-feedbacks-table :evaluations="evaluations" :semester="Semester.SECOND" :subject="subject"/>
      </q-expansion-item>
      <q-expansion-item v-model="expansionItem[Semester.YEARLY]"
                        header-class="text-primary"
                        icon="book"
                        label="Общ брой забележки за учебната година">
        <student-feedbacks-table :evaluations="evaluations" :semester="Semester.YEARLY" :subject="subject"/>
      </q-expansion-item>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {Semester} from "../../model/SchoolPeriod";
import StudentFeedbacksTable from "./subject-feedbacks-table.vue";
import {$ref} from "vue/macros";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {Subject} from "../../model/Subject";

const props = defineProps<{
  evaluations: StudentWithEvaluationDTO[],
  subject: Subject
}>()
const expansionItem = $ref({"FIRST": false, "SECOND": false, "YEARLY": false})
</script>

<style scoped>

</style>