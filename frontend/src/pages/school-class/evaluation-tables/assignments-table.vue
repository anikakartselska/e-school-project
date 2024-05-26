<template>
  <q-card>
    <q-table
            :columns="columns"
            :pagination="{rowsPerPage:20}"
            :rows="assignmentsFilteredByAssignmentTypeAndSemester"
            :title="assignmentTypePluralTranslation[tab]"
            hide-pagination
            no-data-label="Няма данни в таблицата"
            no-results-label="Няма резултати от вашето търсене"
            row-key="id"
            rows-per-page-label="Редове на страница"
            virtual-scroll
    >
      <template
              v-if="(tab === AssignmentType.EVENT || lesson!=null) && currentUserHasAnyRole([SchoolRole.ADMIN,SchoolRole.TEACHER])"
              v-slot:top-right>
        <div class="q-pr-xs">
          <q-btn color="secondary" icon="add"
                 outline rounded @click="addNewAssignment()">
          </q-btn>
        </div>
      </template>
      <template v-slot:body-cell-edit="props">
        <q-td>
          <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN,SchoolRole.TEACHER])" color="primary" dense flat
                 icon="edit"
                 @click="updateAssignment(props.row)">
          </q-btn>
          <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN,SchoolRole.TEACHER])" color="negative" dense flat
                 icon="delete"
                 @click="deleteAssignment(props.row)">
          </q-btn>
        </q-td>
      </template>
    </q-table>
  </q-card>
  <q-separator/>
</template>

<script lang="ts" setup>
import {SchoolClass} from "../../../model/SchoolClass";
import {
    Assignments,
    AssignmentType,
    assignmentTypePluralTranslation,
    assignmentTypeTranslation
} from "../../../model/Assignments";
import {$computed, $ref} from "vue/macros";
import {Semester} from "../../../model/SchoolPeriod";
import {
    constructAssignmentValueMessage,
    EventValue,
    ExaminationValue,
    HomeworkValue
} from "../../../model/AssignmentValue";
import {deleteAssignments, fetchSchoolById, mergeAssignments} from "../../../services/RequestService";
import {useQuasar} from "quasar";
import AssignmentsEditCreateDialog from "../dialogs/assignments-edit-create-dialog.vue";
import {SchoolLesson} from "../../../model/SchoolLesson";
import {currentUserHasAnyRole, getCurrentUserAsUserView} from "../../../services/LocalStorageService";
import {watch} from "vue";
import {confirmActionPromiseDialog, dateTimeToBulgarianLocaleString} from "../../../utils";
import {SchoolRole} from "../../../model/User";

const props = defineProps<{
  periodId: number
  schoolClass: SchoolClass
  schoolId: number,
  assignments: Assignments[]
  tab: AssignmentType,
  semester: Semester,
  lesson: SchoolLesson | null
}>()

let assignmentsFilteredByAssignmentTypeAndSemester = $ref([...props.assignments].filter(it => it.assignmentType === props.tab && it.semester === props.semester))
watch(() => props.tab, () => assignmentsFilteredByAssignmentTypeAndSemester = [...props.assignments].filter(it => it.assignmentType === props.tab && it.semester === props.semester))
const quasar = useQuasar()
const school = $ref(await fetchSchoolById(props.schoolId))
const addNewAssignment = () => {
  let assignmentValue;
  if (props.tab == AssignmentType.HOMEWORK) {
    assignmentValue = <HomeworkValue>{homeworkLesson: props.lesson}
  } else if (props.tab == AssignmentType.EVENT) {
    assignmentValue = <EventValue>{
      from: '',
      to: ''
    }
  } else {
    assignmentValue = <ExaminationValue>{lesson: props.lesson}
  }
  const assignment = <Assignments>{
    createdBy: getCurrentUserAsUserView(),
    semester: props.semester,
    assignmentType: props.tab,
    assignmentValue: assignmentValue
  }
  quasar.dialog({
    component: AssignmentsEditCreateDialog,
    componentProps: {
      assignments: assignment,
      rooms: school.rooms ? school.rooms : [],
      title: `Добави ${assignmentTypeTranslation[assignment.assignmentType]}`,
    },
  }).onOk(async (payload) => {
    const newAssignment = payload.item as Assignments
    await mergeAssignments(newAssignment, props.schoolClass.id, props.schoolId, props.periodId).then(response => {
              assignmentsFilteredByAssignmentTypeAndSemester.push(response)
            }
    )
  })
}
const updateAssignment = async (assignment: Assignments) => {
  quasar.dialog({
    component: AssignmentsEditCreateDialog,
    componentProps: {
      assignments: assignment,
      rooms: school.rooms ? school.rooms : [],
      title: `Редактирай ${assignmentTypeTranslation[assignment.assignmentType]}`,
    },
  }).onOk(async (payload) => {
    const updatedAssignment = payload.item as Assignments
    await mergeAssignments(updatedAssignment, props.schoolClass.id, props.schoolId, props.periodId).then(e => {
              assignmentsFilteredByAssignmentTypeAndSemester = assignmentsFilteredByAssignmentTypeAndSemester.map(it => {
                        if (it.id == assignment.id) {
                          return assignment
                        } else {
                          return it
                        }
                      }
              )
            }
    )
  })
}

const deleteAssignment = async (assignment: Assignments) => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  await deleteAssignments(assignment, props.schoolClass.id, props.schoolId, props.periodId).then(r =>
          assignmentsFilteredByAssignmentTypeAndSemester = assignmentsFilteredByAssignmentTypeAndSemester.filter(it =>
                  it.id != assignment.id
          )
  )
}

const columns = $computed(() => [
  {
    name: 'edit',
    headerClasses: 'q-table--col-auto-width'
  },
  {
    name: "createdBy",
    label: "Създадено от",
    align: "left",
    field: (row: Assignments) => `${row.createdBy.firstName} ${row.createdBy.lastName}`,
    sortable: true
  },
  {
    name: "createdOn",
    label: "Дата на създаване",
    align: "left",
    field: (row: Assignments) => dateTimeToBulgarianLocaleString(row.createdOn),
    sortable: true
  },
  {
    name: "text",
    label: "Описание",
    align: "left",
    field: (row: Assignments) => row.text,
    sortable: true
  },
  {
    name: "assignmentValue",
    label: "Детайли",
    align: "left",
    field: (row: Assignments) => constructAssignmentValueMessage(row),
    sortable: true
  },
])

</script>

<style scoped>

</style>