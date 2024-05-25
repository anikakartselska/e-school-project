<template>
    <div v-if="!selectedSchoolClass">
        <div class="q-pl-md text-negative">Изберете клас, за да видите семичните разписи</div>
        <q-select v-model="selectedSchoolClass"
                  :option-label="option => option.name"
                  :options="schoolClasses"
                  class="q-pl-md"
                  dense
                  label="Клас"
                  reactive-rules
                  style="width: 250px"/>

    </div>
    <div v-if="selectedSchoolClass">
        <school-class-lessons-tab :period-id="periodId" :school-class="selectedSchoolClass" :school-id="schoolId">
            <template v-slot:selectclass>
                <q-select v-model="selectedSchoolClass"
                          :option-label="option => option.name"
                          :options="schoolClasses"
                          class="q-pl-md"
                          dense
                          label="Клас"
                          reactive-rules
                          style="width: 250px"/>
            </template>

        </school-class-lessons-tab>
    </div>
</template>
<script lang="ts" setup>
import {getSchoolClassesFromSchool} from "../../../services/RequestService";
import {SchoolClass} from "../../../model/SchoolClass";
import {$ref} from "vue/macros";
import SchoolClassLessonsTab from "../../school-class/school-class-lessons-tab.vue";

const props = defineProps<{
    periodId: number,
    schoolId: number
}>()
const schoolClasses = await getSchoolClassesFromSchool(props.schoolId, props.periodId)
let selectedSchoolClass = $ref<SchoolClass | null>(null)

</script>
