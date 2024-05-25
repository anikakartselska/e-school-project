<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <div class="row">
        <div class="col-3"></div>
        <q-separator class="q-ma-sm" vertical/>
        <div class="col q-pb-sm">
          <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN,SchoolRole.TEACHER])"
                 class="float-right q-ma-sm" color="primary" icon="edit" round @click="updateLesson"/>
          <div class="text-h6 q-pb-lg q-pt-sm">
            Информация за урока
          </div>
          Предмет: <span class="text-primary">{{
            currentLesson?.subject?.name
          }}</span><br>
          Преподавател:
          <router-link :to="`/user/${currentLesson?.subject?.teacher?.id}/${periodId}/${schoolId}`"
                       active-class="text-negative" class="text-primary"
                       exact-active-class="text-negative">
            {{
              `${currentLesson?.subject?.teacher?.firstName} ${currentLesson?.subject?.teacher?.lastName}`
            }}
          </router-link>
          <br>
          Паралелка: <span class="text-primary">{{
            currentLesson?.schoolClass.name
          }}</span><br>
          Тема на урока: <span class="text-primary">{{
            currentLesson?.lessonTopic
          }}</span><br>
          Маркиран като взет: <span class="text-primary">{{
            currentLesson?.taken === true ? 'Да' : 'Не'
          }}</span><br>
          Стая: <span class="text-primary">{{
            currentLesson?.room?.room
          }}</span><br>
          Час на провеждане: <span class="text-primary">{{
            `${dateTimeToBulgarianLocaleString(currentLesson?.startTimeOfLesson)} - ${dateTimeToBulgarianLocaleString(currentLesson?.endTimeOfLesson)}`
          }}</span><br>
          Седмица: <span class="text-primary">{{
            currentLesson?.week
          }}</span><br>
          Срок: <span class="text-primary">{{
            translationOfSemester[currentLesson?.semester]
          }}</span><br>
          Ден от седмицата: <span class="text-primary">{{
            `${translationOfWorkingDays[currentLesson?.workingDay.workingDays]} (${currentLesson?.workingDay.hour} час)`
          }}</span><br>
        </div>
        <q-separator class="q-ma-sm" vertical/>
        <div class="col-3"></div>
      </div>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {SchoolLesson} from "../../model/SchoolLesson";
import {dateTimeToBulgarianLocaleString, translationOfSemester, translationOfWorkingDays} from "../../utils";
import {
    fetchAvailableRoomsForSchoolLesson,
    fetchAvailableTeachersForSchoolLesson,
    updateSchoolLesson
} from "../../services/RequestService";
import {useQuasar} from "quasar";
import SchoolLessonEditDialog from "./school-lesson-edit-dialog.vue";
import {$ref} from "vue/macros";
import {currentUserHasAnyRole} from "../../services/LocalStorageService";
import {SchoolRole} from "../../model/User";

const props = defineProps<{
  lesson: SchoolLesson
  periodId: number
  schoolId: number
}>()
const quasar = useQuasar()
let currentLesson = $ref(props.lesson)
const updateLesson = async () => quasar.dialog({
  component: SchoolLessonEditDialog,
  componentProps: {
    schoolLesson: currentLesson,
    rooms: await fetchAvailableRoomsForSchoolLesson(currentLesson, props.schoolId, props.periodId),
    teachers: await fetchAvailableTeachersForSchoolLesson(currentLesson, props.schoolId, props.periodId)
  },
}).onOk(async (payload) => {
  const schoolLesson = payload.item as SchoolLesson
  await updateSchoolLesson(schoolLesson).then(e => {
    currentLesson = schoolLesson
  })

})
</script>

<style scoped>

</style>