<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card style="min-width: 800px">
      <q-btn v-close-popup
             class="float-right text-black"
             dense
             flat
             icon="close"
             round/>
      <q-table
              :columns="columns"
              :pagination="{rowsPerPage:20}"
              :rows="studentEvaluations"
              no-data-label="Няма данни в таблицата"
              row-key="student"
              separator="cell"
              title="Отзиви"
      >
        <template v-slot:body-cell-feedbacks="props">
          <q-td class="text-center">
            <q-select
                    v-model="props.row.feedbacks"
                    :option-label="option => feedbacksMapTranslation.get(option)"
                    :option-value="option => createFeedBack(option,props.row.student)"
                    :options="Object.keys(Feedback)"
                    color="primary"
                    emit-value
                    label="Добави отзиви"
                    map-options
                    multiple
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
                <q-btn :class="feedbacksMap.get(scope.opt)===true? `text-green-14` : `text-red-14`"
                       :icon="feedbacksMap.get(scope.opt)===true? 'thumb_up_alt' : 'thumb_down_alt'"
                       flat
                       rounded
                >
                  <q-tooltip>
                    {{ feedbacksMapTranslation.get(scope.opt) }}
                  </q-tooltip>
                  <q-badge align="bottom" color="white" floating
                           style="width:2px; height: 2px ">
                    <template v-slot:default>
                      <div class="row">
                        <q-btn class="bg-negative absolute-left" icon="close" round size="5px"
                               @click="scope.removeAtIndex(scope.index)"/>
                      </div>
                    </template>
                  </q-badge>
                </q-btn>
              </template>
            </q-select>
          </q-td>
        </template>
      </q-table>
      <q-card-actions align="right">
        <q-btn color="primary" label="Добави" @click="submit"/>
        <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent, useQuasar} from "quasar";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {$ref} from "vue/macros";
import {getCurrentUserAsUserView} from "../../services/LocalStorageService";
import {Subject} from "../../model/Subject";
import {Semester} from "../../model/SchoolPeriod";
import {Evaluation, EvaluationType, Feedback, FeedbackValue} from "../../model/Evaluation";
import {feedbacksMap, feedbacksMapTranslation} from "../../services/helper-services/EvaluationService";
import {StudentView} from "../../model/User";
import {SchoolLesson} from "../../model/SchoolLesson";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  subject: Subject,
  semester: Semester,
  evaluations: StudentWithEvaluationDTO[],
  lesson?: SchoolLesson | null
}>()
const currentUser = getCurrentUserAsUserView()
const studentEvaluations = $ref([...props.evaluations].map(it => {
  return {
    ...it,
    feedbacks: [],
    grades: [],
    absences: []
  }
}))

const submit = () => {
  onDialogOK({
    item: studentEvaluations
  })
}
const model = $ref([])

const createFeedBack = (feedback: Feedback, student: StudentView) => {
  return <Evaluation>{
    id: null,
    subject: props.subject,
    student: student,
    schoolLessonId: props.lesson?.id,
    evaluationType: EvaluationType.FEEDBACK,
    evaluationValue: <FeedbackValue>{feedback: feedback},
    semester: props.semester,
    createdBy: currentUser
  }
}

const columns = [
  {
    name: "numberInClass",
    label: "Номер в клас",
    align: "center",
    field: (row: StudentWithEvaluationDTO) => `${row.student?.numberInClass}`,
    sortable: true
  },
  {
    name: "student",
    label: "Име на ученика",
    align: "center",
    field: (row: StudentWithEvaluationDTO) => row.student ? `${row.student?.firstName} ${row.student?.middleName} ${row.student?.lastName}` : 'Общо',
    sortable: true
  },
  {
    name: "feedbacks",
    align: "center",
    label: "Отзиви",
  },
]
</script>

<style scoped>
.dialog-header {
  display: flex;
  justify-content: space-between;
}
</style>