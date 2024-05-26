<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Редактирай урок
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
          <q-field label="Предмет" stack-label><span class="text-black">{{
              updatedSchoolLesson?.subject?.name
            }}</span><br>
          </q-field>
          <q-select v-model="updatedSchoolLesson.subject.teacher"
                    :option-label="(option) => `${option.firstName} ${option.lastName}`" :options="teachers"
                    class="text-black"
                    label="Преподавател"
                    label-color="primary"/>
          <q-select v-model="updatedSchoolLesson.status"
                    :option-label="(option) => translationOfSchoolLessonStatus[option]"
                    :options="Object.keys(SchoolLessonStatus)"
                    class="text-black"
                    label="Статус"
                    label-color="primary"/>
          <q-field label="Паралелка" stack-label><span class="text-black">{{
              updatedSchoolLesson?.schoolClass.name
            }}</span><br>
          </q-field>
          <q-input v-model="updatedSchoolLesson.lessonTopic" label="Тема на урока" stack-label>
            <template v-slot:prepend>
              <q-icon name="edit"/>
            </template>
          </q-input>
          <q-checkbox v-model="updatedSchoolLesson.taken" label="Маркиран като взет" left-label/>
          <q-select v-model="updatedSchoolLesson.room" :options="rooms" class="text-black" label="Стая"
                    :option-label="option=> roomToSubjectsText(option)"
                    label-color="primary"/>
          <q-field label="Час на провеждане" stack-label><span class="text-black">{{
              `${dateTimeToBulgarianLocaleString(updatedSchoolLesson?.startTimeOfLesson)} - ${dateTimeToBulgarianLocaleString(updatedSchoolLesson?.endTimeOfLesson)}`
            }}</span><br></q-field>
          <q-field label="Седмица" stack-label><span class="text-black">{{
              updatedSchoolLesson?.week
            }}</span><br></q-field>
          <q-field label="Срок" stack-label><span class="text-black">{{
              translationOfSemester[updatedSchoolLesson?.semester]
            }}</span><br></q-field>
          <q-field label="Ден от седмицата" stack-label> <span class="text-black">{{
              `${translationOfWorkingDays[updatedSchoolLesson?.workingDay.workingDays]} (${updatedSchoolLesson.workingDay.hour} час)`
            }}</span><br></q-field>
          <q-card-actions align="right">
            <q-btn color="primary" label="Редактирай" @click="submit"/>
            <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
          </q-card-actions>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent, useQuasar} from "quasar";
import {SchoolLesson, SchoolLessonStatus, translationOfSchoolLessonStatus} from "../../model/SchoolLesson";
import {$ref} from "vue/macros";
import {
    confirmActionPromiseDialog,
    dateTimeToBulgarianLocaleString,
    translationOfSemester,
    translationOfWorkingDays
} from "../../utils";
import {UserView} from "../../model/User";
import {roomToSubjectsText} from "../../model/School";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  schoolLesson: SchoolLesson,
  rooms: string[],
  teachers: UserView[]
}>()

const updatedSchoolLesson = $ref({...props.schoolLesson})

const submit = async () => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
    onDialogOK({
        item: updatedSchoolLesson
    })
}
</script>

<style scoped>

</style>