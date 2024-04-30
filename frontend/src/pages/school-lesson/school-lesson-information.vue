<template>
  {{ lesson }}
</template>

<script lang="ts" setup>
import {SchoolLesson} from "../../model/SchoolLesson";

const props = defineProps<{
  lesson: SchoolLesson
}>()
</script>

<style scoped>

</style>