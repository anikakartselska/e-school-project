<template>
  <q-page class="q-pa-sm bg-sms">
    <div class="row">
      <div class="col-1"/>
      <div class="text-h4 q-ml-md q-mb-md text-primary">Статистики</div>
    </div>
    <div class="row">
      <div class="col-1"/>
      <q-card
              class="my-card text-white col-3 q-ml-md q-mr-md"
              style="background: radial-gradient(circle, #35a2ff 0%, #014a88 100%)"
      >
        <q-card-section>
          <div class="text-h6">Среден успех</div>
          <div class="text-h1">{{ statistics.success?.toPrecision(2) }}</div>
        </q-card-section>
      </q-card>
      <q-card
              class="my-card text-white col-3 q-ml-md q-mr-md"
              style="height: 20vh;background: radial-gradient(circle, #7835ff 0%, #370188 100%)"
      >
        <q-card-section>
          <div class="text-h6">Брой оценки</div>
          <div class="text-h1">{{ statistics.grades }}</div>
        </q-card-section>
      </q-card>
      <q-card
              class="my-card text-white col-3 q-ml-md q-mr-md"
              style="background: radial-gradient(circle, #ff353c 0%, #880105 100%)"
      >
        <q-card-section>
          <div class="text-h6">Брой отсъствия</div>
          <div class="text-h1">{{ statistics.absences }}</div>
        </q-card-section>
      </q-card>
    </div>
    <div class="row q-mt-xl">
      <div class="col-1"/>
      <q-card
              class="my-card text-white col-3 q-ml-md q-mr-md"
              style="background: radial-gradient(circle, #35ff75 0%, #1e8801 100%)"
      >
        <q-card-section>
          <div class="text-h6">Брой отзиви</div>
          <div class="text-h1">{{ statistics.feedback }}</div>
        </q-card-section>
      </q-card>
      <q-card
              class="my-card text-white col-3 q-ml-md q-mr-md"
              style="background: radial-gradient(circle, #ff8235 0%, #884901 100%)"
      >
        <q-card-section>
          <div class="text-h6">Брой събития</div>
          <div class="text-h1">{{ statistics.events }}</div>
        </q-card-section>
      </q-card>
      <q-card
              class="my-card text-white col-3 q-ml-md q-mr-md"
              style="height: 20vh;
            background: radial-gradient(circle, #ffe435 0%, #7b8801 100%)"
      >
        <q-card-section>
          <div class="text-h6">Брой изпитвания</div>
          <div class="text-h1">{{ statistics.examinations }}</div>
        </q-card-section>
      </q-card>
    </div>
    <q-separator class="q-ma-lg"/>
    <div class="row">
      <div class="col-1"/>
      <div class="col-5">
        <q-table
                :columns="schoolClassesColumns"
                :rows="statistics.schoolClassToAverageGrade"
                hide-pagination
                no-data-label="Няма данни в таблицата"
                no-results-label="Няма резултати от вашето търсене"
                row-key="first"
                rows-per-page-label="Редове на страница"
                style="height: 35vh"
                title="Класация по класове"
                virtual-scroll
        >
          <template v-slot:body-cell-schoolClass="props">
            <q-td>
              <router-link
                      :to="`/school-class/${periodId}/${schoolId}/${props.row.first.id}/grades`"
                      active-class="text-negative" class="text-primary"
                      exact-active-class="text-negative">
                {{ props.row.first.name }}
              </router-link>
            </q-td>
          </template>
          <template v-slot:body-cell-mainTeacher="props">
            <q-td>
              <router-link
                      :to="`/user/${props.row.first.mainTeacher.id}/${periodId}/${schoolId}`"
                      active-class="text-negative" class="text-primary"
                      exact-active-class="text-negative">
                {{ props.row.first.mainTeacher.firstName }} {{ props.row.first.mainTeacher.lastName }}
              </router-link>
            </q-td>
          </template>
        </q-table>
      </div>
      <div class="col q-ml-md">
        <q-table
                :columns="studentsColumns"
                :rows="statistics.studentToAverageGrade"
                hide-pagination
                no-data-label="Няма данни в таблицата"
                no-results-label="Няма резултати от вашето търсене"
                row-key="first"
                rows-per-page-label="Редове на страница"
                style="height: 35vh"
                title="Класация на учениците"
                virtual-scroll
        >
          <template v-slot:body-cell-student="props">
            <q-td>
              <router-link
                      :to="`/student-diary/${props.row.first.schoolClassId}/${props.row.first.id}/${periodId}/${schoolId}/grades`"
                      active-class="text-negative" class="text-primary"
                      exact-active-class="text-negative">
                {{ props.row.first.firstName }} {{ props.row.first.lastName }}
              </router-link>
            </q-td>
          </template>
          <template v-slot:body-cell-schoolClass="props">
            <q-td>
              <router-link
                      :to="`/school-class/${periodId}/${schoolId}/${props.row.first.schoolClassId}/grades`"
                      active-class="text-negative" class="text-primary"
                      exact-active-class="text-negative">
                {{ props.row.first.schoolClassName }}
              </router-link>
            </q-td>
          </template>
        </q-table>
      </div>
      <div class="col-1"/>
    </div>
  </q-page>
</template>

<script lang="ts" setup>
import {fetchStatisticsForSchool} from "../../services/RequestService";
import {$computed} from "vue/macros";
import {Pair} from "../../model/Pair";
import {SchoolClass} from "../../model/SchoolClass";
import {StudentView} from "../../model/User";

const props = defineProps<{
  periodId: number
  schoolId: number
}>()


const statistics = await fetchStatisticsForSchool(props.schoolId, props.periodId)

const schoolClassesColumns = $computed(() => [
  {
    name: 'edit',
    headerClasses: 'q-table--col-auto-width'
  },
  {
    name: "schoolClass",
    label: "Клас",
    align: "left",
    field: (row: Pair<SchoolClass, number | null>) => row.first.name,
    sortable: true
  },
  {
    name: "mainTeacher",
    label: "Класен ръководител",
    align: "left",
    field: (row: Pair<SchoolClass, number | null>) => `${row.first.mainTeacher.firstName} ${row.first.mainTeacher.lastName}`,
    sortable: true
  },
  {
    name: "second",
    label: "Среден успех",
    align: "left",
    field: (row: Pair<SchoolClass, number | null>) => row.second,
    sortable: true
  }
])

const studentsColumns = $computed(() => [
  {
    name: 'edit',
    headerClasses: 'q-table--col-auto-width'
  },
  {
    name: "student",
    label: "Ученик",
    align: "left",
    field: (row: Pair<StudentView, number | null>) => `${row.first.firstName} ${row.first.lastName}`,
    sortable: true
  },
  {
    name: "schoolClass",
    label: "Клас",
    align: "left",
    field: (row: Pair<StudentView, number | null>) => `${row.first.schoolClassName}`,
    sortable: true
  },
  {
    name: "second",
    label: "Среден успех",
    align: "left",
    field: (row: Pair<StudentView, number | null>) => row.second,
    sortable: true
  }
])
</script>

<style scoped>

</style>