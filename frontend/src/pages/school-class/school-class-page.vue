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
        <template v-slot:top-left="props">
          <div class="text-h4">
            Клас: {{ schoolClass.name }}
          </div>
          <div class="text-h5">
            Класен ръководител:
            <router-link :to="`/user/${schoolClass.mainTeacher.id}/${periodId}/${schoolId}`"
                         active-class="text-negative" class="text-primary" exact-active-class="text-negative">
              {{ schoolClass.mainTeacher.firstName }} {{ schoolClass.mainTeacher.lastName }}
            </router-link>
            <q-btn class="q-mr-xs" color="primary" flat icon="edit" round @click="updateMainTeacher"/>
          </div>
        </template>
        <template v-slot:top-right="props">
          <q-btn class="q-mr-xs" color="primary" label="Добави нов" outline/>
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
import {
    getAllStudentsFromSchoolClass,
    getAllTeachersThatDoNotHaveSchoolClass,
    getSchoolClassById,
    saveSchoolClass,
    syncNumbersInClass
} from "../../services/RequestService";
import {StudentView, UserView} from "../../model/User";
import {useRouter} from "vue-router";
import {useQuasar} from "quasar";
import ChangeMainTeacherDialog from "./change-main-teacher-dialog.vue";

const props = defineProps<{
  periodId: number,
  schoolId: number,
  schoolClassId: number
}>()
const schoolClass = $ref(await getSchoolClassById(props.schoolClassId, props.periodId))
let studentsFromSchoolClass = $ref(await getAllStudentsFromSchoolClass(props.schoolClassId, props.periodId))
const router = useRouter()
const quasar = useQuasar()

const filter = $ref('')
const openUser = (row: UserView) => {
  router.push({
    name: "user",
    params: {
      id: row.id
    }
  })
}
const updateMainTeacher = async () =>
        quasar.dialog({
          component: ChangeMainTeacherDialog,
          componentProps: {
            title: `Смяна на класен ръководител на ${schoolClass.name} клас`,
            mainTeacher: schoolClass.mainTeacher,
            teacherOptions: await getAllTeachersThatDoNotHaveSchoolClass(props.schoolId, props.periodId)
          },
        }).onOk(async (payload) => {
          schoolClass.mainTeacher = payload.item as UserView
          await saveSchoolClass(schoolClass)
        })

const syncSchoolClassNumbersInClass = async () => {
  await syncNumbersInClass(props.schoolClassId).then(async e =>
      studentsFromSchoolClass = await getAllStudentsFromSchoolClass(props.schoolClassId, props.periodId))
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