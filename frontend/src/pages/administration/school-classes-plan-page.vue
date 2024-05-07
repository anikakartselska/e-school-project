<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-table
              :columns="columns"
              :pagination="{rowsPerPage:20}"
              :rows="subjectToClassesCount"
              no-data-label="Няма данни в таблицата"
              no-results-label="Няма резултати от вашето търсене"
              row-key="id"
              rows-per-page-label="Редове на страница"
              title="Учебни предмети, седмичен и годишен брой на учебните часове"
      >
        <template v-slot:top-left>
          <div class="text-h6">Учебни предмети, седмичен и годишен брой на учебните часове</div>
          <div class="text-h6">
            <span class="text-primary">Име на учебния план: </span>
            {{ plan?.name }}
            <q-btn color="primary" dense flat icon="edit" @click="updateSchoolPlan(plan)">
            </q-btn>
          </div>
          <div class="text-h6">
            <span class="text-primary">Класове обучаващи се по учебния план: </span>
            {{ plan?.schoolClassesWithTheSchoolPlan.map(it => it.name).join(",") }}
          </div>
        </template>
        <template v-slot:top-right>
          <div class="q-pr-xs">
            <q-btn color="secondary" icon="add" label="Добави нов предмет"
                   outline rounded @click="addNewSubjectAndClassesCount()">
            </q-btn>
          </div>
          <div class="q-pr-xs">
            <q-btn color="primary" icon="save" label="Запази промените"
                   outline rounded @click="save()">
            </q-btn>
          </div>
          <div>
            <q-btn color="negative" icon="restart_alt" label="Върни промените"
                   outline rounded @click="reset()">
            </q-btn>
          </div>
        </template>
        <template v-slot:body-cell-edit="props">
          <q-td>
            <q-btn color="primary" dense flat icon="edit" @click="updateSubjectAndClassesCount(props.row)">
            </q-btn>
            <q-btn color="negative" dense flat icon="delete" @click="deleteSubjectAndClassesCount(props.row)">
            </q-btn>
          </q-td>
        </template>
      </q-table>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {useRouter} from "vue-router";
import {useQuasar} from "quasar";
import {
    fetchAllSchoolClassesFromSchoolAndPeriodWithoutPlans,
    fetchPlanById,
    getAllSubjects,
    mergeSchoolPlansForClasses
} from "../../services/RequestService";
import {$computed, $ref} from "vue/macros";
import {SubjectAndClassesCount} from "../../model/SubjectAndClassesCount";
import {SchoolPlanForClasses} from "../../model/SchoolPlanForClasses";
import SchoolPlanAddDialog from "./dialogs/school-plan-add-dialog.vue";
import SubjectAndClassesCountCreateUpdateDialog from "./dialogs/subject-and-classes-count-create-update-dialog.vue";
import {cloneDeep} from "lodash-es";


const props = defineProps<{
  periodId: number,
  schoolId: number,
  schoolPlanId: number
}>()

const router = useRouter()
const quasar = useQuasar()
let plan = $ref(await fetchPlanById(props.schoolPlanId, props.schoolId, props.periodId))
let planFromDatabase = $ref(cloneDeep(plan))
let subjectToClassesCount = $computed(() => plan?.subjectAndClassesCount ? plan?.subjectAndClassesCount : [])
const allSubjects = $ref(await getAllSubjects())

const reset = () => {
  plan = cloneDeep(planFromDatabase)
}

const save = async () => {
  await mergeSchoolPlansForClasses(plan, props.schoolId, props.periodId).then(r => {
    planFromDatabase = plan
  })
}

const updateSchoolPlan = async (schoolPlanForClasses: SchoolPlanForClasses) =>
        quasar.dialog({
          component: SchoolPlanAddDialog,
          componentProps: {
            schoolClassesWithoutPrograms: await fetchAllSchoolClassesFromSchoolAndPeriodWithoutPlans(props.schoolId, props.periodId),
            schoolClassPlan: schoolPlanForClasses,
            title: "Редактирай учебен план"
          },
        }).onOk(async (payload) => {
          const newSchoolPlanForClasses = payload.item as SchoolPlanForClasses
          await mergeSchoolPlansForClasses(newSchoolPlanForClasses, props.schoolId, props.periodId).then(e =>
                  plan = newSchoolPlanForClasses
          )
        })

const addNewSubjectAndClassesCount = async () => {
  const alreadyExistingSubjects = plan.subjectAndClassesCount.map(it => it.subjectName)
  quasar.dialog({
    component: SubjectAndClassesCountCreateUpdateDialog,
    componentProps: {
      subjects: allSubjects.filter(it => !alreadyExistingSubjects.includes(it)),
      title: "Добави часовете на седмица за предмет"
    },
  }).onOk(async (payload) => {
            const newSubjectAndClassesCount = payload.item as SubjectAndClassesCount
            plan.subjectAndClassesCount.push(newSubjectAndClassesCount)
          }
  )
}

const updateSubjectAndClassesCount = async (subjectAndClassesCount: SubjectAndClassesCount) => {
  const alreadyExistingSubjects = plan.subjectAndClassesCount.map(it => it.subjectName)
  quasar.dialog({
    component: SubjectAndClassesCountCreateUpdateDialog,
    componentProps: {
      subjectAndClassesCount: subjectAndClassesCount,
      subjects: allSubjects.filter(it => !alreadyExistingSubjects.includes(it)),
      title: "Редактирай часовете на седмица за предмет"
    },
  }).onOk(async (payload) => {
            const newSubjectAndClassesCount = payload.item as SubjectAndClassesCount
            plan.subjectAndClassesCount = plan.subjectAndClassesCount.map(it => {
              if (it.subjectName == newSubjectAndClassesCount.subjectName) {
                return newSubjectAndClassesCount
              } else {
                return it
              }
            })
          }
  )
}


const deleteSubjectAndClassesCount = (subjectAndClassesCount: SubjectAndClassesCount) => {
  plan.subjectAndClassesCount = plan.subjectAndClassesCount.filter(it => !(it.subjectName === subjectAndClassesCount.subjectName))
}


const columns = $computed(() => [
  {
    name: 'edit',
    headerClasses: 'q-table--col-auto-width'
  },
  {
    name: "subjectName",
    label: "Учебен предмет",
    align: "left",
    field: (row: SubjectAndClassesCount) => row.subjectName,
    sortable: true
  },
  {
    name: "classesPerWeek",
    label: "Седмичен брой учебни часове",
    align: "left",
    field: (row: SubjectAndClassesCount) => row.classesPerWeek,
    sortable: true
  }
])
</script>

<style scoped>

</style>