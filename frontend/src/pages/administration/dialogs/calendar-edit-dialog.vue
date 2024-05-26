<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">{{ title }}
        <q-btn v-close-popup
               class="float-right text-black"
               dense
               flat
               icon="close"
               round/>
            </span>
      </q-card-section>
      <q-card-section>
        <q-form class="q-gutter-md" @submit="submit">
          <div class="col-6">
            <input-date-picker v-model="updatedCalendar.beginningOfYear" label="Начало на учебната година"/>
            <input-date-picker v-model="updatedCalendar.endOfFirstSemester" label="Край на първи срок"/>
            <input-date-picker v-model="updatedCalendar.beginningOfSecondSemester" label="Начало на първи срок"/>
            <div v-for="schoolClass in classes">
              <input-date-picker v-model="updatedCalendar.classToEndOfYearDate[schoolClass]"
                                 :label="`Край на учебната година (${schoolClass} клас)`"/>
            </div>
            <q-separator class="q-mr-xl q-mt-md q-mb-md"/>
            <div class="text-h6">
              Празници
              <q-btn flat icon="add" round text-color="primary" @click="addRestDay"/>
            </div>
            <div v-for="(restDay,index) in updatedCalendar.restDays">
              {{ restDay.holidayName }}: <span class="text-primary"> {{
                `${formatToBulgarian(restDay.from)} - ${formatToBulgarian(restDay.to)}`
              }}</span>
              <q-btn flat icon="delete" round size="sm" text-color="negative" @click="deleteRestDay(index)"/>
              <q-btn flat icon="edit" round size="sm" text-color="secondary" @click="updateRestDay(index,restDay)"/>
              <br>
            </div>
            <q-separator class="q-mr-xl q-mt-md q-mb-md"/>
            <div class="text-h6">
              Изпити
              <q-btn flat icon="add" round text-color="primary" @click="addExamDay"/>
            </div>
            <div v-for="(examDay,index) in updatedCalendar.examDays">
              {{ examDay.holidayName }}: <span class="text-primary"> {{
                `${formatToBulgarian(examDay.from)} - ${formatToBulgarian(examDay.to)}`
              }}</span>
              <q-btn flat icon="delete" round size="sm" text-color="negative" @click="deleteExamDay(index)"/>
              <q-btn flat icon="edit" round size="sm" text-color="secondary" @click="updateExamDay(index,examDay)"/>
              <br>
            </div>
            <q-separator class="q-mr-xl q-mt-md q-mb-md"/>
            <div class="text-h6">
              График
            </div>
            Първа смяна: <br>
            <q-input v-model="updatedCalendar.firstShiftSchedule.startOfClasses" dense
                     label="Начало на учебните занятия" lazy-rules mask="##:##"/>
            <q-input v-model="updatedCalendar.firstShiftSchedule.durationOfClasses" dense
                     label="Продължителност на часовете" lazy-rules suffix="мин." type="number"/>
            <q-input v-model="updatedCalendar.firstShiftSchedule.breakDuration" dense
                     label="Продължителност на междучасията" lazy-rules suffix="мин." type="number"/>
            Втора смяна: <br>
            <q-input v-model="updatedCalendar.secondShiftSchedule.startOfClasses" dense
                     label="Начало на учебните занятия" lazy-rules mask="##:##"/>
            <q-input v-model="updatedCalendar.secondShiftSchedule.durationOfClasses" dense
                     label="Продължителност на часовете" lazy-rules suffix="мин." type="number"/>
            <q-input v-model="updatedCalendar.secondShiftSchedule.breakDuration" dense
                     label="Продължителност на междучасията" lazy-rules suffix="мин." type="number"/>
          </div>
          <q-card-actions align="right">
            <q-btn color="primary" label="Запази" @click="submit"/>
            <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
          </q-card-actions>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent, useQuasar} from "quasar";
import {$ref} from "vue/macros";
import {Calendar, DailySchedule, RestDay} from "../../../model/Calendar";
import {confirmActionPromiseDialog, formatToBulgarian, getRangeOf} from "../../../utils";
import RestOrExamDayAddDialog from "./rest-or-exam-day-add-dialog.vue";
import {cloneDeep} from "lodash-es";
import InputDatePicker from "../../common/input-date-picker.vue";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
    calendar: Calendar | null,
    title: string
}>()

const map = {
    1: null,
    2: null,
    3: null,
    4: null,
    5: null,
    6: null,
    7: null,
    8: null,
    9: null,
    10: null,
    11: null,
    12: null
};
const updatedCalendar = $ref(props.calendar ? cloneDeep(props.calendar) : <Calendar><unknown>{
    classToEndOfYearDate: {...map},
    classToSecondSemesterWeeksCount: {...map},
    restDays: [],
    examDays: [],
    firstShiftSchedule: <DailySchedule>{},
    secondShiftSchedule: <DailySchedule>{}
})
let classes = getRangeOf(1, 12, 1).map(it => it.toString())
const deleteRestDay = (index) => {
    updatedCalendar.restDays.splice(index, 1)
}

const deleteExamDay = (index) => {
    updatedCalendar.examDays.splice(index, 1)
}
const submit = async () => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
    onDialogOK({
        item: updatedCalendar
    })
}

const updateRestDay = async (index, restDay) => quasar.dialog({
  component: RestOrExamDayAddDialog,
  componentProps: {
    title: "Редактирай ваканция",
    type: "Ваканция",
    restDay: restDay
  },
}).onOk(async (payload) => {
  const updatedRestDay = payload.item as RestDay
  updatedCalendar.restDays = updatedCalendar.restDays.map((it, inx) => {
            if (inx == index) {
              return updatedRestDay
            } else {
              return it
            }
          }
  )
})

const addRestDay = async () => quasar.dialog({
  component: RestOrExamDayAddDialog,
  componentProps: {
    title: "Добави ваканция",
    type: "Ваканция"
  },
}).onOk(async (payload) => {
  const newRestDay = payload.item as RestDay
  updatedCalendar.restDays.push(newRestDay)
})

const updateExamDay = async (index, examDay) => quasar.dialog({
  component: RestOrExamDayAddDialog,
  componentProps: {
    title: "Редактирай ваканция",
    type: "Ваканция",
    restDay: examDay
  },
}).onOk(async (payload) => {
  const updatedExamDay = payload.item as RestDay
  updatedCalendar.examDays = updatedCalendar.examDays.map((it, inx) => {
            if (inx == index) {
              return updatedExamDay
            } else {
              return it
            }
          }
  )
})

const addExamDay = async () => quasar.dialog({
  component: RestOrExamDayAddDialog,
  componentProps: {
    title: "Добави изпит",
    type: "Изпит"
  },
}).onOk(async (payload) => {
  const newExamDay = payload.item as RestDay
  updatedCalendar.examDays.push(newExamDay)
})
</script>

<style scoped>

</style>