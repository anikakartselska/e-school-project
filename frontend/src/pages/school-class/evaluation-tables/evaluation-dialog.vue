<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Информация за {{ translationOfEvaluationValue[updatedEvaluation.evaluationType] }}
              <q-btn v-if="!readonly" color="negative" flat icon="delete" round @click="submitForDelete()"/>
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
          <div>
            Ученик:
            <router-link :to="`/user/${updatedEvaluation.student.id}/${periodId}/${schoolId}`"
                         active-class="text-negative" class="text-primary"
                         exact-active-class="text-negative">
              {{ updatedEvaluation.student.firstName }} {{ updatedEvaluation.student.lastName }}
            </router-link>
          </div>
          <div>
            Въведен от:
            <router-link :to="`/user/${updatedEvaluation.createdBy.id}/${periodId}/${schoolId}`"
                         active-class="text-negative" class="text-primary"
                         exact-active-class="text-negative">
              {{ updatedEvaluation.createdBy.firstName }} {{ updatedEvaluation.createdBy.lastName }}
            </router-link>
          </div>
          <div>
            Въведен на:
            <span class="text-primary">
                    {{ dateTimeToBulgarianLocaleString(updatedEvaluation.evaluationDate) }}
                </span>
          </div>
          <div>
            Предмет:
            <router-link v-if="updatedEvaluation.schoolLessonId!=null"
                         :to="`/school-lesson/${periodId}/${schoolId}/${updatedEvaluation.schoolLessonId}/lesson`"
                         active-class="text-negative" class="text-primary"
                         exact-active-class="text-negative">
              {{ updatedEvaluation.subject.name }}
              <q-tooltip>
                Кликни за да видиш повече информация за урока, в който е въведена оценката
              </q-tooltip>
            </router-link>
            <span v-else class="text-primary">
                    {{ updatedEvaluation.subject.name }}
                </span>
          </div>
          <q-input v-model="updatedEvaluation.comment" :readonly="readonly" dense label="Коментар" stack-label>
            <template v-slot:prepend>
              <q-icon name="edit"/>
            </template>
          </q-input>
          <div>
            Срок:
            <span class="text-primary">{{ translationOfSemester[updatedEvaluation.semester] }}</span>
          </div>
          <div v-if="updatedEvaluation.evaluationType === EvaluationType.GRADE">
              <span v-if="updatedEvaluation.evaluationValue.finalGrade" class="text-negative">
              {{ updatedEvaluation.semester === Semester.YEARLY ? 'Годишна оценка:' : 'Срочна оценка:' }}
              </span>
            <span v-else>Оценка: </span>
            <span class="text-primary">
              {{ gradeText.get(updatedEvaluation.evaluationValue.grade) }}</span>
            <q-btn :class="`q-ma-xs ${gradeBackgroundColorMap.get(updatedEvaluation.evaluationValue.grade)}`"
                   :label="gradeMap.get(updatedEvaluation.evaluationValue.grade)?.toString()"
                   flat
                   rounded>
              <q-badge v-if="!readonly" align="bottom" color="white" floating
                       style="width:2px; height: 2px ">
                <template v-slot:default>
                  <div class="row">
                    <q-btn class="bg-secondary absolute-left" icon="edit" round size="5px"
                           @click="editGrade()"/>
                  </div>
                </template>
              </q-badge>
            </q-btn>
          </div>
          <div v-if="updatedEvaluation.evaluationType === EvaluationType.ABSENCE">
            <span>Отсъствие: <span class="text-primary">{{
                getAbsenceValueText(updatedEvaluation.evaluationValue)
              }}</span></span>
            <q-btn :class="`q-ma-xs ${getAbsenceBackgroundColor(updatedEvaluation)}`"
                   :label="absenceMap.get(updatedEvaluation.evaluationValue.absence)"
                   flat
                   rounded>
              <q-badge v-if="!readonly" align="bottom" color="white" floating
                       style="width:2px; height: 2px ">
                <template v-slot:default>
                  <div class="row">
                    <q-btn class="bg-secondary absolute-left" icon="edit" round size="5px"
                           @click="editAbsence()"/>
                  </div>
                </template>
              </q-badge>
            </q-btn>
          </div>
          <div v-if="updatedEvaluation.evaluationType === EvaluationType.FEEDBACK">
            <q-select
                    v-model="updatedEvaluation.evaluationValue.feedback"
                    :option-label="option => feedbacksMapTranslation.get(option)"
                    :options="Object.keys(Feedback)"
                    :readonly="readonly"
                    color="black"
                    emit-value
                    label="Oтзив:"
                    map-options
                    stack-label
            >
              <template v-slot:option="scope">
                <q-item v-bind="scope.itemProps">
                  <q-item-section avatar>
                    <q-icon :color="feedbacksMap.get(scope.opt)===true? `green-14` : `red-14`"
                            :name="feedbacksMap.get(scope.opt)===true? 'thumb_up_alt' : 'thumb_down_alt'"
                    />
                  </q-item-section>
                  <q-item-section>
                    <q-item-label>{{ scope.label }}</q-item-label>
                  </q-item-section>
                </q-item>
              </template>
              <template v-slot:selected-item="scope">
                <span class="text-primary">{{ feedbacksMapTranslation.get(scope.opt) }}</span>
                <q-btn :class="feedbacksMap.get(scope.opt)===true? `text-green-14` : `text-red-14`"
                       :icon="feedbacksMap.get(scope.opt)===true? 'thumb_up_alt' : 'thumb_down_alt'"
                       flat
                       rounded
                >
                </q-btn>
              </template>
            </q-select>
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
import {AbsenceValue, Evaluation, EvaluationType, Feedback, GradeValue} from "../../../model/Evaluation";
import {cloneDeep} from "lodash-es";
import {
    confirmActionPromiseDialog,
    dateTimeToBulgarianLocaleString,
    translationOfEvaluationValue,
    translationOfSemester
} from "../../../utils";
import {
    absenceMap,
    feedbacksMap,
    feedbacksMapTranslation,
    getAbsenceBackgroundColor,
    getAbsenceValueText,
    gradeBackgroundColorMap,
    gradeMap,
    gradeText
} from "../../../services/helper-services/EvaluationService";
import {Semester} from "../../../model/SchoolPeriod";
import {periodId, schoolId} from "../../../model/constants";
import GradesChooseComponent from "./grades-choose-component.vue";
import AbsencesChooseComponent from "./absences-choose-component.vue";


const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  evaluation: Evaluation,
  periodId: number,
  schoolId: number,
  readonly: boolean
}>()

const updatedEvaluation = $ref(cloneDeep(props.evaluation))

const editGrade = () => {
  quasar.dialog({
    component: GradesChooseComponent,
  }).onOk(async (payload) => {
    (updatedEvaluation.evaluationValue as GradeValue).grade = payload.item
  })
}

const editAbsence = () => {
  quasar.dialog({
    component: AbsencesChooseComponent,
  }).onOk(async (payload) => {
    (updatedEvaluation.evaluationValue as AbsenceValue) = payload.item
  })
}
const submitForDelete = async () => {
  await confirmActionPromiseDialog(`Сигурни ли сте, че искате да изтриете ${translationOfEvaluationValue[updatedEvaluation.evaluationType]} ?`, '')
  onDialogOK({
    item: {evaluation: updatedEvaluation, delete: true}
  })
}

const submit = () => {
  onDialogOK({
    item: {evaluation: updatedEvaluation, delete: false}
  })
}
</script>

<style scoped>

</style>