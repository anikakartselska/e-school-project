<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 300px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Оценки
        <q-btn v-close-popup
               class="float-right text-black"
               dense
               flat
               icon="close"
               round/>
            </span>
      </q-card-section>
      <q-form>
        <div class="text-center">
          <q-btn v-for="currentGrade in Object.keys(Grade)"
                 :class="`q-ma-xs ${gradeBackgroundColorMap.get(currentGrade)}`"
                 :label="gradeMap.get(currentGrade)?.toString()"
                 flat
                 rounded
                 type="a"
                 @click="chooseGrade(currentGrade)">
            <q-tooltip>
              Кликни, за да смениш оценка
            </q-tooltip>
          </q-btn>
        </div>
        <q-card-actions align="right">
          <q-btn color="primary" flat label="Отказ" @click="onDialogCancel"/>
        </q-card-actions>
      </q-form>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {Grade} from "../../../model/Evaluation";
import {useDialogPluginComponent, useQuasar} from "quasar";
import {gradeBackgroundColorMap, gradeMap} from "../../../services/helper-services/EvaluationService";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const chooseGrade = (grade: Grade) => {
  onDialogOK({
    item: grade
  })
}

</script>
<style scoped>

</style>