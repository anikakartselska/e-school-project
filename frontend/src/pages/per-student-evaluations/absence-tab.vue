<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-expansion-item v-model="expansionItem[Semester.FIRST]"
                        header-class="text-primary"
                        icon="book"
                        label="Първи срок">
        <student-absences-table :evaluations="evaluations" :semester="Semester.FIRST"/>
      </q-expansion-item>
      <q-expansion-item v-model="expansionItem[Semester.SECOND]"
                        header-class="text-primary"
                        icon="book"
                        label="Втори срок">
        <student-absences-table :evaluations="evaluations" :semester="Semester.SECOND"/>
      </q-expansion-item>
      <q-expansion-item v-model="expansionItem[Semester.YEARLY]"
                        header-class="text-primary"
                        icon="book"
                        label="Общ брой отсъствия за учебната година">
        <student-absences-table :evaluations="evaluations" :semester="Semester.YEARLY"/>
      </q-expansion-item>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {SubjectWithEvaluationDTO} from "../../model/SubjectWithEvaluationDTO";
import StudentAbsencesTable from "./student-absences-table.vue";
import {Semester} from "../../model/SchoolPeriod";

const props = defineProps<{
  evaluations: SubjectWithEvaluationDTO[],
}>()
const expansionItem = $ref({"FIRST": true, "SECOND": false, "YEARLY": false})

</script>

<style scoped>

</style>