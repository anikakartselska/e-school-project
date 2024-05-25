<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-table
              :columns="columns"
              :pagination="{rowsPerPage:20}"
              :rows="yearlyResults"
              no-data-label="Няма данни в таблицата"
              no-results-label="Няма резултати от вашето търсене"
              row-key="id"
              rows-per-page-label="Редове на страница"
              title="Годишни резултати"
      >
        <template v-slot:body-cell-edit="props">
          <q-td>
            <q-btn color="primary" dense flat icon="edit" @click="saveUpdateYearlyResults(props.row)">
            </q-btn>
          </q-td>
        </template>
        <template v-slot:body-cell-result="props">
          <q-td>
            <div :class="`text-white  ${getResultTypeColorClass(props.row?.yearlyResults?.result)}`"
                 style="text-align: center">
              {{ translationOfResultType[props.row?.yearlyResults?.result] }}
            </div>
          </q-td>
        </template>
        <template v-slot:body-cell-resultAfterTakingResitExams="props">
          <q-td>
            <div :class="`text-white ${getResultTypeColorClass(props.row?.yearlyResults?.resultAfterTakingResitExams)}`"
                 style="text-align: center">
              {{ translationOfResultType[props.row?.yearlyResults?.resultAfterTakingResitExams] }}
            </div>
          </q-td>
        </template>
      </q-table>
    </q-card>
  </q-page>
</template>


<script lang="ts" setup>
import {useRouter} from "vue-router";
import {useQuasar} from "quasar";
import {SchoolClass} from "../../model/SchoolClass";
import {$ref} from "vue/macros";
import {
    fetchAllYearlyResultsForSchoolClassPeriodAndSchool,
    saveYearlyResultsForStudent
} from "../../services/RequestService";
import {getResultTypeColorClass, StudentToYearlyResult, translationOfResultType} from "../../model/YearlyResults";
import YearlyResultsEditDialog from "./dialogs/yearly-results-edit-dialog.vue";

const props = defineProps<{
  periodId: number,
  schoolId: number,
  schoolClass: SchoolClass
}>()

const router = useRouter()
const quasar = useQuasar()

let yearlyResults = $ref(await fetchAllYearlyResultsForSchoolClassPeriodAndSchool(props.schoolId, props.periodId, props.schoolClass.id))

const saveUpdateYearlyResults = (studentToYearlyResults: StudentToYearlyResult) => quasar.dialog({
    component: YearlyResultsEditDialog,
    componentProps: {
        studentToYearlyResult: studentToYearlyResults
    },
}).onOk(async (payload) => {
    const updatedStudentToYearlyResults = payload.item as StudentToYearlyResult
    await saveYearlyResultsForStudent(updatedStudentToYearlyResults).then(r => {
                yearlyResults = yearlyResults.map(results => {
                    if (results.studentView.id == updatedStudentToYearlyResults.studentView.id) {
                        return updatedStudentToYearlyResults
                    } else {
                        return results
                    }
                })
            }
    )
})

const columns = [
  {
    name: "edit"
  },
  {
    name: "numberInClass",
    label: "Номер в клас",
    align: "center",
    field: (row: StudentToYearlyResult) => row?.studentView?.numberInClass != undefined ? `${row?.studentView?.numberInClass}` : '',
    sortable: true
  },
  {
    name: "student",
    label: "Име на ученика",
    align: "center",
    field: (row: StudentToYearlyResult) => row?.studentView?.firstName != undefined ? `${row?.studentView?.firstName} ${row?.studentView?.middleName} ${row?.studentView?.lastName}` : '',
    sortable: true
  },
  {
    name: "result",
    align: "center",
    label: "Резултат",
  },
  {
    name: "resultAfterTakingResitExams",
    align: "center",
    label: "Резултат след изпит"
  }
]

</script>

<style scoped>

</style>