<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-expansion-item v-model="expansionItem[Semester.FIRST]"
                        header-class="text-primary"
                        icon="book"
                        label="Първи срок">
        <student-grades-table :evaluations="evaluations" :lesson="lesson" :semester="Semester.FIRST"
                              :subject="subject"/>
      </q-expansion-item>
      <q-expansion-item v-model="expansionItem[Semester.SECOND]"
                        header-class="text-primary"
                        icon="book"
                        label="Втори срок">
        <student-grades-table :evaluations="evaluations" :lesson="lesson" :semester="Semester.SECOND"
                              :subject="subject"/>
      </q-expansion-item>
      <q-expansion-item v-model="expansionItem[Semester.YEARLY]"
                        header-class="text-primary"
                        icon="book"
                        label="Годишни оценки">
        <student-grades-table :evaluations="evaluations" :lesson="lesson" :semester="Semester.YEARLY"
                              :subject="subject"/>
      </q-expansion-item>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {Semester} from "../../model/SchoolPeriod";
import StudentGradesTable from "./subject-grades-table.vue";
import {$ref} from "vue/macros";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {Subject} from "../../model/Subject";
import {SchoolLesson} from "../../model/SchoolLesson";

const props = defineProps<{
  evaluations: StudentWithEvaluationDTO[],
  subject: Subject,
  lesson?: SchoolLesson | null
}>()
const expansionItem = $ref({"FIRST": true, "SECOND": false, "YEARLY": false})
</script>

<style scoped>

</style>