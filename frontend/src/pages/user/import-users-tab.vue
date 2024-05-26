<template>
  <q-page class="q-pa-sm bg-sms">
      <q-card class="row">
          <q-card-section class="col-3">
              <single-file-picker
                      v-model="adminFile"
                      accept-file-format=".xlsx"
                      class="q-mt-md q-mr-sm"
                      label="Файл с админи"
                      @action-button-event="uploadExcelFile(SchoolRole.ADMIN,adminFile)"
              />
              <single-file-picker
                      v-model="teachersFile"
                      accept-file-format=".xlsx"
                      class="q-mt-md q-mr-sm"
                      label="Файл с учители"
                      @action-button-event="uploadExcelFile(SchoolRole.TEACHER,teachersFile)"
              />
              <single-file-picker
                      v-model="parentsFile"
                      accept-file-format=".xlsx"
                      class="q-mt-md q-mr-sm"
                      label="Файл с родители"
                      @action-button-event="uploadExcelFile(SchoolRole.PARENT,parentsFile)"
              />
              <single-file-picker
                      v-model="studentsFile"
                      accept-file-format=".xlsx"
                      class="q-mt-md q-mr-sm"
                      label="Файл с ученици"
                      @action-button-event="uploadExcelFile(SchoolRole.STUDENT,studentsFile)"
              />
          </q-card-section>
          <q-separator class="q-ml-xl q-mr-xl" vertical/>
          <q-card-section class="col-8 q-ml-md">
              <q-table
                      :columns="columns"
                      :filter="filter"
                      :pagination="{rowsPerPage:10}"
                      :rows="newlyAddedUsers"
                      class="fit full-width"
                      no-data-label="Няма данни в таблицата"
                      no-results-label="Няма резултати от вашето търсене"
                      row-key="id"
                      rows-per-page-label="Редове на страница"
                      title="Новодобавени или модифицирани потребители"
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
                          <span v-if="props.row.status ===RequestStatus.REJECTED"
                                class="text-negative">ДЕАКТИВИРАН</span>
                          <span v-if="props.row.status ===RequestStatus.PENDING" class="text-primary">ИЗЧАКВАЩ</span>
                      </q-td>
                  </template>

                  <template v-slot:top-right="props">
                      <q-input v-model="filter" debounce="300" dense outlined placeholder="Търсене">
                          <template v-slot:append>
                              <q-icon name="search"/>
                          </template>
                      </q-input>
                      <q-btn class="q-ml-md" color="primary" icon="fact_check" outline round @click="goToRequests()">
                          <q-tooltip>Отвори заявките</q-tooltip>
                      </q-btn>
                  </template>
              </q-table>
          </q-card-section>
      </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {useRouter} from "vue-router";
import {uploadUsersExcelFile} from "../../services/RequestService";
import {periodId, schoolId} from "../../model/constants";
import {SchoolRole, UserView} from "../../model/User";
import {confirmActionPromiseDialog, confirmActionPromiseDialogWithCancelButton} from "../../utils";
import SingleFilePicker from "../common/single-file-picker.vue";
import {$ref} from "vue/macros";
import {RequestStatus} from "../../model/RequestStatus";

const props = defineProps<{
    periodId: number,
    schoolId: number
}>()
let adminFile = $ref(null)
let teachersFile = $ref(null)
let studentsFile = $ref(null)
let parentsFile = $ref(null)

const router = useRouter()
let newlyAddedUsers = $ref(<UserView[]>[])
const uploadExcelFile = async (schoolRole: SchoolRole, file: File) => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
    await uploadUsersExcelFile(file, periodId.value, schoolId.value, schoolRole).then(async response => {
        if (response.status == 200) {
            newlyAddedUsers = response.data
            await confirmActionPromiseDialogWithCancelButton("Успешна операция",
                    "За всеки потребител от ексела са създадени заявки за регистрация и роля, които друг админ трябва да одобри")
            adminFile = null
            teachersFile = null
            studentsFile = null
            parentsFile = null
        }
    })
}

const goToRequests = async () => {
    await router.push(`/requests/${props.periodId}/${props.schoolId}/user-requests`)
}
const openUser = (row: UserView) => {
    router.push({
        name: "user",
        params: {
            id: row.id
        }
    })
}

const getRowColor = (row: UserView) => {
    if (row.status === RequestStatus.REJECTED) {
        return "bg-red-1"
    }
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