<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Скала за оценяване
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
          <q-separator/>
          <div class="text-bold q-pl-lg">Слаб (2)</div>
          <div class="row">
            <div class="col-6 q-pr-lg">
              <q-input v-model="updatedGradingScale.interval2.startingPoints" disable label="От" stack-label
                       suffix="точки">
              </q-input>
            </div>
            <div class="col-6 q-pl-lg">
              <q-input v-model="updatedGradingScale.interval2.endingPoints"
                       :rules="[val=> ((+val>updatedGradingScale.interval2.startingPoints || !updatedGradingScale.interval2.startingPoints) && (+val<updatedGradingScale.interval3.startingPoints || !updatedGradingScale.interval3.startingPoints)) || 'Невалидни точки']"
                       label="До" reactive-rules
                       stack-label
                       suffix="точки"
                       type="number"
              >
              </q-input>
            </div>
          </div>
          <q-separator/>
          <div class="text-bold q-pl-lg">Среден (3)</div>
          <div class="row">
            <div class="col-6 q-pr-lg">
              <q-input v-model="updatedGradingScale.interval3.startingPoints"
                       :rules="[val=> (((+val) === +updatedGradingScale.interval2.endingPoints+1 || !updatedGradingScale.interval2.endingPoints) && (+val<updatedGradingScale.interval3.endingPoints || !updatedGradingScale.interval3.endingPoints)) || 'Невалидни точки']"
                       label="От" reactive-rules
                       stack-label
                       suffix="точки"
                       type="number"
              >
              </q-input>
            </div>
            <div class="col-6 q-pl-lg">
              <q-input v-model="updatedGradingScale.interval3.endingPoints"
                       :rules="[val=> ((+val>updatedGradingScale.interval3.startingPoints || !updatedGradingScale.interval3.startingPoints) && (+val<updatedGradingScale.interval4.startingPoints || !updatedGradingScale.interval4.startingPoints)) || 'Невалидни точки']"
                       label="До" reactive-rules
                       stack-label
                       suffix="точки"
                       type="number"
              >
              </q-input>
            </div>
          </div>
          <q-separator/>
          <div class="text-bold q-pl-lg">Добър (4)</div>
          <div class="row">
            <div class="col-6 q-pr-lg">
              <q-input v-model="updatedGradingScale.interval4.startingPoints"
                       :rules="[val=> ((+val=== +updatedGradingScale.interval3.endingPoints+1 || !updatedGradingScale.interval3.endingPoints) && (+val<updatedGradingScale.interval4.endingPoints || !updatedGradingScale.interval4.endingPoints)) || 'Невалидни точки']"
                       label="От" reactive-rules
                       stack-label
                       suffix="точки"
                       type="number"
              >
              </q-input>
            </div>
            <div class="col-6 q-pl-lg">
              <q-input v-model="updatedGradingScale.interval4.endingPoints"
                       :rules="[val=> ((+val>updatedGradingScale.interval4.startingPoints || !updatedGradingScale.interval4.startingPoints) && (+val<updatedGradingScale.interval5.startingPoints || !updatedGradingScale.interval5.startingPoints)) || 'Невалидни точки']"
                       label="До" reactive-rules
                       stack-label
                       suffix="точки"
                       type="number"
              >
              </q-input>
            </div>
          </div>
          <q-separator/>
          <div class="text-bold q-pl-lg">Мн.добър (5)</div>
          <div class="row">
            <div class="col-6 q-pr-lg">
              <q-input v-model="updatedGradingScale.interval5.startingPoints"
                       :rules="[val=> ((+val===+updatedGradingScale.interval4.endingPoints+1 || !updatedGradingScale.interval4.endingPoints) && (+val<updatedGradingScale.interval5.endingPoints || !updatedGradingScale.interval5.endingPoints)) || 'Невалидни точки']"
                       label="От" reactive-rules
                       stack-label
                       suffix="точки"
                       type="number"
              >
              </q-input>
            </div>
            <div class="col-6 q-pl-lg">
              <q-input v-model="updatedGradingScale.interval5.endingPoints"
                       :rules="[val=> ((+val>updatedGradingScale.interval5.startingPoints || !updatedGradingScale.interval5.startingPoints) && (+val<updatedGradingScale.interval6.startingPoints || !updatedGradingScale.interval6.startingPoints)) || 'Невалидни точки']"
                       label="До" reactive-rules
                       stack-label
                       suffix="точки"
                       type="number"
              >
              </q-input>
            </div>
          </div>
          <q-separator/>
          <div class="text-bold q-pl-lg">Отличен (6)</div>
          <div class="row">
            <div class="col-6 q-pr-lg">
              <q-input v-model="updatedGradingScale.interval6.startingPoints"
                       :rules="[val=> ((+val===+updatedGradingScale.interval5.endingPoints+1 || !updatedGradingScale.interval5.endingPoints) && (+val<updatedGradingScale.interval6.endingPoints || !updatedGradingScale.interval6.endingPoints)) || 'Невалидни точки']"
                       label="От" reactive-rules
                       stack-label
                       suffix="точки"
                       type="number"
              >
              </q-input>
            </div>
            <div class="col-6 q-pl-lg">
              <q-input v-model="updatedGradingScale.interval6.endingPoints" disable label="До" stack-label
                       suffix="точки">
              </q-input>
            </div>
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
import {confirmActionPromiseDialog} from "../../utils";
import {GradingScale} from "../../model/GradingScale";


const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  gradingScale: GradingScale | null,
  maximumPoints: number
}>()

const updatedGradingScale = $ref<GradingScale>(props.gradingScale ? {
  ...props.gradingScale,
  interval2: {startingPoints: "0", endingPoints: props.gradingScale.interval2.endingPoints},
  interval6: {startingPoints: props.gradingScale.interval6.startingPoints, endingPoints: props.maximumPoints.toString()}
} : <GradingScale>{
  interval2: {startingPoints: "0"},
  interval3: {},
  interval4: {},
  interval5: {},
  interval6: {endingPoints: props.maximumPoints.toString()}
})

const submit = async () => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  onDialogOK({
    item: {...updatedGradingScale}
  })
}
</script>

<style scoped>

</style>