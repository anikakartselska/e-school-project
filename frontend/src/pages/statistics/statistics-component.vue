<template>
  {{ statistics }}
  <div class="row">
    <div class="col-1"/>
    <q-card
            class="my-card text-white col-3 q-ml-md q-mr-md"
            style="background: radial-gradient(circle, #35a2ff 0%, #014a88 100%)"
    >
      <q-card-section>
        <div class="text-h6">Our Changing Planet</div>
        <div class="text-subtitle2">by John Doe</div>
      </q-card-section>
    </q-card>
    <q-card
            class="my-card text-white col-3 q-ml-md q-mr-md"
            style="background: radial-gradient(circle, #7835ff 0%, #370188 100%)"
    >
      <q-card-section>
        <div class="text-h6">Our Changing Planet</div>
        <div class="text-subtitle2">by John Doe</div>
      </q-card-section>
    </q-card>
    <q-card
            class="my-card text-white col-3 q-ml-md q-mr-md"
            style="background: radial-gradient(circle, #ff353c 0%, #880105 100%)"
    >
      <q-card-section>
        <div class="text-h6">Our Changing Planet</div>
        <div class="text-subtitle2">by John Doe</div>
      </q-card-section>
    </q-card>
  </div>
  <div class="row q-mt-xl">
    <div class="col-1"/>
    <q-card
            class="my-card text-white col-3 q-ml-md q-mr-md"
            style="background: radial-gradient(circle, #35ff75 0%, #1e8801 100%)"
    >
      <q-card-section>
        <div class="text-h6">Our Changing Planet</div>
        <div class="text-subtitle2">by John Doe</div>
      </q-card-section>
    </q-card>
    <q-card
            class="my-card text-white col-3 q-ml-md q-mr-md"
            style="background: radial-gradient(circle, #ff8235 0%, #884901 100%)"
    >
      <q-card-section>
        <div class="text-h6">Our Changing Planet</div>
        <div class="text-subtitle2">by John Doe</div>
      </q-card-section>
    </q-card>
    <q-card
            class="my-card text-white col-3 q-ml-md q-mr-md"
            style="height: 20vh;
            background: radial-gradient(circle, #ffe435 0%, #7b8801 100%)"
    >
      <q-card-section>
        <div class="text-h6">Our Changing Planet</div>
        <div class="text-subtitle2">by John Doe</div>
      </q-card-section>
    </q-card>
  </div>
</template>

<script lang="ts" setup>
import {StudentStatistics} from "../../model/StudentStatistics";

const props = defineProps<{
  studentId: number
  periodId: number
  schoolId: number
}>()

const statistics = <StudentStatistics>{
  success: 1.66,
  grades: 2,
  absences: 3,
  feedback: 4,
  examinations: 5,
  events: 6,
  homeworks: 7,
  placeInClass: 8,
  placeInGraduationClass: 9,
  placeInSchool: 10,
}
</script>

<style scoped>

</style>