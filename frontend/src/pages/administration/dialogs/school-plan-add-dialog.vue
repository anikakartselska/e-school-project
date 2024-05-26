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
          <q-input v-model="newOrUpdatedSchoolClassPlan.name"
                   :rules="[val=>val !== null && val !== '' || 'Задължително поле']"
                   label="Име"
                   reactive-rules/>
          <q-select v-model="newOrUpdatedSchoolClassPlan.schoolClassesWithTheSchoolPlan"
                    :option-label="(option:SchoolClass) => `${option.name}`"
                    :options="schoolClassesOptions"
                    label="Класове, учещи по програмата"
                    multiple
                    use-chips/>
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
import {SchoolClass} from "../../../model/SchoolClass";
import {SchoolPlanForClasses} from "../../../model/SchoolPlanForClasses";
import {confirmActionPromiseDialog} from "../../../utils";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  schoolClassesWithoutPrograms: SchoolClass[],
  schoolClassPlan: SchoolPlanForClasses | null
  title: string
}>()

const newOrUpdatedSchoolClassPlan: SchoolPlanForClasses = $ref(props.schoolClassPlan ? props.schoolClassPlan : <SchoolPlanForClasses><unknown>{
    schoolClassesWithTheSchoolPlan: [],
    subjectAndClassesCount: []
})
const schoolClassesOptions = [...props.schoolClassesWithoutPrograms].concat(props.schoolClassPlan?.schoolClassesWithTheSchoolPlan ? props.schoolClassPlan?.schoolClassesWithTheSchoolPlan : [])
const submit = async () => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
    onDialogOK({
        item: newOrUpdatedSchoolClassPlan
    })
}
</script>

<style scoped>

</style>