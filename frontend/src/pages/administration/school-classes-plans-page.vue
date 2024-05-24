<template>
  <q-page id="test" class="q-pa-sm bg-sms">
    <school-classes-plans-component :period-id="periodId" :school-id="schoolId"/>
  </q-page>
</template>

<script lang="ts" setup>
import SchoolClassesPlansComponent from "./components/school-classes-plans-component.vue";

const props = defineProps<{
    schoolId: number,
    periodId: number
}>()


</script>

<style scoped>

</style>