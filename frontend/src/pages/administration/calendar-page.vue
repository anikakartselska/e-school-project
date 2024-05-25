<template>
  <div class="bg-sms row">
      <div class="col-2"></div>
      <div class="col-8">
          <q-page class="page-content" padding>
              <div class="text-h4">
                  Календар
                  <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN]) && calendar" class="q-mr-xs" color="primary"
                         flat
                         icon="edit" round
                         @click="updateCalendar()"/>
                  <q-btn v-else-if="currentUserHasAnyRole([SchoolRole.ADMIN])" class="q-mr-xs" color="negative" flat
                         icon="add" round
                         @click="createCalendar()"/>
              </div>
              <span v-if="!calendar" class="text-negative">Училището все още няма календар за текущата година</span>
              <q-separator class="q-mt-md q-mb-md"/>
              <div class="row">
                  <div class="col-6">
                      Начало на учебната година: <span class="text-primary">{{
                      formatToBulgarian(calendar?.beginningOfYear)
                      }}</span><br>
                      Край на първи срок: <span class="text-primary"> {{
                      calendar ? `${formatToBulgarian(calendar?.endOfFirstSemester)} (${calendar?.firstSemesterWeeksCount} седмици)` : ``
                      }}</span>
                      <br>
                      Начало на първи срок: <span class="text-primary">{{
                      formatToBulgarian(calendar?.beginningOfSecondSemester)
                      }}</span><br>
                      Край на учебната година:<br>
                      <div v-for="schoolClass in classes">
                          {{ `${schoolClass} клас: ` }}<span class="text-primary"> {{
                          calendar ? `${formatToBulgarian(calendar?.classToEndOfYearDate[schoolClass])} (${calendar?.classToSecondSemesterWeeksCount[schoolClass]} седмици)` : ``
                          }}</span> <br>
                      </div>
                      <q-separator class="q-mr-xl q-mt-md q-mb-md"/>
                      <div class="text-h6">
                          Празници
                      </div>
                      <div v-for="restDay in calendar?.restDays">
                          {{ restDay.holidayName }}: <span class="text-primary"> {{
                          `${formatToBulgarian(restDay.from)} - ${formatToBulgarian(restDay.to)}`
                          }}</span> <br>
                      </div>
                      <q-separator class="q-mr-xl q-mt-md q-mb-md"/>
                      <div class="text-h6">
                          Изпити
                      </div>
                      <div v-for="restDay in calendar?.examDays">
                          {{ restDay.holidayName }}: <span class="text-primary"> {{
                          `${formatToBulgarian(restDay.from)} - ${formatToBulgarian(restDay.to)}`
                          }}</span> <br>
                      </div>
                      <q-separator class="q-mr-xl q-mt-md q-mb-md"/>
                      <div class="text-h6">
                          График
                      </div>
                      Първа смяна: <br>
                      Начало на учебните занятия: <span class="text-primary"> {{
                      calendar?.firstShiftSchedule?.startOfClasses
                      }}</span> <br>
                      Продължителност на часовете: <span class="text-primary"> {{
                      calendar ? `${calendar?.firstShiftSchedule?.durationOfClasses} мин.` : ``
                      }}</span> <br>
                      Продължителност на междучасията: <span class="text-primary"> {{
                      calendar ? `${calendar?.firstShiftSchedule?.breakDuration} мин.` : ``
                      }}</span> <br>
                      Втора смяна: <br>
                      Начало на учебните занятия: <span class="text-primary"> {{
                      calendar?.secondShiftSchedule?.startOfClasses
                      }}</span> <br>
                      Продължителност на часовете: <span class="text-primary"> {{
                      calendar ? `${calendar?.secondShiftSchedule?.durationOfClasses} мин.` : ``
                      }}</span> <br>
                      Продължителност на междучасията: <span class="text-primary"> {{
                      calendar ? `${calendar?.secondShiftSchedule?.breakDuration} мин.` : ``
                      }}</span> <br>
                  </div>
                  <div class="col">
                      <q-date v-model="selectedDate" :events="events" :locale="daysAndMonthsInBulgarian"
                              event-color="primary" style="width: 400px">
                          <template v-slot:default>
                              <div class="bg-negative text-white" v-html="title"/>
                          </template>
                      </q-date>
                  </div>
              </div>
          </q-page>
      </div>
  </div>
</template>
<script lang="ts" setup>
import {$computed, $ref} from "vue/macros";
import {fetchSchoolCalendarForSchoolAndPeriod, saveCalendarChanges} from "../../services/RequestService";
import {formatToBulgarian, formatWithDash, getDatesInRange, getRangeOf, isDateInRange} from "../../utils";
import {watch} from "vue";
import {date, useQuasar} from "quasar";
import CalendarEditDialog from "./dialogs/calendar-edit-dialog.vue";
import {Calendar} from "../../model/Calendar";
import {currentUserHasAnyRole} from "../../services/LocalStorageService";
import {SchoolRole} from "../../model/User";

const props = defineProps<{
    periodId: number,
    schoolId: number
}>()
let calendar = $ref(await fetchSchoolCalendarForSchoolAndPeriod(props.schoolId, props.periodId))

let classes = getRangeOf(1, 12, 1).map(it => it.toString())
const vacations = $computed(() => calendar?.restDays ? calendar?.restDays.concat(calendar?.examDays).map(it => getDatesInRange(it.from, it.to))?.flat(1)?.map(it => formatWithDash(date.formatDate(it, 'YYYY/MM/DD'))) : [])
const events = $computed(() => [calendar?.beginningOfYear, calendar?.beginningOfSecondSemester, calendar?.endOfFirstSemester].concat(calendar?.classToEndOfYearDate ? Object.entries(calendar?.classToEndOfYearDate).map(it => it[1]) : []).map(it => formatWithDash(it!!))
        .concat(vacations))
const selectedDate = $ref(null)
const daysAndMonthsInBulgarian = {
    days: ['неделя', 'понеделник', 'вторник', 'сряда', 'четвъртък', 'петък', 'събота'],
    daysShort: ['нед', 'пон', 'вт', 'ср', 'чет', 'пет', 'съб'],
    months: ['януари', 'февруари', 'март', 'април', 'май', 'юни', 'юли', 'август', 'септември', 'октомври', 'ноември', 'декември'],
    monthsShort: ['ян', 'фев', 'мар', 'апр', 'май', 'юни', 'юли', 'авг', 'сеп', 'окт', 'ное', 'дек']
}
const quasar = useQuasar()
const updateCalendar = async () => quasar.dialog({
    component: CalendarEditDialog,
    componentProps: {
        calendar: calendar,
        title: 'Редактирай календар'
    },
}).onOk(async (payload) => {
    const updatedCalendar = payload.item as Calendar
    await saveCalendarChanges(updatedCalendar, props.schoolId, props.periodId).then(e => {
        calendar = e
    })
})

const createCalendar = async () => quasar.dialog({
    component: CalendarEditDialog,
    componentProps: {
        calendar: calendar,
        title: 'Създай календар'
    },
}).onOk(async (payload) => {
    const updatedCalendar = payload.item as Calendar
    await saveCalendarChanges(updatedCalendar, props.schoolId, props.periodId).then(e => {
        calendar = e
    })
})
let title = $ref<string | null>(null)
watch(() => selectedDate, () => {
            title = ''
            if (selectedDate == null) {
                title = null
            } else {
                if (selectedDate == formatWithDash(calendar?.beginningOfYear ? calendar?.beginningOfYear : null)) {
                    title = title + `Първи учебен ден<br>`
                } else if (selectedDate == formatWithDash(calendar?.beginningOfSecondSemester ? calendar?.beginningOfSecondSemester : null)) {
                    title = title + `Начало на втори срок<br>`
                } else if (selectedDate == formatWithDash(calendar?.endOfFirstSemester ? calendar.endOfFirstSemester : null)) {
                    title = title + `Край на първи срок<br>`
                } else if ((calendar?.classToEndOfYearDate ? Object.values(calendar?.classToEndOfYearDate) : []).map(it => formatWithDash(it)).includes(selectedDate)) {
                    const selectedDateClass = (calendar?.classToEndOfYearDate ? Object.entries(calendar?.classToEndOfYearDate) : []).filter(it => selectedDate == formatWithDash(it[1]))!!
                    title = title + `Последен учебен ден за ${selectedDateClass.map(it => it[0])} клас<br>`
                } else if (vacations.includes(selectedDate)) {
                    title = title!! + calendar?.restDays.concat(calendar.examDays).filter(it => isDateInRange(selectedDate!!, it.from, it.to))
                            .map(it => `${it.holidayName}<br>`)
                } else {
                    title = null
                }
            }
        }
)

</script>

<style scoped>

</style>