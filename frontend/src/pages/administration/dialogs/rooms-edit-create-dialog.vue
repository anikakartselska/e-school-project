<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Редактирай информацията за стая
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
          <q-input v-model="updatedRoomToSubjects.room" label="Стая" stack-label>
            <template v-slot:prepend>
              <q-icon name="edit"/>
            </template>
          </q-input>
          <q-select v-model="updatedRoomToSubjects.subjects" :options="subjects" class="text-black" hide-dropdown-icon
                    hint="Оставете празно за всички предмети"
                    input-debounce="0" label="Предмети, които могат да се провеждат в стаята"
                    label-color="primary"
                    multiple new-value-mode="add-unique"
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
import {useDialogPluginComponent, useQuasar} from "quasar";
import {$ref} from "vue/macros";
import {RoomToSubjects} from "../../../model/School";


const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  roomToSubjects: RoomToSubjects,
  subjects: string[]
}>()

const updatedRoomToSubjects = $ref({...props.roomToSubjects})

const submit = () => {
  onDialogOK({
    item: updatedRoomToSubjects
  })
}
</script>

<style scoped>

</style>