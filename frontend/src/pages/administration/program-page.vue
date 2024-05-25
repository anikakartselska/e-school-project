<template>
  <div class="bg-sms row">
    <div class="col-1"></div>
    <div class="col-10">
      <q-page class="page-content" padding>
          <div class="text-h4">
              Учебна програма
          </div>
          <school-program-component :period-id="props.periodId" :school-id="props.schoolId"/>
      </q-page>
    </div>
  </div>
</template>
<script lang="ts" setup>

import SchoolProgramComponent from "./components/school-program-component.vue";

const props = defineProps<{
    periodId: number,
    schoolId: number
}>()


</script>

