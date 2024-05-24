<template>
  <div class="bg-sms row">
      <div class="col-1"></div>
      <div class="col-10">
          <q-page class="page-content" padding>
              <q-stepper
                      v-model="step"
                      animated
                      color="primary"
                      header-nav
                      vertical
              >
                  <q-step
                          :done="step > 1"
                          :name="1"
                          icon="start"
                          title="Започване на учебната година"
                  >
              <span class="row">
        <q-select v-model="selectedPeriod"
                  :option-label="(option:SchoolPeriod) => `${option.startYear}/${option.endYear}`"
                  :options="schoolPeriodOptions"
                  dense
                  label="Учебна година"
                  style="width: 15vw"/>
                    <q-btn :disable="selectedPeriodStarted || selectedPeriod==null" class="q-ml-sm" color="primary"
                           icon="start"
                           @click="startYear()"
                    ></q-btn>
                    </span>
                      <div v-if="selectedPeriodStarted" class="text-negative">Учебната година вече е стартирана в
                          училището
                      </div>

                      <q-stepper-navigation>
                          <q-btn color="primary" label="Напред" @click="step = 2"/>
                      </q-stepper-navigation>
                  </q-step>
                  <q-step
                          :done="step > 2"
                          :name="2"
                          icon="person"
                          title="Менажиране на потребители"
                  >
                      <div>
                          Добавяне на потребители ръчно или чрез ексел файлове, редактиране и деактивиране
                      </div>
                      <q-btn color="primary" icon="open_in_new" label="Страница за менажиране на потребители"
                             outline @click="goToUsersPage()"></q-btn>
                      <q-stepper-navigation>
                          <q-btn color="primary" label="Напред" @click="step = 3"/>
                          <q-btn class="q-ml-sm" color="primary" flat label="Назад" @click="step = 1"/>
                      </q-stepper-navigation>
                  </q-step>
                  <q-step
                          :done="step > 3"
                          :name="3"
                          icon="square_foot"
                          title="Информация за учителите"
                  >
                      <teachers-table-component :period-id="props.periodId" :school-id="props.schoolId"/>
                      <q-stepper-navigation>
                          <q-btn color="primary" label="Напред" @click="step = 4"/>
                          <q-btn class="q-ml-sm" color="primary" flat label="Назад" @click="step = 2"/>
                      </q-stepper-navigation>

                  </q-step>
                  <q-step
                          :done="step > 4"
                          :name="4"
                          icon="group"
                          title="Информация за класовете"
                  >
                      <school-classes-with-shifts-component :period-id="props.periodId" :school-id="props.schoolId"/>
                      <q-stepper-navigation>
                          <q-btn color="primary" label="Напред" @click="step = 5"/>
                          <q-btn class="q-ml-sm" color="primary" flat label="Назад" @click="step = 3"/>
                      </q-stepper-navigation>
                  </q-step>
                  <q-step
                          :done="step > 5"
                          :name="5"
                          icon="group"
                          title="Учебни планове"
                  >
                      <school-classes-plans-component :period-id="periodId" :school-id="schoolId"/>
                      <q-stepper-navigation>
                          <q-btn color="primary" label="Напред" @click="step = 6"/>
                          <q-btn class="q-ml-sm" color="primary" flat label="Назад" @click="step = 4"/>
                      </q-stepper-navigation>
                  </q-step>
                  <q-step
                          :done="step > 6"
                          :name="6"
                          icon="group"
                          title="Учебна програма"
                  >
                      <school-program-component :period-id="props.periodId" :school-id="props.schoolId"/>
                      <q-stepper-navigation>
                          <q-btn color="primary" label="Напред" @click="step = 7"/>
                          <q-btn class="q-ml-sm" color="primary" flat label="Назад" @click="step = 5"/>
                      </q-stepper-navigation>
                  </q-step>

              </q-stepper>
              <q-separator class="q-mt-sm"/>
          </q-page>
    </div>
  </div>
</template>

<script lang="ts" setup>

import {useRouter} from "vue-router";
import {SchoolPeriod} from "../../model/SchoolPeriod";
import {getCurrentUser} from "../../services/LocalStorageService";
import {$ref} from "vue/macros";
import {watch} from "vue";
import {checkIfSchoolYearStarted, startSchoolYear} from "../../services/RequestService";
import {periodId, schoolId} from "../../model/constants";
import TeachersTableComponent from "../user/teachers-table-component.vue";
import SchoolClassesWithShiftsComponent from "../school-class/school-classes-with-shifts-component.vue";
import SchoolProgramComponent from "./components/school-program-component.vue";
import SchoolClassesPlansComponent from "./components/school-classes-plans-component.vue";

const props = defineProps<{
    periodId: number,
    schoolId: number
}>()

schoolId.value = props.schoolId.toString()
periodId.value = props.periodId.toString()

const route = useRouter()
const currentYear = (new Date()).getFullYear()
const currentUserPeriod = getCurrentUser().role.period
const step = $ref(1)
const schoolPeriodOptions = [currentUserPeriod, <SchoolPeriod>{
    startYear: currentUserPeriod.startYear + 1,
    endYear: currentUserPeriod.endYear + 1
}]
const selectedPeriod = $ref(null)
let selectedPeriodStarted = $ref(false)

watch(() => selectedPeriod, async () => {
    if (selectedPeriod != null) {
        selectedPeriodStarted = await checkIfSchoolYearStarted(selectedPeriod, props.schoolId)
    } else {
        selectedPeriodStarted = false
    }
})

const goToUsersPage = async () => {
    await route.push(`/users/${periodId.value}/${schoolId.value}/all`)
}

const startYear = async () => {
    await startSchoolYear(selectedPeriod!!, props.periodId)
            .then(r => {
                selectedPeriodStarted = true
            })
}
</script>

<style scoped>

.page-content {
    box-shadow: 0 30px 25px rgba(0, 0, 0, 0.4);
}

</style>