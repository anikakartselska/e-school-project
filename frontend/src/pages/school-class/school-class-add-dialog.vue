<template>

</template>

<script>
import {$ref} from "vue/macros";
import {SchoolClass} from "../../model/SchoolClass";
import {UserView} from "../../model/User";

const props = defineProps<{
  schoolClass: SchoolClass,
  alreadyExistingSchoolClasses: SchoolClass[]
  teacherOptions: UserView[]
}>()

const schoolClass = $ref(props.schoolClass)
</script>

<style scoped>

</style>