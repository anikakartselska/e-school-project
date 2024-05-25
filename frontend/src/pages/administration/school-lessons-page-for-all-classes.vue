<template>
  <div class="bg-sms row">
    <div class="col-1"></div>
    <div class="col-10">
      <q-page class="page-content" padding>
        <div class="text-h4">
          Седмичен разпис
        </div>
        <q-card class="q-pa-lg">
          <school-lessons-component :period-id="props.periodId" :school-id="props.schoolId"/>
        </q-card>
      </q-page>
    </div>
  </div>
</template>
<script lang="ts" setup>

import SchoolLessonsComponent from "./components/school-lessons-component.vue";

const props = defineProps<{
  periodId: number,
  schoolId: number
}>()


</script>
