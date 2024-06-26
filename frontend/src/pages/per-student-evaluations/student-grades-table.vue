<template>
  <q-table
          :columns="columns"
          :pagination="{rowsPerPage:20}"
          :rows="grades"
          :visible-columns="visibleColumns"
          no-data-label="Няма данни в таблицата"
          row-key="subject"
          separator="cell"
          title="Оценки"
  >
      <template v-slot:body-cell-grades="props">
          <q-td class="text-center">
              <q-btn v-for="grade in props.row.grades.filter(it => !(it.evaluationValue.finalGrade === true) && it.semester === semester)"
                     v-if="props.row.subject !== undefined"
                     :class="`q-ma-xs ${gradeBackgroundColorMap.get(grade.evaluationValue.grade)}`"
                     :label="gradeMap.get(grade.evaluationValue.grade)?.toString()"
                     @click="updateEvaluationDialog(grade)"
                     flat
                     rounded>
                  <q-tooltip>Кликни за повече информация</q-tooltip>
              </q-btn>
          </q-td>
      </template>
      <template v-slot:body-cell-average="props">
          <q-td class="text-center">
              <q-btn v-if="!isNaN(calculateAverageGrade(props.row.grades.filter(it => !(it.evaluationValue.finalGrade === true) && it.semester === semester)))"
                     :class="`q-ma-xs ${getAverageGradeColorClass(calculateAverageGrade(props.row.grades.filter(it => !(it.evaluationValue.finalGrade === true) && it.semester === semester)))}`"
                     :label="calculateAverageGrade(props.row.grades.filter(it => !(it.evaluationValue.finalGrade === true) && it.semester === semester)) ? calculateAverageGrade((props.row.grades.filter(it => !(it.evaluationValue.finalGrade === true && it.semester === semester)))) : ''"
                     flat
                     rounded>
                  <q-tooltip>
                      Средноаритметична оценка
                  </q-tooltip>
              </q-btn>
          </q-td>
      </template>
      <template v-slot:body-cell-finalGrade="props">
          <q-td class="text-center">
              <q-btn v-for="grade in props.row.grades?.filter(it=> it.evaluationValue.finalGrade === true && it.semester === semester)"
                     v-if="props.row.subject !== undefined"
                     :class="`q-ma-xs ${gradeBackgroundColorMap.get(grade.evaluationValue.grade)}`"
                     :label="gradeMap.get(grade.evaluationValue.grade)?.toString()"
                     flat
                     @click="updateEvaluationDialog(grade)"
                     rounded>
                  <q-tooltip>Кликни за повече информация</q-tooltip>
              </q-btn>
              <q-btn v-else-if="!isNaN(calculateAverageGrade(props.row?.grades?.filter(it => it.evaluationValue.finalGrade === true && it.semester === semester),true))"
                     :class="`q-ma-xs ${getAverageGradeColorClass(calculateAverageGrade(props.row?.grades?.filter(it => it.evaluationValue.finalGrade === true && it.semester === semester),true))}`"
                     :label="calculateAverageGrade(props.row?.grades?.filter(it => it.evaluationValue.finalGrade === true && it.semester === semester),true) ? calculateAverageGrade((props.row?.grades?.filter(it => it.evaluationValue.finalGrade === true && it.semester === semester)),true) : ''"
                     flat
                     rounded>
                  <q-tooltip>
                      Средноаритметична оценка
                  </q-tooltip>
              </q-btn>
          </q-td>
      </template>
  </q-table>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {SubjectWithEvaluationDTO} from "../../model/SubjectWithEvaluationDTO";
import {
    calculateAverageGrade,
    getAverageGradeColorClass,
    gradeBackgroundColorMap,
    gradeMap,
} from "../../services/helper-services/EvaluationService";
import {Semester} from "../../model/SchoolPeriod";
import {Evaluation} from "../../model/Evaluation";
import {useQuasar} from "quasar";
import EvaluationDialog from "../school-class/dialogs/evaluation-delete-update-dialog.vue";
import {periodId, schoolId} from "../../model/constants";

const props = defineProps<{
    evaluations: SubjectWithEvaluationDTO[],
    semester: Semester
}>()
const grades = $ref([...props.evaluations,
    {
        grades: props.evaluations.map(it => it.grades).flat(1).filter((it: Evaluation) => it.semester == props.semester || props.semester == Semester.YEARLY),
        subject: undefined,
        absences: [],
        feedbacks: []
    }])

const quasar = useQuasar()
const updateEvaluationDialog = (evaluation: Evaluation) => {
    quasar.dialog({
        component: EvaluationDialog,
        componentProps: {
            evaluation: evaluation,
            periodId: periodId.value,
            schoolId: schoolId.value,
            readonly: true
        },
    })
}

const columns = [
    {
        name: "subject",
        label: "Предмет",
        align: "center",
        field: (row: SubjectWithEvaluationDTO) => row.subject?.name ? row.subject?.name : 'Общо',
        sortable: true
    },
    {
        name: "grades",
        align: "center",
        label: "Оценки",
    },
    {
        name: "average",
        align: "center",
        label: "Средно аритметично"
    },
    {
        name: "finalGrade",
        align: "center",
        label: "Оформена оценка"
    }
]
const visibleColumns = [...columns].filter(it => props.semester !== Semester.YEARLY || (it.name != 'grades' && it.name != 'average')).map(it => it.name)

</script>

<style scoped>

</style>