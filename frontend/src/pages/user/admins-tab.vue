<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-table
              :columns="columns"
              :filter="filter"
              :pagination="{rowsPerPage:10}"
              :rows="students"
              no-data-label="Няма данни в таблицата"
              no-results-label="Няма резултати от вашето търсене"
              row-key="id"
              rows-per-page-label="Редове на страница"
              title="Админи"
              @row-click.prevent="(_,row,__)=>openUser(row)"
      >
        <template v-slot:body-cell-edit="props">
          <q-td>
            <q-btn dense flat icon="open_in_new" @click="openUser(props.row)">
            </q-btn>
          </q-td>
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
import {SchoolRole, UserView} from "../../model/User";
import {useRouter} from "vue-router";

const props = defineProps<{
  users: UserView[]
}>()

const router = useRouter()
const students = props.users.filter(user => user.roles.includes(SchoolRole.ADMIN))

const openUser = (row: UserView) => {
  router.push({
    name: "user",
    params: {
      id: row.id
    }
  })
}

const filter = $ref('')
const columns = [
  {name: 'edit'},
  {
    name: "firstName",
    label: "Име",
    align: "left",
    field: (row: UserView) => row.firstName,
    sortable: true
  },
  {
    name: "middleName",
    align: "left",
    label: "Презиме",
    field: (row: UserView) => row.middleName,
    sortable: true
  },
  {
    name: "lastName",
    align: "left",
    label: "Фамилия",
    field: (row: UserView) => row.lastName,
    sortable: true
  },
  {
    name: "username",
    align: "left",
    label: "Потребителско име",
    field: (row: UserView) => row.username,
    sortable: true
  },
  {
    name: "email",
    align: "left",
    label: "Е-майл",
    field: (row: UserView) => row.email,
    sortable: true
  },
  {
    name: "role",
    align: "left",
    label: "Роля",
    field: (row: UserView) => row.roles.join(","),
    sortable: true
  }
]

</script>

<style scoped>

</style>