<template>
  <h1> HOME </h1>
  <div>
    <div>{{ number }}</div>
    <q-btn icon="add" @click="myFunction()"></q-btn>
  </div>

</template>

<script lang="ts" setup>

import {$ref} from "vue/macros";
import {periodId, schoolId} from "../model/constants";

const props = defineProps<{
  periodId: number,
  schoolId: number
}>()

let number = $ref(1)
schoolId.value = props.schoolId.toString()
periodId.value = props.periodId.toString()

console.log(props)
console.log(schoolId.value)
console.log(periodId.value)

const myFunction = () => number++;
</script>

<style scoped>

</style>