<template>
  <q-page class="q-pa-sm">
    <q-card>
      <q-table
              title="Потребители"
              :rows="userData"
              :columns="columns"
              row-key="firstName"
              :filter="filter"
              :pagination="{rowsPerPage:10}"
              rows-per-page-label="Редове на страница"
              no-results-label="Няма резултати от вашето търсене"
              no-data-label="Няма данни в таблицата"
              @row-click.prevent="(_,row,__)=>openUser(row)"
      >
        <template v-slot:body-cell-edit="props">
          <q-td>
            <q-btn icon="open_in_new" dense flat @click="openUser(props.row)">
            </q-btn>
          </q-td>
        </template>
        <template v-slot:top-right="props">
          <q-btn outline color="primary" label="Добави нов" class="q-mr-xs"/>

          <q-input outlined dense debounce="300" v-model="filter" placeholder="Търсене">
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
import {getAllUsersBySchoolId} from "../services/RequestService";
import {periodId, schoolId} from "../model/constants";
import {$ref} from "vue/macros";
import {UserView} from "../model/User";
import {useRouter} from "vue-router";

const router = useRouter()
const userData: UserView[] = $ref(await getAllUsersBySchoolId(schoolId))

const openUser = async (row: UserView) => {
    await router.push({
        name: "user",
        params: {
            id: row.id,
            periodId: periodId,
            schoolId: schoolId
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
    field: 'firstName',
    sortable: true
  },
  {
    name: "middleName",
    align: "left",
    label: "Презиме",
    field: 'middleName',
    sortable: true
  },
  {
    name: "lastName",
    align: "left",
    label: "Фамилия",
    field: 'lastName',
    sortable: true
  },
  {
    name: "username",
    align: "left",
    label: "Потребителско име",
    field: 'username',
    sortable: true
  },
  {
    name: "email",
    align: "left",
    label: "Е-майл",
    field: 'email',
    sortable: true
  },
  {
    name: "role",
    align: "left",
    label: "Роля",
    field: 'role',
    sortable: true
  }
]

</script>

<style scoped>

</style>