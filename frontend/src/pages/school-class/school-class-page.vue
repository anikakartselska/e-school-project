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
import {getAllStudentsFromSchoolClass, getSchoolClassById} from "../../services/RequestService";
import {StudentView, UserView} from "../../model/User";
import {useRouter} from "vue-router";

const props = defineProps<{
  periodId: number,
  schoolId: number,
  schoolClassId: number
}>()
console.log(props)
const schoolClass = $ref(await getSchoolClassById(props.schoolClassId, props.schoolClassId))
const studentsFromSchoolClass = $ref(await getAllStudentsFromSchoolClass(props.schoolClassId, props.periodId))
const router = useRouter()

const filter = $ref('')
const openUser = (row: UserView) => {
  router.push({
    name: "user",
    params: {
      id: row.id
    }
  })
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
    name: "numberInCass",
    align: "left",
    label: "Номер в клас",
    field: (row: StudentView) => row.numberInCass,
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