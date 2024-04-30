<template>
  <div>
    <div class="row">
      <q-select v-model="selectedWeek"
                :option-label="(option:Week) => `Седмица ${option.weekNumber} (${formatToBulgarian(option.startDate)} - ${formatToBulgarian(option.endDate)})`"
                :options="weekOptions"
                label="Седмица"

      />
    </div>
    <q-separator/>
    <div class="row">
      <div v-for="day in Object.keys(WorkingDays)" class="col-2 q-ma-sm">
        <div class=" text-center text-primary text-h6">
          {{ workingDaysTranslation.get(day) }}
        </div>
        <q-card v-for="lesson in schoolLessonsGroupedByDay.get(WorkingDays[day])" class="q-mb-sm"
                style="height: 12vh" @click="reRouteToLessonPage(lesson.id)">
          <q-card-section>
            Предмет:<span class="text-primary">
                      {{
              lesson.subject.name
            }}</span><br>
            Учител:<span class="text-primary">
                      {{ lesson.subject.teacher?.firstName }} {{ lesson.subject.teacher?.lastName }}</span>
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
  </div>
</template>

<script lang="ts" setup>
import {
    fetchSchoolLessonsForSchoolClassWeekSchoolAndPeriod,
    fetchWeeksForSchoolClassSchoolAndPeriod
} from "../../services/RequestService";
import {SchoolClass} from "../../model/SchoolClass";
import {formatToBulgarian} from "../../utils";
import {WorkingDays, workingDaysTranslation} from "../../model/PlannedSchoolLesson";
import {watch} from "vue";
import {$ref} from "vue/macros";
import {SchoolLesson} from "../../model/SchoolLesson";
import {useRouter} from "vue-router";

const props = defineProps<{
  periodId: number,
  schoolId: number,
  schoolClass: SchoolClass
}>()
const router = useRouter()
const currentDate = new Date()
const weekOptions = await fetchWeeksForSchoolClassSchoolAndPeriod(props.schoolClass.name, props.schoolId, props.periodId)
const currentWeek = weekOptions.find(it => new Date(it.startDate) <= currentDate && new Date(it.endDate) >= currentDate)
const selectedWeek = $ref(currentWeek ? currentWeek : weekOptions.at(weekOptions.length - 1)!!)
let schoolLessonsFromSelectedWeek = $ref<SchoolLesson[]>(await fetchSchoolLessonsForSchoolClassWeekSchoolAndPeriod(props.schoolClass.id, selectedWeek.weekNumber, props.schoolId, props.periodId))
const schoolLessonsGroupedByDay = $ref(new Map<WorkingDays, SchoolLesson[]>())
Object.keys(WorkingDays).forEach(day => {
  schoolLessonsGroupedByDay.set(day, schoolLessonsFromSelectedWeek.filter((lesson: SchoolLesson) => lesson.workingDay.workingDays == day))
})
watch(() => selectedWeek, async () => {
  schoolLessonsFromSelectedWeek = await fetchSchoolLessonsForSchoolClassWeekSchoolAndPeriod(props.schoolClass.id, selectedWeek.weekNumber, props.schoolId, props.periodId)
  Object.keys(WorkingDays).forEach(day => {
    schoolLessonsGroupedByDay.set(day, schoolLessonsFromSelectedWeek.filter((lesson: SchoolLesson) => lesson.workingDay.workingDays == day))
  })
})

const reRouteToLessonPage = async (lessonId) => await router.push(`/school-lesson/${props.periodId}/${props.schoolId}/${lessonId}/lesson`)


</script>

<style scoped>
.q-card:hover {
  background-color: #cce3f8; /* Light grey when hovered in light mode */
}

/* Dark mode specific hover background */
.body--dark
.q-card:hover {
  background-color: #003038; /* Darker grey when hovered in dark mode */
}

</style>