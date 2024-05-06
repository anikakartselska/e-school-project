<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Редактирай информацията за училище
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
          <q-input v-model="updatedSchool.schoolName" label="Име на училището" stack-label>
            <template v-slot:prepend>
              <q-icon name="edit"/>
            </template>
          </q-input>
          <q-input v-model="updatedSchool.city" label="Град" stack-label>
            <template v-slot:prepend>
              <q-icon name="edit"/>
            </template>
          </q-input>
          <q-input v-model="updatedSchool.address" label="Адрес" stack-label>
            <template v-slot:prepend>
              <q-icon name="edit"/>
            </template>
          </q-input>
          <q-select v-model="updatedSchool.rooms" class="text-black" hide-dropdown-icon input-debounce="0" label="Стаи"
                    label-color="primary" multiple
                    new-value-mode="add-unique" use-chips
                    use-input/>
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
import {$ref} from "vue/macros";
import {School} from "../../../model/School";


const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  school: School,
}>()

const updatedSchool = $ref({...props.school})

const submit = () => {
  onDialogOK({
    item: updatedSchool
  })
}
</script>

<style scoped>

</style>