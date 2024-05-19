<template>
  <div class="bg-sms row">
      <div class="col-2"></div>
      <div class="col-8">
          <q-page class="page-content" padding>
        <span class="text-h6">
            Започване на учебна година
             </span>
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
              <div v-if="selectedPeriodStarted" class="text-negative">Учебната година вече е стартирана в училището
              </div>

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

const props = defineProps<{
    periodId: number,
    schoolId: number
}>()

schoolId.value = props.schoolId.toString()
periodId.value = props.periodId.toString()

const route = useRouter()
const currentYear = (new Date()).getFullYear()
const currentUserPeriod = getCurrentUser().role.period
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