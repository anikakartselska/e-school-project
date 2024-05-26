<template>
    <div class="bg-sms row">
        <div class="col-2"></div>
        <div class="col-8">
            <q-page class="page-content" padding>
                <teacher-school-lesson-component :period-id="periodId" :school-id="schoolId" :show-title="true"
                                                 :teacher-id="teacherId"/>
            </q-page>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {
    fetchMaxWeeksForSchoolAndPeriod,
    fetchSchoolCalendarForSchoolAndPeriod,
    fetchSchoolLessonsForTeacherWeekSchoolAndPeriod
} from "../../services/RequestService";
import {formatWithDash, getDatesInRange, isDateInRange} from "../../utils";
import {WorkingDays, workingDaysOrderString} from "../../model/PlannedSchoolLesson";
import {watch} from "vue";
import {$ref} from "vue/macros";
import {SchoolLesson, SchoolLessonStatus} from "../../model/SchoolLesson";
import {useRouter} from "vue-router";
import {date} from "quasar";
import TeacherSchoolLessonComponent from "./teacher-school-lesson-component.vue";

const props = defineProps<{
    periodId: number,
    schoolId: number,
  teacherId: number
}>()

const router = useRouter()
const currentDate = new Date()

const weekOptions = await fetchMaxWeeksForSchoolAndPeriod(props.schoolId, props.periodId)
const currentWeek = weekOptions.find(it => new Date(it.startDate) <= currentDate && new Date(it.endDate) >= currentDate)
const selectedWeek = $ref(currentWeek ? currentWeek : weekOptions.at(weekOptions.length - 1)!!)
let schoolLessonsFromSelectedWeek = $ref<SchoolLesson[]>(await fetchSchoolLessonsForTeacherWeekSchoolAndPeriod(props.teacherId, selectedWeek.weekNumber, props.schoolId, props.periodId))
const schoolLessonsGroupedByDay = $ref(new Map<WorkingDays, SchoolLesson[]>())
const calendar = $ref(await fetchSchoolCalendarForSchoolAndPeriod(props.schoolId, props.periodId))
Object.keys(WorkingDays).forEach(day => {
  schoolLessonsGroupedByDay.set(day, schoolLessonsFromSelectedWeek.filter((lesson: SchoolLesson) => lesson.workingDay.workingDays == day))
})
watch(() => selectedWeek, async () => {
  schoolLessonsFromSelectedWeek = await fetchSchoolLessonsForTeacherWeekSchoolAndPeriod(props.teacherId, selectedWeek.weekNumber, props.schoolId, props.periodId)
  Object.keys(WorkingDays).forEach(day => {
    schoolLessonsGroupedByDay.set(day, schoolLessonsFromSelectedWeek.filter((lesson: SchoolLesson) => lesson.workingDay.workingDays == day))
  })
})


const vacations = calendar.restDays.concat(calendar.examDays).map(it => getDatesInRange(it.from, it.to)).flat(1).map(it => formatWithDash(date.formatDate(it, 'YYYY/MM/DD')))

const defineDate = (dayOfWeek) => {
  let startDate = new Date(selectedWeek.startDate)
  startDate.setDate(startDate.getDate() + workingDaysOrderString.get(dayOfWeek)!! - 1)
  const selectedDate = date.formatDate(new Date(startDate), 'YYYY/MM/DD')
  debugger
  let title: string | null = ''
  if (selectedDate == null) {
    title = null
  } else {
    if (selectedDate == formatWithDash(calendar.beginningOfYear)) {
      title = title + `Първи учебен ден<br>`
    } else if (selectedDate == formatWithDash(calendar.beginningOfSecondSemester)) {
      title = title + `Начало на втори срок<br>`
    } else if (selectedDate == formatWithDash(calendar.endOfFirstSemester)) {
      title = title + `Край на първи срок<br>`
    } else if (Object.values(calendar.classToEndOfYearDate).map(it => formatWithDash(it)).includes(selectedDate)) {
      const selectedDateClass = Object.entries(calendar.classToEndOfYearDate).filter(it => selectedDate == formatWithDash(it[1]))!!
      title = title + `Последен учебен ден за ${selectedDateClass.map(it => it[0])} клас<br>`
    } else if (vacations.includes(selectedDate)) {
      title = title!! + calendar.restDays.concat(calendar.examDays).filter(it => isDateInRange(selectedDate!!, it.from, it.to))
              .map(it => `${it.holidayName}<br>`)
    } else {
      title = null
    }
  }
  return title
}


const schoolLessonClass = (lesson: SchoolLesson): string => {
  if (lesson.taken) {
    return 'bg-green-1'
  }
  if (lesson.status === SchoolLessonStatus.FREE) {
    return 'bg-blue-2'
  }
  if (lesson.status === SchoolLessonStatus.SUBSTITUTION) {
    return 'bg-yellow-2'
  }
  return ''
}

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

.page-content {
    box-shadow: 0 30px 25px rgba(0, 0, 0, 0.4);
}

</style>