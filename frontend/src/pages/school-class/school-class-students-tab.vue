<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-table
              :columns="columns"
              :filter="filter"
              :pagination="{rowsPerPage:10}"
              :rows="studentsFromSchoolClass"
              no-data-label="Няма данни в таблицата"
              no-results-label="Няма резултати от вашето търсене"
              row-key="id"
              rows-per-page-label="Редове на страница"
              title="Ученици"
              @row-click.prevent="(_,row,__)=>openUser(row)"
      >
        <template v-slot:body-cell-edit="props">
          <q-td>
            <q-btn dense flat icon="open_in_new" @click="openUser(props.row)">
            </q-btn>
          </q-td>
        </template>
        <template v-slot:top-right="props">
          <single-file-picker
                  v-model="studentsFile"
                  accept-file-format=".xlsx"
                  class="q-mt-md q-mr-sm"
                  label="Добави ученици"
                  @action-button-event="addStudentsWithExcel()"
          />
          <q-input v-model="filter" debounce="300" dense outlined placeholder="Търсене">
            <template v-slot:append>
              <q-icon name="search"/>
            </template>
          </q-input>
          <q-btn
                  color="primary"
                  flat
                  icon-right="sync"
                  no-caps
                  @click="syncSchoolClassNumbersInClass"
          >
            <q-tooltip>
              Синхронизирай номерата на учениците
            </q-tooltip>
          </q-btn>
        </template>
      </q-table>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {getAllStudentsFromSchoolClass, syncNumbersInClass, uploadUsersExcelFile} from "../../services/RequestService";
import {SchoolRole, StudentView, UserView} from "../../model/User";
import {useRouter} from "vue-router";
import {useQuasar} from "quasar";
import SingleFilePicker from "../common/single-file-picker.vue";
import {SchoolClass} from "../../model/SchoolClass";
import {Subject} from "../../model/Subject";

const props = defineProps<{
  periodId: number,
  schoolId: number,
  schoolClass: SchoolClass
  subjectsTaughtInSchoolClassForYear: Subject[]
  studentsFromSchoolClass: StudentView[]
}>()
let studentsFromSchoolClass = $ref(props.studentsFromSchoolClass)
const router = useRouter()
const quasar = useQuasar()
let studentsFile = $ref(null)
const filter = $ref('')
const openUser = (row: UserView) => {
  router.push({
    name: "user",
    params: {
      id: row.id
    }
  })
}
const addStudentsWithExcel = async () => {
  await uploadUsersExcelFile(studentsFile, props.periodId, props.schoolId, SchoolRole.STUDENT, props.schoolClass.id)
}
const syncSchoolClassNumbersInClass = async () => {
  await syncNumbersInClass(props.schoolClass.id, props.periodId).then(async e =>
          studentsFromSchoolClass = await getAllStudentsFromSchoolClass(props.schoolClass.id, props.periodId))
}
const columns = [
  {name: 'edit'},
  {
    name: "firstName",
    align: "left",
    label: "Име",
    field: (row: StudentView) => row.firstName,
    sortable: true
  },
  {
    name: "middleName",
    align: "left",
    label: "Презиме",
    field: (row: StudentView) => row.middleName,
    sortable: true
  },
  {
    name: "lastName",
    align: "left",
    label: "Фамилия",
    field: (row: StudentView) => row.lastName,
    sortable: true
  },
  {
    name: "username",
    align: "left",
    label: "Потребителско име",
    field: (row: StudentView) => row.username,
    sortable: true
  },
  {
    name: "numberInClass",
    align: "left",
    label: "Номер в клас",
    field: (row: StudentView) => row.numberInClass,
    sortable: true
  },
  {
    name: "email",
    label: "Имейл",
    align: "left",
    field: (row: StudentView) => row.email,
    sortable: true
  }
]
</script>

<style scoped>

</style>