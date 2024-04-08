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
          <q-select v-model="teacher"
                    :option-label="(option:UserView) => `${option.firstName} ${option.middleName} ${option.lastName}`"
                    :options="teacherOptions"
                    :rules="[val=>val !== null && val !== '' || 'Задължително поле']"
                    label="Класен ръководител"/>
          <q-card-actions align="right">
            <q-btn color="primary" label="Промяна" @click="submit"/>
            <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
          </q-card-actions>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent, useQuasar} from "quasar";
import {UserView} from "../../model/User";
import {$ref} from "vue/macros";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  title: string,
  mainTeacher: UserView,
  teacherOptions: UserView[]
}>()

const teacher = $ref(props.mainTeacher)

const submit = () => {
  onDialogOK({
    item: teacher
  })
}
</script>

<style scoped>

</style>