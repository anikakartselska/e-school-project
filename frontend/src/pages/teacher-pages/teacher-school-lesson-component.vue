<template>
    <div class="row">
        <div v-if="showTitle" class="text-h6">
            Седмична програма
        </div>
        <q-space v-if="showTitle"/>
        <q-select v-model="selectedWeek"
                  :option-label="(option:Week) => `Седмица ${option.weekNumber} (${formatToBulgarian(option.startDate)} - ${formatToBulgarian(option.endDate)})`"
                  :options="weekOptions"
                  label="Седмица"
        />
        <q-space v-if="!showTitle"/>
        <slot name="selectteacher"></slot>
    </div>
    <q-separator/>
    <div class="row">
        <div v-for="day in Object.keys(WorkingDays)" class="col-2 q-ma-sm">
            <div class=" text-center text-primary text-h6">
                {{ workingDaysTranslation.get(day) }}
            </div>
            <q-card v-for="lesson in schoolLessonsGroupedByDay.get(WorkingDays[day])"
                    :class="schoolLessonClass(lesson)"
                    class="q-mb-sm my-box cursor-pointer q-hoverable"
                    style="height: 16vh" @click="reRouteToLessonPage(lesson.id)">
                <q-card-section>
              <span v-if="lesson.status !== SchoolLessonStatus.NORMAL" class="text-primary text-bold">
                  {{ translationOfSchoolLessonStatus[lesson.status] }}
                  <br>
              </span>
                    Предмет:<span class="text-primary">
                      {{
                    lesson.subject.name
                    }}</span><br>
                    Учител:<span class="text-primary">
                      {{ lesson.subject.teacher?.firstName }} {{ lesson.subject.teacher?.lastName }}</span>
                    <br>
                    Стая:<span class="text-primary">
                      {{ lesson.room.room }}
          </span>
                    <br>
                    Час на провеждане: <span class="text-primary">
              {{ formatTime(lesson.startTimeOfLesson) }} - {{ formatTime(lesson.endTimeOfLesson) }}
          </span>
                </q-card-section>
            </q-card>
            <q-card v-if="schoolLessonsGroupedByDay.get(WorkingDays[day]).length === 0"
                    class="q-mb-sm my-box cursor-pointer q-hoverable"
                    style="height: 16vh">
                <q-card-section>
                    <span class="text-primary" v-html="defineDate(day)"/>
                </q-card-section>
            </q-card>
        </div>
    </div>
</template>
<script lang="ts" setup>
import {
    fetchMaxWeeksForSchoolAndPeriod,
    fetchSchoolCalendarForSchoolAndPeriod,
    fetchSchoolLessonsForTeacherWeekSchoolAndPeriod
} from "../../services/RequestService";
import {$ref} from "vue/macros";
import {SchoolLesson, SchoolLessonStatus, translationOfSchoolLessonStatus} from "../../model/SchoolLesson";
import {WorkingDays, workingDaysOrderString, workingDaysTranslation} from "../../model/PlannedSchoolLesson";
import {watch} from "vue";
import {formatTime, formatToBulgarian, formatWithDash, getDatesInRange, isDateInRange} from "../../utils";
import {useRouter} from "vue-router";
import {date} from "quasar";
import {Week} from "../../model/Calendar";

const props = defineProps<{
    periodId: number,
    schoolId: number,
    teacherId: number,
    showTitle: boolean
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
watch(() => [selectedWeek, props.teacherId], async () => {
    schoolLessonsFromSelectedWeek = await fetchSchoolLessonsForTeacherWeekSchoolAndPeriod(props.teacherId, selectedWeek.weekNumber, props.schoolId, props.periodId)
    Object.keys(WorkingDays).forEach(day => {
        schoolLessonsGroupedByDay.set(day, schoolLessonsFromSelectedWeek.filter((lesson: SchoolLesson) => lesson.workingDay.workingDays == day))
    })
})


const vacations = calendar?.restDays.concat(calendar?.examDays).map(it => getDatesInRange(it.from, it.to)).flat(1).map(it => formatWithDash(date.formatDate(it, 'YYYY/MM/DD')))

const defineDate = (dayOfWeek) => {
    let startDate = new Date(selectedWeek.startDate)
    startDate.setDate(startDate.getDate() + workingDaysOrderString.get(dayOfWeek)!! - 1)
    const selectedDate = date.formatDate(new Date(startDate), 'YYYY/MM/DD')

    let title: string | null = ''
    if (selectedDate == null) {
        title = null
    } else {
        if (calendar?.beginningOfYear && selectedDate == formatWithDash(calendar?.beginningOfYear)) {
            title = title + `Първи учебен ден<br>`
        } else if (calendar?.beginningOfSecondSemester && selectedDate == formatWithDash(calendar?.beginningOfSecondSemester)) {
            title = title + `Начало на втори срок<br>`
        } else if (calendar?.endOfFirstSemester && selectedDate == formatWithDash(calendar?.endOfFirstSemester)) {
            title = title + `Край на първи срок<br>`
        } else if (Object.values(calendar?.classToEndOfYearDate ? calendar?.classToEndOfYearDate : []).map(it => formatWithDash(it)).includes(selectedDate)) {
            const selectedDateClass = Object.entries(calendar?.classToEndOfYearDate ? calendar?.classToEndOfYearDate : []).filter(it => selectedDate == formatWithDash(it[1]))!!
            title = title + `Последен учебен ден за ${selectedDateClass.map(it => it[0])} клас<br>`
        } else if (vacations?.includes(selectedDate)) {
            title = title!! + calendar?.restDays.concat(calendar?.examDays).filter(it => isDateInRange(selectedDate!!, it.from, it.to))
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
