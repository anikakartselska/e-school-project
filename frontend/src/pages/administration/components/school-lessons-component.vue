<template>
    <q-tabs v-model="selectedTab" dense>
        <q-tab :label="`За класове`"
               name="FOR_CLASS"/>
        <q-tab label="За учители" name="FOR_TEACHER"/>
    </q-tabs>
    <div v-if="selectedTab === 'FOR_CLASS'">
        <div v-if="!selectedSchoolClass">
            <div class="q-pl-md text-negative">Изберете клас, за да видите седмичните разписи</div>
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
    </div>
    <div v-else-if="selectedTab === 'FOR_TEACHER'">
        <div v-if="!selectedTeacher">
            <div class="q-pl-md text-negative">Изберете учител, за да видите седмичните разписи</div>
            <q-select v-model="selectedTeacher"
                      :option-label="option => `${option.firstName} ${option.lastName}`"
                      :options="teachers"
                      class="q-pl-md"
                      dense
                      label="Учител"
                      reactive-rules
                      style="width: 250px"/>

        </div>
        <div v-else>
            <teacher-school-lesson-component :period-id="periodId" :school-id="schoolId" :show-title="false"
                                             :teacher-id="selectedTeacher.id">
                <template v-slot:selectteacher>
                    <q-select v-model="selectedTeacher"
                              :option-label="option => `${option.firstName} ${option.lastName}`"
                              :options="teachers"
                              class="q-pl-md"
                              dense
                              label="Учител"
                              reactive-rules
                              style="width: 250px"/>
                </template>
            </teacher-school-lesson-component>
        </div>
    </div>
</template>
<script lang="ts" setup>
import {fetchAllTeachers, getSchoolClassesFromSchool} from "../../../services/RequestService";
import {SchoolClass} from "../../../model/SchoolClass";
import {$ref} from "vue/macros";
import SchoolClassLessonsTab from "../../school-class/school-class-lessons-tab.vue";
import {TeacherView} from "../../../model/User";
import TeacherSchoolLessonComponent from "../../teacher-pages/teacher-school-lesson-component.vue";

const props = defineProps<{
    periodId: number,
    schoolId: number
}>()
const schoolClasses = await getSchoolClassesFromSchool(props.schoolId, props.periodId)
const teachers = await fetchAllTeachers(props.schoolId, props.periodId)
let selectedSchoolClass = $ref<SchoolClass | null>(null)
let selectedTeacher = $ref<TeacherView | null>(null)
let selectedTab = $ref("FOR_CLASS")

</script>
