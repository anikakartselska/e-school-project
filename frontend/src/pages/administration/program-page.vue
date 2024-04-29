<template>
    <div class="bg-sms row">
        <div class="col-1"></div>
        <div class="col-10">
            <q-page class="page-content" padding>
                <div class="row">
                    <div class="text-h4">
                        Седмичен разпис
                    </div>
                    <q-space/>
                    <q-select v-model="selectedSchoolClass"
                              :option-label="option => option.name"
                              :options="schoolClasses"
                              label="Клас"
                              reactive-rules
                              style="width: 250px"/>
                </div>
                <q-separator class="q-mt-md q-mb-md"/>
                <div class="row">
                    <div v-for="day in Object.keys(WorkingDays)" class="col-2 q-ma-sm">
                        <div class=" text-center text-primary text-h6">
                            {{ workingDaysTranslation.get(day) }}
                        </div>
                        <q-card v-for="lesson in plannedLessonsGroupedByDay.get(WorkingDays[day])" class="q-mb-sm"
                                style="height: 12vh">
                            <q-card-section>
                                Предмет:<span class="text-primary">
              {{
                                lesson.subject
                                }}</span><br>
                                Учител:<span class="text-primary">
              {{ lesson.teacher.firstName }} {{ lesson.teacher.lastName }}</span>
                                <br>
                                Стая:<span class="text-primary">
              {{
                                lesson.room
                                }}
                  </span>
                            </q-card-section>
                        </q-card>
                    </div>
                </div>
            </q-page>
        </div>
    </div>
</template>
<script lang="ts" setup>
import {fetchPlannedSchoolLessonsForSchool, getSchoolClassesFromSchool} from "../../services/RequestService";
import {$ref} from "vue/macros";
import {watch} from "vue";
import {SchoolClass} from "../../model/SchoolClass";
import {
    PlannedSchoolLesson,
    WorkingDays,
    workingDaysOrder,
    workingDaysTranslation
} from "../../model/PlannedSchoolLesson";

const props = defineProps<{
    periodId: number,
    schoolId: number
}>()

const schoolPlannedLessons = await fetchPlannedSchoolLessonsForSchool(props.schoolId, props.periodId)
const schoolClasses = await getSchoolClassesFromSchool(props.schoolId, props.periodId)
let selectedSchoolClass = $ref<SchoolClass | null>(null)
const plannedLessonsGroupedByDay = $ref(new Map<WorkingDays, PlannedSchoolLesson[]>())

let plannedLessons = $ref<PlannedSchoolLesson[]>([])
watch(() => selectedSchoolClass, () => {
            console.log(selectedSchoolClass)
            if (selectedSchoolClass == null) {
                plannedLessons = []
            } else {
                plannedLessons = schoolPlannedLessons.filter(it => it.schoolClass.id == selectedSchoolClass!!.id).sort(function (a, b) {
                    return workingDaysOrder.get(a.workingHour.workingDays)!! - workingDaysOrder.get(b.workingHour.workingDays)!! || a.workingHour.hour - b.workingHour.hour;
                })
                Object.keys(WorkingDays).forEach(day => {
                    plannedLessonsGroupedByDay.set(day, plannedLessons.filter(lesson => lesson.workingHour.workingDays == day))

                })
            }
        }
)

</script>

<style scoped>

</style>