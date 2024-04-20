<template>
  <q-table
          :columns="columns"
          :pagination="{rowsPerPage:20}"
          :rows="feedbacks"
          :visible-columns="visibleColumns"
          no-data-label="Няма данни в таблицата"
          :row-key="getRowKey"
          separator="cell"
          title="Отзиви"
  >
      <template v-if="semester !== Semester.YEARLY" v-slot:top-right>
          <q-btn color="primary"
                 icon="add_circle_outline"
                 label="Добави отзиви за повече ученици"
                 outline
                 @click="addNewFeedbacks()"
          />
      </template>
      <template v-slot:header-cell-total="props">
          <q-th>
              <div class="row">
                  <div class="col text-center">
                      Позитивни
                  </div>
                  <q-separator vertical/>
                  <div class="col-4 text-center">
                      Негативни
                  </div>
          <q-separator vertical/>
          <div class="col-4 text-center">
            Общо
          </div>
        </div>
      </q-th>
    </template>
    <template v-slot:body-cell-feedbacks="props">
      <q-td class="text-center">
        <q-btn v-for="feedback in props.row.feedbacks?.filter(it=>it.semester === semester)"
               v-if="props.row.student?.firstName !== undefined"
               :class="feedbacksMap.get(feedback.evaluationValue.feedback)===true? `text-green-14` : `text-red-14`"
               :icon="feedbacksMap.get(feedback.evaluationValue.feedback)===true? 'thumb_up_alt' : 'thumb_down_alt'"
               flat
               rounded
        >
          <q-tooltip>
            {{ feedbacksMapTranslation.get(feedback.evaluationValue.feedback) }}
          </q-tooltip>
          <q-popup-proxy>
            <q-banner>
              Въведен от:<span class="text-primary">{{
                feedback.createdBy.firstName
              }} {{ feedback.createdBy.lastName }}</span><br/>
              Дата:<span class="text-primary">{{
                feedback.evaluationDate
              }}</span><br/>
            </q-banner>
          </q-popup-proxy>
        </q-btn>
      </q-td>
    </template>
    <template v-slot:body-cell-total="props">
      <q-td>
        <div class="col row">
          <div class="col text-center">
            <q-btn :class="`q-ma-xs bg-green-3`"
                   :label="countFeedbacksSum(props.row.feedbacks?.filter(it=>semester === Semester.YEARLY || it.semester === semester),true)"
                   flat
                   rounded>
            </q-btn>
          </div>
          <q-separator vertical/>
          <div class="col-4 text-center">
            <q-btn :class="`q-ma-xs bg-red-3`"
                   :label="countFeedbacksSum(props.row.feedbacks?.filter(it=>semester === Semester.YEARLY || it.semester === semester),false)"
                   flat
                   rounded>
            </q-btn>
          </div>
          <q-separator vertical/>
          <div class="col-4 text-center">
            <q-btn :class="`q-ma-xs bg-blue-3`"
                   :label="countFeedbacksSum(props.row.feedbacks?.filter(it=>semester === Semester.YEARLY || it.semester === semester))"
                   flat
                   rounded>
            </q-btn>
          </div>
        </div>
      </q-td>
    </template>
  </q-table>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {
    countFeedbacksSum,
    feedbacksMap,
    feedbacksMapTranslation
} from "../../services/helper-services/EvaluationService";
import {Semester} from "../../model/SchoolPeriod";
import {Evaluation} from "../../model/Evaluation";
import {Subject} from "../../model/Subject";
import {StudentView} from "../../model/User";
import {saveEvaluations} from "../../services/RequestService";
import {periodId, schoolId} from "../../model/constants";
import {useQuasar} from "quasar";
import AddFeedbacksDialog from "./add-feedbacks-dialog.vue";

const props = defineProps<{
    evaluations: StudentWithEvaluationDTO[],
    semester: Semester,
    subject: Subject
}>()
const feedbacks: StudentWithEvaluationDTO[] = $ref(props.evaluations ? [...props.evaluations] : [])
feedbacks.push(
        {
            feedbacks: props.evaluations?.map(it => it.feedbacks).flat(1)?.filter((it: Evaluation) => it.semester == props.semester || props.semester == Semester.YEARLY),
            student: <StudentView><unknown>{id: 10000},
            grades: [],
            absences: []
        })
const getRowKey = (row) => {
    return row?.student ? row?.student : '1000'
}
const quasar = useQuasar()
const addNewFeedbacks = async () => quasar.dialog({
    component: AddFeedbacksDialog,
    componentProps: {
        evaluations: props.evaluations,
        subject: props.subject,
        semester: props.semester
    },
}).onOk(async (payload) => {
    await saveEvaluations(payload.item, periodId.value, schoolId.value).then(e => {
                const newlyAddedFeedbacks = e.data
                feedbacks.forEach(studentEvaluations => {
                            const newlyAddedFeedbacksForCurrentStudent = newlyAddedFeedbacks.find(v => v.student.id == studentEvaluations.student.id)?.feedbacks
                            if (studentEvaluations.student.id == 10000) {
                                studentEvaluations.feedbacks = studentEvaluations.feedbacks.concat(newlyAddedFeedbacks.map(it => it.feedbacks).flat(1))
                            }
                            studentEvaluations.feedbacks = studentEvaluations.feedbacks.concat(newlyAddedFeedbacksForCurrentStudent ? newlyAddedFeedbacksForCurrentStudent : [])
                        }
                )
            }
    )
})
const columns = [
    {
        name: "numberInClass",
        label: "Номер в клас",
        align: "center",
        field: (row: StudentWithEvaluationDTO) => row?.student?.numberInClass != undefined ? `${row?.student?.numberInClass}` : '',
        sortable: true
    },
    {
        name: "student",
        label: "Име на ученика",
        align: "center",
        field: (row: StudentWithEvaluationDTO) => row?.student?.firstName != undefined ? `${row?.student?.firstName} ${row?.student?.middleName} ${row?.student?.lastName}` : 'Общо',
        sortable: true
    },
  {
    name: "feedbacks",
    align: "center",
    label: "Отзиви",
  },
  {
    name: "total",
    align: "center",
    label: "Общо"
  }
]
const visibleColumns = [...columns].filter(it => props.semester !== Semester.YEARLY || it.name != 'feedbacks').map(it => it.name)

</script>

<style scoped>

</style>