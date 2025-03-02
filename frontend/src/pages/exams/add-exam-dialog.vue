<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Добави изпитване
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
          <q-input v-model="newlyCreatedExam.examNote" class="q-pa-sm"
                   label="Заглавие на изпита"/>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent, useQuasar} from "quasar";
import {Exam} from "../../model/Exam";
import {$ref} from "vue/macros";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])


const newlyCreatedExam = $ref(<Exam>{})
const submit = () => {
  onDialogOK({
    item: {exam: newlyCreatedExam}
  })
}

</script>

<style scoped>

</style>