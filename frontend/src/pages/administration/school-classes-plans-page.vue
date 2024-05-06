<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-table
              :columns="columns"
              :filter="filter"
              :pagination="{rowsPerPage:10}"
              :rows="schoolPlans"
              no-data-label="Няма данни в таблицата"
              no-results-label="Няма резултати от вашето търсене"
              row-key="id"
              rows-per-page-label="Редове на страница"
              title="Учебни планове"
              @row-click.prevent="(_,row,__)=>openSchoolPlan(row)"
      >
        <template v-slot:body-cell-edit="props">
          <q-td>
            <q-btn dense flat icon="open_in_new" @click="openSchoolPlan(props.row)">
            </q-btn>
          </q-td>
        </template>
        <template v-slot:top-right="props">
          <q-btn class="q-ml-md" color="primary" icon="add" outline round @click="addNewSchoolPlan">
            <q-tooltip>Добави нов</q-tooltip>
          </q-btn>
          <q-input v-model="filter" debounce="300" dense outlined placeholder="Търсене">
            <template v-slot:append>
              <q-icon name="search"/>
            </template>
          </q-input>
          <q-btn
                  color="primary"
                  icon-right="archive"
                  label="Export to csv"
                  no-caps
          />
        </template>
      </q-table>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {useRouter} from "vue-router";
import {
    fetchAllSchoolClassesFromSchoolAndPeriodWithoutPlans,
    fetchAllSchoolPlansForSchool
} from "../../services/RequestService";
import {useQuasar} from "quasar";
import {SchoolPlanForClasses} from "../../model/SchoolPlanForClasses";
import SchoolPlanAddDialog from "./dialogs/school-plan-add-dialog.vue";
import {periodId} from "../../model/constants";

const props = defineProps<{
  schoolId: number
}>()
const quasar = useQuasar()
const router = useRouter()
const schoolPlans = $ref(await fetchAllSchoolPlansForSchool(props.schoolId))
const openSchoolPlan = (schoolPlan: SchoolPlanForClasses) => {
  router.push({
    path: `/school-class-plan/${props.schoolId}/${schoolPlan.id}`,
  });
}


const addNewSchoolPlan = async () =>
        quasar.dialog({
          component: SchoolPlanAddDialog,
          componentProps: {
            schoolClassesWithoutPrograms: await fetchAllSchoolClassesFromSchoolAndPeriodWithoutPlans(props.schoolId, periodId.value),
            title: "Добави учебен план"
          },
        }).onOk(async (payload) => {
          const schoolClassToStudentsFile = payload.item as SchoolPlanForClasses
          console.log(schoolClassToStudentsFile)

        })

const filter = $ref('')
const columns = [
  {name: 'edit'},
  {
    name: "name",
    label: "Име на учебната програма",
    align: "left",
    field: (row: SchoolPlanForClasses) => row.name,
    sortable: true
  },
  {
    name: "schoolClassesWithTheSchoolPlan",
    align: "left",
    label: "Класове",
    field: (row: SchoolPlanForClasses) => row.schoolClassesWithTheSchoolPlan.map(it => it.name).join(','),
    sortable: true
  }
]

</script>

<style scoped>

</style>