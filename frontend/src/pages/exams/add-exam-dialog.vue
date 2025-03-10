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
import {Exam} from "../../model/Exam";
import {$ref} from "vue/macros";
import {dateTimeToBulgarianLocaleString} from "../../utils";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
    exam: Exam | null
}>()
console.log(props)

const newlyCreatedExam = $ref(props.exam ? {...props.exam} : <Exam>{})
const dateFrom = $ref(newlyCreatedExam.startTimeOfExam ? newlyCreatedExam.startTimeOfExam : '')
const dateTo = $ref(newlyCreatedExam.endTimeOfExam ? newlyCreatedExam.endTimeOfExam : '')
const submit = () => {
    onDialogOK({
        item: {
            exam: {
                ...newlyCreatedExam,
                startTimeOfExam: new Date(dateFrom).toISOString().slice(0, 19),
                endTimeOfExam: new Date(dateTo).toISOString().slice(0, 19),
            }
        }
    })
}

</script>

<style scoped>

</style>