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
          <single-file-picker
                  v-model="userFile"
                  accept-file-format=".xlsx"
                  class="q-mt-md"
                  label="Файл с админи"
                  @action-button-event="uploadExcelFile"
          />
          <q-btn class="q-mr-xs" color="primary" label="Добави нов" outline/>
          <q-btn
                  color="primary"
                  icon-right="archive"
                  label="Export to csv"
                  no-caps
          />
          <q-input v-model="filter" debounce="300" dense outlined placeholder="Търсене">
            <template v-slot:append>
              <q-icon name="search"/>
            </template>
          </q-input>
        </template>
      </q-table>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {SchoolRole, UserView} from "../../model/User";
import {useRouter} from "vue-router";
import {uploadUsersExcelFile} from "../../services/RequestService";
import {periodId, schoolId} from "../../model/constants";
import {getCurrentUser} from "../../services/LocalStorageService";
import {confirmActionPromiseDialogWithCancelButton} from "../../utils";
import SingleFilePicker from "../common/single-file-picker.vue";

const props = defineProps<{
  users: UserView[]
}>()

const router = useRouter()
const students = props.users.filter(user => user.roles.includes(SchoolRole.ADMIN))
const userFile = $ref(null)
const currentUserId = getCurrentUser().id

const openUser = (row: UserView) => {
  router.push({
    name: "user",
    params: {
      id: row.id
    }
  })
}
const uploadExcelFile = async () => {
  console.log(userFile)
  await uploadUsersExcelFile(userFile, periodId.value, schoolId.value, SchoolRole.ADMIN, currentUserId).then(async response => {
    if (response.status == 200) {
      await confirmActionPromiseDialogWithCancelButton("Успешна операция",
              "За всеки потребител от ексела са създадени заявки за регистрация и роля, които друг админ трябва да одобри")
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