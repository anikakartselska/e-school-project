<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">{{ props.title }}
        <q-btn v-close-popup
               class="float-right text-black"
               dense
               flat
               icon="close"
               round/>
            </span>
      </q-card-section>
      <q-card-section>
        <q-form class="q-gutter-md" @submit="submit()">
          <q-select v-model="newOrUpdatedSubjectAndClassesCount.subjectName"
                    :disable="subjectAndClassesCount!=null"
                    :options="subjectOptions"
                    :rules="[val=>val !== null && val !== '' || 'Задължително поле']"
                    label="Предмет"/>
          <q-input v-model="newOrUpdatedSubjectAndClassesCount.classesPerWeek"
                   :rules="[val=>val !== null && val !== '' || 'Задължително поле']"
                   label="Часове на седмица"
                   reactive-rules
                   type="number"/>
          <q-card-actions align="right">
            <q-btn color="primary" label="Готово" @click="submit"/>
            <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
          </q-card-actions>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {useDialogPluginComponent, useQuasar} from "quasar";
import {SubjectAndClassesCount} from "../../../model/SubjectAndClassesCount";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  subjectAndClassesCount: SubjectAndClassesCount | null,
  subjects: string[]
  title: string
}>()

const newOrUpdatedSubjectAndClassesCount: SubjectAndClassesCount = $ref(props.subjectAndClassesCount ? {...props.subjectAndClassesCount} : <SubjectAndClassesCount><unknown>{
  classesPerSchoolYear: 0
})
const subjectOptions = [...props.subjects].concat(props.subjectAndClassesCount?.subjectName ? props.subjectAndClassesCount?.subjectName : [])
const submit = () => {
  onDialogOK({
    item: newOrUpdatedSubjectAndClassesCount
  })
}
</script>

<style scoped>

</style>