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
          <q-input v-model="updatedAssignment.text" label="Описание" stack-label>
            <template v-slot:prepend>
              <q-icon name="edit"/>
            </template>
          </q-input>
          <div v-if="updatedAssignment.assignmentType === AssignmentType.EVENT">
            <q-input v-model="dateFrom" :model-value="dateTimeToBulgarianLocaleString(dateFrom)">
              <template v-slot:prepend>
                <q-icon class="cursor-pointer" name="event">
                  <q-popup-proxy cover transition-hide="scale" transition-show="scale">
                    <q-date v-model="dateFrom" mask="MM/DD/YYYY HH:mm">
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup color="primary" flat label="Затвори"/>
                      </div>
                    </q-date>
                  </q-popup-proxy>
                </q-icon>
              </template>

              <template v-slot:append>
                <q-icon class="cursor-pointer" name="access_time">
                  <q-popup-proxy cover transition-hide="scale" transition-show="scale">
                    <q-time v-model="dateFrom" format24h mask="MM/DD/YYYY HH:mm">
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup color="primary" flat label="Затвори"/>
                      </div>
                    </q-time>
                  </q-popup-proxy>
                </q-icon>
              </template>
            </q-input>
            <q-input v-model="dateTo" :model-value="dateTimeToBulgarianLocaleString(dateTo)">
              <template v-slot:prepend>
                <q-icon class="cursor-pointer" name="event">
                  <q-popup-proxy cover transition-hide="scale" transition-show="scale">
                    <q-date v-model="dateTo" mask="MM/DD/YYYY HH:mm">
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup color="primary" flat label="Затвори"/>
                      </div>
                    </q-date>
                  </q-popup-proxy>
                </q-icon>
              </template>

              <template v-slot:append>
                <q-icon class="cursor-pointer" name="access_time">
                  <q-popup-proxy cover transition-hide="scale" transition-show="scale">
                    <q-time v-model="dateTo" format24h mask="MM/DD/YYYY HH:mm">
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup color="primary" flat label="Затвори"/>
                      </div>
                    </q-time>
                  </q-popup-proxy>
                </q-icon>
              </template>
            </q-input>
            <q-select v-model="updatedAssignment.assignmentValue.room" :options="rooms" class="text-black" label="Място"
                      :option-label="option=>roomToSubjectsText(option)"
                      label-color="primary"
                      use-input/>
          </div>
          <div v-if="updatedAssignment.assignmentType === AssignmentType.HOMEWORK">
            <q-input v-model="dateTo" :model-value="dateTimeToBulgarianLocaleString(dateTo)">
              <template v-slot:prepend>
                <q-icon class="cursor-pointer" name="event">
                  <q-popup-proxy cover transition-hide="scale" transition-show="scale">
                    <q-date v-model="dateTo" mask="MM/DD/YYYY HH:mm">
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup color="primary" flat label="Затвори"/>
                      </div>
                    </q-date>
                  </q-popup-proxy>
                </q-icon>
              </template>

              <template v-slot:append>
                <q-icon class="cursor-pointer" name="access_time">
                  <q-popup-proxy cover transition-hide="scale" transition-show="scale">
                    <q-time v-model="dateTo" format24h mask="MM/DD/YYYY HH:mm">
                      <div class="row items-center justify-end">
                        <q-btn v-close-popup color="primary" flat label="Затвори"/>
                      </div>
                    </q-time>
                  </q-popup-proxy>
                </q-icon>
              </template>
            </q-input>
          </div>
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
import {Assignments, AssignmentType} from "../../../model/Assignments";
import {cloneDeep} from "lodash-es";
import {confirmActionPromiseDialog, dateTimeToBulgarianLocaleString} from "../../../utils";
import {RoomToSubjects, roomToSubjectsText} from "../../../model/School";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  assignments: Assignments,
  rooms: RoomToSubjects[],
  title: string,
}>()

const updatedAssignment = $ref(cloneDeep(props.assignments))
const dateFrom = $ref(updatedAssignment.assignmentValue.from ? updatedAssignment.assignmentValue.from : '')
const dateTo = $ref(updatedAssignment.assignmentValue.to ? updatedAssignment.assignmentValue.to : '')
const submit = async () => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
    let assignmentValue;
    if (updatedAssignment.assignmentType == AssignmentType.HOMEWORK) {
        assignmentValue = {...updatedAssignment.assignmentValue, to: new Date(dateTo + 'Z').toISOString().slice(0, 19)}
    } else if (updatedAssignment.assignmentType == AssignmentType.EVENT) {
        assignmentValue = {
            ...updatedAssignment.assignmentValue,
            to: new Date(dateTo + 'Z').toISOString().slice(0, 19),
            from: new Date(dateFrom + 'Z').toISOString().slice(0, 19)
        }
    } else {
        assignmentValue = updatedAssignment.assignmentValue
    }
    onDialogOK({
        item: {...updatedAssignment, assignmentValue: assignmentValue}
    })
}
</script>

<style scoped>

</style>