<template>
  <q-card>
    <q-table
            :columns="columns"
            :filter="filter"
            :pagination="{rowsPerPage:10}"
            :rows="allTeachers"
            no-data-label="Няма данни в таблицата"
            no-results-label="Няма резултати от вашето търсене"
            row-key="id"
            rows-per-page-label="Редове на страница"
            title="Учители"
            @row-click.prevent="(_,row,__)=>openUser(row)"
    >
      <template v-slot:body-cell-edit="props">
        <q-td>
          <q-btn dense flat icon="open_in_new" @click="openUser(props.row)">
          </q-btn>
        </q-td>
      </template>
      <template v-slot:top-right="props">
        <q-btn color="primary" flat icon="edit" @click="goToUsersPage()">
        </q-btn>
        <q-input v-model="filter" debounce="300" dense outlined placeholder="Търсене">
          <template v-slot:append>
            <q-icon name="search"/>
          </template>
        </q-input>
      </template>
    </q-table>
  </q-card>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";

import {TeacherView, UserView} from "../../model/User";
import {fetchAllTeachers} from "../../services/RequestService";
import {useRouter} from "vue-router";
import {periodId, schoolId} from "../../model/constants";

const props = defineProps<{
  periodId: number,
  schoolId: number
}>()
const allTeachers = $ref(await fetchAllTeachers(props.schoolId, props.periodId))
const filter = $ref('')
const router = useRouter()
const columns = [

  {
    name: "firstName",
    label: "Име",
    align: "left",
    field: (row: TeacherView) => row.firstName,
    sortable: true
  },
  {
    name: "middleName",
    align: "left",
    label: "Презиме",
    field: (row: TeacherView) => row.middleName,
    sortable: true
  },
  {
    name: "lastName",
    align: "left",
    label: "Фамилия",
    field: (row: TeacherView) => row.lastName,
    sortable: true
  },
  {
    name: "username",
    align: "left",
    label: "Потребителско име",
    field: (row: TeacherView) => row.username,
    sortable: true
  },
  {
    name: "email",
    align: "left",
    label: "Е-майл",
    field: (row: TeacherView) => row.email,
    sortable: true
  },
  {
    name: 'qualifiedSubjects',
    align: "left",
    label: "Предмети, по-които е специализиран",
    field: (row: TeacherView) => row.qualifiedSubjects.join(','),
    sortable: true
  },
]

const openUser = (row: UserView) => {
  router.push({
    name: "user",
    params: {
      id: row.id
    }
  })
}

const goToUsersPage = async () => {
  await router.push(`/users/${periodId.value}/${schoolId.value}/all`)
}
</script>

<style scoped>

</style>