<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Добави {{ translationOfEvaluationValue[newEvaluation.evaluationType] }}
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
          <q-select v-model="newEvaluation.student"
                    :option-label="(student:StudentView) => `${student.numberInClass}. ${ student?.firstName } ${ student?.lastName }`"
                    :options="students"
                    label="Ученик"/>
          <q-select v-model="newEvaluation.subject"
                    :disable="props.subject!=null"
                    :option-label="(subject:Subject) => subject.name"
                    :options="subjects"
                    label="Предмет"/>
          <q-input v-model="newEvaluation.comment" dense label="Коментар" stack-label>
            <template v-slot:prepend>
              <q-icon name="edit"/>
            </template>
          </q-input>
          <div>
            Срок:
            <span class="text-primary">{{ translationOfSemester[newEvaluation.semester] }}</span>
          </div>
          <div v-if="newEvaluation.evaluationType === EvaluationType.GRADE">
              <span v-if="newEvaluation?.evaluationValue?.finalGrade" class="text-negative">
              {{ newEvaluation.semester === Semester.YEARLY ? 'Годишна оценка:' : 'Срочна оценка:' }}
              </span>
            <span v-else>Оценка: </span>
            <span v-if="newEvaluation?.evaluationValue">
            <span class="text-primary">
              {{ gradeText.get(newEvaluation?.evaluationValue?.grade) }}</span>
              <q-btn :class="`q-ma-xs ${gradeBackgroundColorMap.get(newEvaluation?.evaluationValue?.grade)}`"
                     :label="gradeMap.get(newEvaluation?.evaluationValue?.grade)?.toString()"
                     flat
                     rounded>
                <q-badge align="bottom" color="white" floating
                         style="width:2px; height: 2px ">
                  <template v-slot:default>
                    <div class="row">
                      <q-btn class="bg-secondary absolute-left" icon="edit" round size="5px"
                             @click="editGrade()"/>
                    </div>
                  </template>
                </q-badge>
              </q-btn>
                <br>
            <q-checkbox v-model="newEvaluation.evaluationValue.finalGrade"
                        :disable="existingFinalGrade"
                        :label=" newEvaluation.semester === Semester.YEARLY ? 'Годишна оценка:' : 'Срочна оценка:'"
                        left-label>
                <q-tooltip v-if="existingFinalGrade">
                    Ученикът вече има оформена {{
                    newEvaluation.semester === Semester.YEARLY ? 'годишна оценка' : 'срочна оценка'
                  }} по избрания предмет
                </q-tooltip>
            </q-checkbox>
            </span>
            <span v-else>
              <q-btn color="primary" icon="add" round size="sm" @click="editGrade()"/>
            </span>
          </div>
          <div v-if="newEvaluation.evaluationType === EvaluationType.ABSENCE">
            <span>Отсъствие: <span v-if="newEvaluation.evaluationValue" class="text-primary">{{
                getAbsenceValueText(newEvaluation.evaluationValue)
              }}</span></span>
            <q-btn v-if="newEvaluation.evaluationValue" :class="`q-ma-xs ${getAbsenceBackgroundColor(newEvaluation)}`"
                   :label="absenceMap.get(newEvaluation.evaluationValue.absence)"
                   flat
                   rounded>
              <q-badge align="bottom" color="white" floating
                       style="width:2px; height: 2px ">
                <template v-slot:default>
                  <div class="row">
                    <q-btn class="bg-secondary absolute-left" icon="edit" round size="5px"
                           @click="editAbsence()"/>
                  </div>
                </template>
              </q-badge>
            </q-btn>
            <span v-if="!newEvaluation.evaluationValue">
              <q-btn color="primary" icon="add" round size="sm" @click="editAbsence()"/>
            </span>
          </div>
          <div v-if="newEvaluation.evaluationType === EvaluationType.FEEDBACK">
            <q-select
                    v-model="newEvaluation.evaluationValue.feedback"
                    :option-label="option => feedbacksMapTranslation.get(option)"
                    :options="Object.keys(Feedback)"
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
            <q-btn :disable="existingFinalGrade && (newEvaluation.evaluationType===EvaluationType.GRADE && newEvaluation.evaluationValue.finalGrade===true)" color="primary" label="Готово" type="a"
                   @click="submit">
              <q-tooltip v-if="existingFinalGrade">
                Ученикът вече има оформена {{
                  newEvaluation.semester === Semester.YEARLY ? 'годишна оценка' : 'срочна оценка'
                }} по избрания предмет
              </q-tooltip>
            </q-btn>
            <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
          </q-card-actions>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>

import {$computed, $ref} from "vue/macros";
import {useDialogPluginComponent, useQuasar} from "quasar";
import {Evaluation, EvaluationType, Feedback, FeedbackValue, GradeValue} from "../../../model/Evaluation";
import {cloneDeep} from "lodash-es";
import {translationOfEvaluationValue, translationOfSemester} from "../../../utils";
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
import GradesChooseComponent from "../evaluation-tables/grades-choose-component.vue";
import AbsencesChooseComponent from "../evaluation-tables/absences-choose-component.vue";
import {StudentView} from "../../../model/User";
import {Subject} from "../../../model/Subject";


const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  evaluation: Evaluation,
  periodId: number,
  schoolId: number,
  students: StudentView[],
  subjects: Subject[],
  subject: Subject | null,
  lessonId: number | null,
  grades: any | null
}>()

const newEvaluation = $ref(cloneDeep(props.evaluation))

if (newEvaluation.evaluationType == EvaluationType.FEEDBACK) {
  newEvaluation.evaluationValue = <FeedbackValue>{}
}
if (props.subject != null) {
  newEvaluation.subject = props.subject
}
if (props.lessonId != null) {
  newEvaluation.schoolLessonId = props.lessonId
}

const existingFinalGrade = $computed(() => {
  if (props.evaluation.evaluationType == EvaluationType.GRADE && newEvaluation?.subject?.id && newEvaluation?.student?.id) {
    if (props.subject == null) {
      return props.grades[newEvaluation?.subject?.id][newEvaluation?.student?.id].find(grade => grade.evaluationValue.finalGrade == true && grade.semester === newEvaluation.semester) != null
    } else {
      return props.grades.find(it => it.student.id == newEvaluation.student.id && it.grades.find(grade => grade.evaluationValue.finalGrade && grade.semester === newEvaluation.semester) != null) != null
    }
  } else {
    return false
  }
})
const editGrade = () => {
  quasar.dialog({
    component: GradesChooseComponent,
  }).onOk(async (payload) => {
    newEvaluation.evaluationValue = <GradeValue>{
      grade: payload.item,
      finalGrade: newEvaluation.semester == Semester.YEARLY
    }
  })
}

const editAbsence = () => {
  quasar.dialog({
    component: AbsencesChooseComponent,
  }).onOk(async (payload) => {
    newEvaluation.evaluationValue = payload.item
  })
}

const submit = () => {
  onDialogOK({
    item: {evaluation: newEvaluation}
  })
}
</script>

<style scoped>

</style>