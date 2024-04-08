<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-table
              :columns="columns"
              :filter="filter"
              :pagination="{rowsPerPage:10}"
              :rows="schoolClasses"
              no-data-label="Няма данни в таблицата"
              no-results-label="Няма резултати от вашето търсене"
              row-key="id"
              rows-per-page-label="Редове на страница"
              title="Класове"
              @row-click.prevent="(_,row,__)=>openSchoolClass(row)"
      >
        <template v-slot:body-cell-edit="props">
          <q-td>
            <q-btn dense flat icon="open_in_new" @click="openSchoolClass(props.row)">
            </q-btn>
          </q-td>
        </template>
        <template v-slot:body-cell-main-teacher="props">
            <q-td :props="props">
                <router-link :to="`/user/${props.value.id}/${periodId}/${schoolId}`"
                             active-class="text-negative" class="text-primary" exact-active-class="text-negative">
                    {{ props.value.firstName }} {{ props.value.lastName }}
                    <q-tooltip>
                        Кликни за повече детайли
                    </q-tooltip>
                </router-link>
            </q-td>
        </template>
          <template v-slot:top-right="props">
              <q-btn class="q-ml-md" color="primary" icon="person_add" outline round @click="addNewSchoolClass">
                  <q-tooltip>Добави нов</q-tooltip>
              </q-btn>
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
import {useRouter} from "vue-router";
import {
    getAllTeachersThatDoNotHaveSchoolClass,
    getSchoolClassesFromSchool,
    saveSchoolClass
} from "../../services/RequestService";
import {SchoolClass} from "../../model/SchoolClass";
import {periodId} from "../../model/constants";
import {useQuasar} from "quasar";
import SchoolClassAddDialog from "./school-class-add-dialog.vue";

const props = defineProps<{
    periodId: number,
    schoolId: number
}>()
const quasar = useQuasar()
const router = useRouter()
const schoolClasses = $ref(await getSchoolClassesFromSchool(props.schoolId, props.periodId))
const openSchoolClass = (schoolClass: SchoolClass) => {
    router.push({
        name: "school-class",
        params: {
            periodId: props.periodId,
            schoolId: props.schoolId,
            schoolClassId: schoolClass.id
        }
    })
}

const addNewSchoolClass = async () =>
        quasar.dialog({
            component: SchoolClassAddDialog,
            componentProps: {
                schoolClass: <SchoolClass>{schoolPeriodId: props.periodId, schoolId: props.schoolId},
                alreadyExistingSchoolClasses: schoolClasses,
                teacherOptions: await getAllTeachersThatDoNotHaveSchoolClass(props.schoolId, props.periodId)
            },
        }).onOk(async (payload) => {
            const schoolClassToStudentsFile = payload.item as Pair<SchoolClass, Blob>
            await saveSchoolClass(schoolClassToStudentsFile.first, schoolClassToStudentsFile.second).then(e => {
                        schoolClassToStudentsFile.first.id = e.data
                        schoolClasses.push(schoolClassToStudentsFile.first)
                    }
            )

        })

const filter = $ref('')
const columns = [
  {name: 'edit'},
  {
    name: "name",
    label: "Клас",
    align: "left",
    field: (row: SchoolClass) => row.name,
    sortable: true
  },
  {
    name: "main-teacher",
    align: "left",
    label: "Класен Ръководител",
    field: (row: SchoolClass) => row.mainTeacher,
    sortable: true
  }
]

</script>

<style scoped>

</style>