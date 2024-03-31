<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-table
              :columns="columns"
              :filter="filter"
              :pagination="{rowsPerPage:10}"
              :rows="allUsers"
              no-data-label="Няма данни в таблицата"
              no-results-label="Няма резултати от вашето търсене"
              row-key="id"
              rows-per-page-label="Редове на страница"
              title="Админи"
              @row-click.prevent="(_,row,__)=>openUser(row)"
      >
          <template v-slot:body-cell-edit="props">
              <q-td :class="getRowColor(props.row)">
                  <q-btn dense flat icon="open_in_new" @click="openUser(props.row)">
                  </q-btn>
              </q-td>
          </template>
          <template v-slot:body-cell="props">
              <q-td
                      :class="getRowColor(props.row)"
                      :props="props"
              >
                  {{ props.value }}
              </q-td>
          </template>
          <template v-slot:body-cell-status="props">
              <q-td :class="getRowColor(props.row)">
                  <span v-if="props.row.status ===RequestStatus.REJECTED" class="text-negative">ДЕАКТИВИРАН</span>
                  <span v-if="props.row.status ===RequestStatus.PENDING" class="text-primary">ИЗЧАКВАЩ</span>
              </q-td>
          </template>
          <template v-slot:body-cell-roles="props">
              <q-td>
              </q-td>
          </template>
          <template v-slot:top="props">
              <q-input v-model="filter" debounce="300" dense outlined placeholder="Търсене">
                  <template v-slot:append>
                      <q-icon name="search"/>
                  </template>
              </q-input>
              <q-space/>
              <single-file-picker
                      v-model="userFile"
                      accept-file-format=".xlsx"
                      class="q-mt-md q-mr-sm"
                      label="Файл с админи"
                      @action-button-event="uploadExcelFile"
              />
              <q-btn class="q-mr-xs" color="primary" icon="person_add" outline round @click="addNewUser()">
                  <q-tooltip>Добави нов</q-tooltip>
              </q-btn>
              <q-btn
                      color="primary"
                      icon="archive"
                      round
              >
                  <q-tooltip> Изтегли ексел с админи</q-tooltip>

              </q-btn>
          </template>
      </q-table>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {SchoolRole, User, UserView} from "../../model/User";
import {useRouter} from "vue-router";
import {getAllUsersBySchoolIdAndPeriodId, uploadUsersExcelFile} from "../../services/RequestService";
import {periodId, schoolId} from "../../model/constants";
import {getCurrentUser, mapUserToUserView} from "../../services/LocalStorageService";
import {confirmActionPromiseDialogWithCancelButton} from "../../utils";
import SingleFilePicker from "../common/single-file-picker.vue";
import {RequestStatus} from "../../model/RequestStatus";
import {useQuasar} from "quasar";
import {defineAsyncComponent} from "vue";

const props = defineProps<{
    periodId: number,
    schoolId: number
}>()

const router = useRouter()
const quasar = useQuasar()
let allUsers = $ref(await getAllUsersBySchoolIdAndPeriodId(props.schoolId, props.periodId))
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

const getRowColor = (row: UserView) => {
    if (row.status === RequestStatus.REJECTED) {
        return "bg-red-1"
    }
}

const addNewUser = async () => quasar.dialog({
    component: await defineAsyncComponent(() => import('./add-user-dialog.vue')).__asyncLoader(),
}).onOk(async (payload) => {
    const updatedOrAddedUser = <User>payload.item
    if (allUsers.find(user => user.id == updatedOrAddedUser.id) !== null) {
        allUsers = [...allUsers, mapUserToUserView(updatedOrAddedUser, true)]
    } else {
        allUsers = [...allUsers].map(user => {
            if (user.id == updatedOrAddedUser.id) {
                return mapUserToUserView(updatedOrAddedUser, true)
            } else {
                return user
            }
        })
    }
})

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
        field: (row: UserView) => {
            if (row.roles.length == 0) {
                return "НЯМА АКТИВНИ РОЛИ"
            } else {
                return row.roles.join(",")
            }
        },
        classes: (row: UserView) => {
            if (row.roles.length == 0) {
                return "text-italic"
            } else {
                return ""
            }
        },
        sortable: true
    },
    {
        name: 'status',
        align: "left",
        label: "Статус",
        field: (row: UserView) => row.status,
        sortable: true
    },
]

</script>

<style scoped>

</style>