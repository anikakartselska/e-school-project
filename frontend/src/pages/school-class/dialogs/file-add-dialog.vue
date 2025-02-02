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
        <q-form class="q-gutter-md" @submit="submit()">
          <q-input v-model="note"
                   label="Коментар"/>
          <single-file-picker
                  v-model="file"
                  class="q-mt-md q-mr-sm"
                  label="Файл"
                  remove-action-button
          />
          <q-card-actions align="right">
            <q-btn color="primary" label="Добави" @click="submit"/>
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
import {confirmActionPromiseDialog} from "../../../utils";
import SingleFilePicker from "../../common/single-file-picker.vue";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  title: string,
  note: string | null,
  fileContent: File | null
}>()

const note = $ref(props?.note ? props?.note : '')
const file = $ref<File | null>(props.fileContent ? props.fileContent : null)
const submit = async () => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  onDialogOK({
    item: {
      note: note,
      file: file
    }
  })
}

</script>

<style scoped>

</style>