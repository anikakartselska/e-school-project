<template>
  <q-card>
    <q-expansion-item v-model="expansionItem[Semester.FIRST]"
                      header-class="text-primary"
                      icon="book"
                      label="Първи срок">
      <school-program-component-per-semester :period-id="periodId" :school-id="schoolId" :semester="Semester.FIRST"/>
    </q-expansion-item>
    <q-expansion-item v-model="expansionItem[Semester.SECOND]"
                      header-class="text-primary"
                      icon="book"
                      label="Втори срок">
      <school-program-component-per-semester :period-id="periodId" :school-id="schoolId" :semester="Semester.SECOND"/>
    </q-expansion-item>
  </q-card>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {Semester} from "../../../model/SchoolPeriod";
import SchoolProgramComponentPerSemester from "./school-program-component-per-semester.vue";

const props = defineProps<{
  periodId: number,
  schoolId: number
}>()
const expansionItem = $ref({"FIRST": true, "SECOND": false, "YEARLY": false})
</script>

<style scoped>

</style>