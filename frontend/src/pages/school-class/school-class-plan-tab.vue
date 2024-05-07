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
                  {{ schoolClassPlan?.name }}
              </div>
          </template>
      </q-table>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {useRouter} from "vue-router";
import {useQuasar} from "quasar";
import {SchoolClass} from "../../model/SchoolClass";
import {fetchSchoolWeeksForSchoolClass, getPlanForSchoolClass} from "../../services/RequestService";
import {$computed, $ref} from "vue/macros";
import {SubjectAndClassesCount} from "../../model/SubjectAndClassesCount";

const props = defineProps<{
  periodId: number,
  schoolId: number,
  schoolClass: SchoolClass
}>()

const router = useRouter()
const quasar = useQuasar()
let schoolClassPlan = $ref(await getPlanForSchoolClass(props.schoolClass))
let subjectToClassesCount = schoolClassPlan?.subjectAndClassesCount ? schoolClassPlan?.subjectAndClassesCount : []
const weeksCount = await fetchSchoolWeeksForSchoolClass(props.schoolClass.name, props.schoolId, props.periodId)

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
  },
  {
    name: "classesPerSchoolYear",
    label: `Годишен брой учебни часове (Седмици: ${weeksCount})`,
    align: "left",
    field: (row: SubjectAndClassesCount) => row.classesPerSchoolYear,
    sortable: true
  },
])
</script>

<style scoped>

</style>