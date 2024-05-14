<template>
  <q-table
          :columns="columns"
          :pagination="{rowsPerPage:20}"
          :rows="absences"
          :visible-columns="visibleColumns"
          no-data-label="Няма данни в таблицата"
          :row-key="getRowKey"
          separator="cell"
          title="Отсъствия"
  >
    <template v-if="semester !== Semester.YEARLY" v-slot:top-right>
        <q-btn color="secondary" icon="add" label="Добави отсъствие" outline @click="addEvaluationDialog()"/>
        <q-btn class="q-ml-sm"
               color="primary"
               icon="add_circle_outline"
               label="Добави отсъствия за повече ученици"
               outline
               @click="addNewAbsences()"
        />
        <q-btn class="q-ml-sm"
               color="secondary"
               icon="check"
               label="Извини отсъствия"
               outline
             @click="updateAbsences()"
      />
    </template>
    <template v-slot:header-cell-total="props">
      <q-th>
        <div class="row">
          <div class="col text-center">
            Извинени
          </div>
          <q-separator vertical/>
          <div class="col-4 text-center">
            Неизвинени
          </div>
          <q-separator vertical/>
          <div class="col-4 text-center">
            Общо
          </div>
        </div>
      </q-th>
    </template>
    <template v-slot:body-cell-absences="props">
      <q-td class="text-center">
        <q-btn v-for="absence in props.row.absences.filter(it=>it.semester === semester)"
               v-if="props.row.student.firstName !== undefined"
               :class="`q-ma-xs ${getAbsenceBackgroundColor(absence)}`"
               :label="absenceMap.get(absence.evaluationValue.absence)"
               flat
               @click="updateEvaluationDialog(absence)"
               rounded>
            <q-tooltip>Кликни за повече информация</q-tooltip>
        </q-btn>
      </q-td>
    </template>
    <template v-slot:body-cell-total="props">
      <q-td>
        <div class="col row">
          <div class="col text-center">
            <q-btn :class="`q-ma-xs bg-green-3`"
                   :label="calculateAbsencesSum(props.row.absences.filter(it=>semester === Semester.YEARLY || it.semester === semester),true)"
                   flat
                   rounded>
            </q-btn>
          </div>
          <q-separator vertical/>
          <div class="col-4 text-center">
            <q-btn :class="`q-ma-xs bg-red-3`"
                   :label="calculateAbsencesSum(props.row.absences.filter(it=>semester === Semester.YEARLY || it.semester === semester),false)"
                   flat
                   rounded>
            </q-btn>
          </div>
          <q-separator vertical/>
          <div class="col-4 text-center">
            <q-btn :class="`q-ma-xs bg-blue-3`"
                   :label="calculateAbsencesSum(props.row.absences.filter(it=>semester === Semester.YEARLY || it.semester === semester))"
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
import {
    absenceMap,
    calculateAbsencesSum,
    getAbsenceBackgroundColor,
} from "../../services/helper-services/EvaluationService";
import {Semester} from "../../model/SchoolPeriod";
import {Evaluation, EvaluationType} from "../../model/Evaluation";
import {StudentWithEvaluationDTO} from "../../model/StudentWithEvaluationDTO";
import {Subject} from "../../model/Subject";
import {StudentView} from "../../model/User";
import {useQuasar} from "quasar";
import AddAbsencesDialog from "./add-absences-dialog.vue";
import {
    deleteEvaluation,
    saveEvaluation,
    saveEvaluations,
    updateEvaluation,
    updateEvaluations
} from "../../services/RequestService";
import {periodId, schoolId} from "../../model/constants";
import UpdateAbsencesDialog from "./update-absences-dialog.vue";
import {SchoolLesson} from "../../model/SchoolLesson";
import {commentPromiseDialog} from "../../utils";
import EvaluationDialog from "../school-class/dialogs/evaluation-delete-update-dialog.vue";
import EvaluationCreateDialog from "../school-class/dialogs/evaluation-create-dialog.vue";
import {getCurrentUserAsUserView} from "../../services/LocalStorageService";

const props = defineProps<{
    evaluations: StudentWithEvaluationDTO[],
    semester: Semester,
    subject: Subject,
    lesson?: SchoolLesson | null,
}>()

const absences: StudentWithEvaluationDTO[] = $ref(props.evaluations ? [...props.evaluations] : [])
absences.push(
        {
          absences: props.evaluations?.map(it => it.absences).flat(1)?.filter((it: Evaluation) => it.semester == props.semester || props.semester == Semester.YEARLY),
          student: <StudentView><unknown>{id: 10000},
          grades: [],
          feedbacks: []
        })

const getRowKey = (row) => {
  return row?.student ? row?.student : '1000'
}
const quasar = useQuasar()
const addNewAbsences = async () => quasar.dialog({
  component: AddAbsencesDialog,
  componentProps: {
    evaluations: props.evaluations,
    subject: props.subject,
    semester: props.semester,
    lesson: props.lesson
  },
}).onOk(async (payload) => {
  const comment = await commentPromiseDialog()
  await saveEvaluations(payload.item, periodId.value, schoolId.value, comment).then(e => {
            const newlyAddedAbsences = e.data
            absences.forEach(studentEvaluations => {
                      const newlyAddedAbsencesForCurrentStudent = newlyAddedAbsences.find(v => v.student.id == studentEvaluations.student.id)?.absences
                      if (studentEvaluations.student.id == 10000) {
                        studentEvaluations.absences = studentEvaluations.absences.concat(newlyAddedAbsences.map(it => it.absences).flat(1))
                      }
                      studentEvaluations.absences = studentEvaluations.absences.concat(newlyAddedAbsencesForCurrentStudent ? newlyAddedAbsencesForCurrentStudent : [])
                    }
            )
          }
  )
})

const updateAbsences = async () => quasar.dialog({
  component: UpdateAbsencesDialog,
  componentProps: {
    evaluations: props.evaluations,
    subject: props.subject
  },
}).onOk(async (payload) => {
  await updateEvaluations(payload.item, periodId.value, schoolId.value).then(e => {
            const updatedAbsences = payload.item
            const allAbsences = (<Evaluation[]>updatedAbsences.map(it => it.absences)).flat(1)
            absences.forEach(studentEvaluations => {
                        const updatedAbsencesForCurrentStudent = updatedAbsences.find(v => v.student.id == studentEvaluations.student.id)?.absences
                        if (studentEvaluations.student.id == 10000) {
                            studentEvaluations.absences = studentEvaluations.absences.map(absence =>
                                    allAbsences.find(oldAbsence => oldAbsence.id == absence.id) ? allAbsences.find(oldAbsence => oldAbsence.id == absence.id)!! : absence)
                        }
                        studentEvaluations.absences = studentEvaluations.absences.map(absence =>
                                updatedAbsencesForCurrentStudent.find(it => it.id == absence.id) ? updatedAbsencesForCurrentStudent.find(it => it.id == absence.id) : absence)

                    }
            )
          }
  )
})
const updateEvaluationDialog = (evaluation: Evaluation) => {
    quasar.dialog({
        component: EvaluationDialog,
        componentProps: {
            evaluation: evaluation,
            periodId: periodId.value,
            schoolId: schoolId.value,
            readonly: false
        },
    }).onOk(async (payload) => {
        const updatedAbsence = payload.item.evaluation as Evaluation
        if (payload.item.delete == false) {
            await updateEvaluation(updatedAbsence, periodId.value, schoolId.value).then(e => {
                        absences.forEach(evaluation => {
                            if (evaluation.student.id == updatedAbsence.student.id || evaluation.student.id == 10000) {
                                evaluation.absences = evaluation.absences.map(it => {
                                    if (it.id == updatedAbsence.id) {
                                        return updatedAbsence
                                    } else {
                                        return it
                                    }
                                })
                            }
                        })
                    }
            )
        } else {
            await deleteEvaluation(updatedAbsence, periodId.value, schoolId.value).then(e => {
                        absences.forEach(evaluation => {
                            if (evaluation.student.id == updatedAbsence.student.id || evaluation.student.id == 10000) {
                                evaluation.absences = evaluation.absences.filter(it => it.id !== updatedAbsence.id)
                            }
                        })
                    }
            )
        }
    })
}

const addEvaluationDialog = () => {
    quasar.dialog({
        component: EvaluationCreateDialog,
        componentProps: {
            evaluation: <Evaluation>{
                evaluationType: EvaluationType.ABSENCE,
                semester: props.semester,
                createdBy: getCurrentUserAsUserView()
            },
            periodId: periodId.value,
            schoolId: schoolId.value,
            students: props.evaluations.map(it => it.student),
            subjects: [],
            subject: props.subject,
            lessonId: props.lesson?.id
        },
    }).onOk(async (payload) => {
        const createdAbsence = payload.item.evaluation as Evaluation
        await saveEvaluation(createdAbsence, periodId.value, schoolId.value).then(newlyCreatedAbsence => {
                    absences.forEach(evaluation => {
                        if (evaluation.student.id == newlyCreatedAbsence.data.student.id || evaluation.student.id == 10000) {
                            evaluation.absences = [...evaluation.absences, newlyCreatedAbsence.data]
                        }
                    })
                }
        )
    })
}

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
    name: "absences",
    align: "center",
    label: "Отсъствия",
  },
  {
    name: "total",
    align: "center",
    label: "Общо"
  }
]
const visibleColumns = [...columns].filter(it => props.semester !== Semester.YEARLY || it.name != 'absences').map(it => it.name)

</script>

<style scoped>

</style>