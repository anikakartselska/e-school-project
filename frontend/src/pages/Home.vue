<template>
  <div v-if="currentUserHasAnyRole([SchoolRole.ADMIN,SchoolRole.TEACHER])">
    <school-statistics-page :period-id="props.periodId" :school-id="props.schoolId"/>
  </div>
  <div v-else>
    <student-statistics-page :period-id="props.periodId" :school-id="props.schoolId"/>
  </div>
</template>

<script lang="ts" setup>

import {currentUserHasAnyRole} from "../services/LocalStorageService";
import {SchoolRole} from "../model/User";
import SchoolStatisticsPage from "./statistics/school-statistics-page.vue";
import StudentStatisticsPage from "./statistics/student-statistics-page.vue";

const props = defineProps<{
  periodId: number,
  schoolId: number
}>()


</script>

<style scoped>

</style>