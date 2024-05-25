<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <q-card-section>
        <span class="text-primary text-h6">
            Резултат:
        </span>
        <span :class="`text-white text-h6 ${getResultTypeColorClass(yearlyResults?.result)}`">
    {{ translationOfResultType[yearlyResults?.result] }}
</span>
      </q-card-section>
      <q-card-section>
        <span class="text-primary text-h6">
            Резултат след изпит:
        </span>
        <span :class="getResultTypeColorClass(yearlyResults?.resultAfterTakingResitExams)"
              class="text-white text-h6">
    {{ translationOfResultType[yearlyResults?.resultAfterTakingResitExams] }}
</span>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {useRouter} from "vue-router";
import {useQuasar} from "quasar";
import {$ref} from "vue/macros";
import {fetchYearlyResultsForStudentPeriodAndSchool} from "../../services/RequestService";
import {getResultTypeColorClass, translationOfResultType, YearlyResults} from "../../model/YearlyResults";

const props = defineProps<{
  periodId: number,
  schoolId: number,
  studentId: number
}>()

const router = useRouter()
const quasar = useQuasar()

const yearlyResults = $ref<YearlyResults | null>(await fetchYearlyResultsForStudentPeriodAndSchool(props.schoolId, props.periodId, props.studentId))

debugger
</script>

<style scoped>

</style>