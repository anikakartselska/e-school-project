<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 300px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Отсъствия
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
          <q-btn v-for="currentAbsence in absencesOptions"
                 :class="`q-ma-xs ${getAbsenceValueBackgroundColor(currentAbsence)}`"
                 :label="absenceMap.get(currentAbsence.absence)?.toString()"
                 flat
                 rounded
                 type="a"
                 @click="chooseAbsences(currentAbsence)">
            <q-tooltip>
              Кликни, за да смениш отсъствие
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
import {AbsenceValue} from "../../../model/Evaluation";
import {useDialogPluginComponent, useQuasar} from "quasar";
import {
    absenceMap,
    absencesOptions,
    getAbsenceValueBackgroundColor
} from "../../../services/helper-services/EvaluationService";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const chooseAbsences = (absenceValue: AbsenceValue) => {
  onDialogOK({
    item: absenceValue
  })
}

</script>
<style scoped>

</style>