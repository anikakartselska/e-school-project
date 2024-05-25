<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Редактирай резултат за {{
              updatedStudentToYearlyResult.studentView.firstName
            }} {{ updatedStudentToYearlyResult.studentView.lastName }}
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
          <q-select v-model="updatedStudentToYearlyResult.yearlyResults.result"
                    :option-label="option => translationOfResultType[option]"
                    :options="Object.keys(ResultType)" label="Резултат">
            <template v-slot:option="scope">
              <q-item v-bind="scope.itemProps">
                <q-item-section>
                  <q-item-label :class="`${getResultTypeColorClass(scope.opt)} text-white`">{{
                      scope.label
                    }}
                  </q-item-label>
                </q-item-section>
              </q-item>
            </template>
            <template v-slot:selected-item="scope">
              <div :class="`${getResultTypeColorClass(scope.opt)} text-white`">{{
                  translationOfResultType[scope.opt]
                }}
              </div>
            </template>
          </q-select>
          <q-select v-model="updatedStudentToYearlyResult.yearlyResults.resultAfterTakingResitExams"
                    :disable="updatedStudentToYearlyResult.yearlyResults.result!==ResultType.TAKES_RESIT_EXAMS"
                    :option-label="option => translationOfResultType[option]"
                    :options="Object.keys(ResultType)" label="Резултат след изпит">
            <template v-slot:option="scope">
              <q-item v-bind="scope.itemProps">
                <q-item-section>
                  <q-item-label :class="`${getResultTypeColorClass(scope.opt)} text-white`">{{
                      scope.label
                    }}
                  </q-item-label>
                </q-item-section>
              </q-item>
            </template>
            <template v-slot:selected-item="scope">
              <div :class="`${getResultTypeColorClass(scope.opt)} text-white`">{{
                  translationOfResultType[scope.opt]
                }}
              </div>
            </template>
          </q-select>
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
import {
    getResultTypeColorClass,
    ResultType,
    StudentToYearlyResult,
    translationOfResultType,
    YearlyResults
} from "../../../model/YearlyResults";
import {$ref} from "vue/macros";
import {cloneDeep} from "lodash-es";
import {watch} from "vue";


const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  studentToYearlyResult: StudentToYearlyResult,
}>()

const updatedStudentToYearlyResult = $ref(cloneDeep({
  ...props.studentToYearlyResult,
  yearlyResults: props.studentToYearlyResult.yearlyResults ? props.studentToYearlyResult.yearlyResults : <YearlyResults>{}
}))

watch(() => updatedStudentToYearlyResult.yearlyResults.result, () => {
  if (updatedStudentToYearlyResult.yearlyResults.result !== ResultType.TAKES_RESIT_EXAMS) {
    updatedStudentToYearlyResult.yearlyResults.resultAfterTakingResitExams = null
  }
})
const submit = () => {
  onDialogOK({
    item: updatedStudentToYearlyResult
  })
}
</script>

<style scoped>

</style>