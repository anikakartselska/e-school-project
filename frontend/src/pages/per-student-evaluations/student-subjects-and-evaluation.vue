<template>
  <div class="col-2"></div>
  <div class="col-8">
    <q-page class="page-content" padding>
      <div style="margin-top: 30px;">
        <div class="row">
          <div class="col-12">
            <q-tabs dense>
              <q-route-tab label="Оценки" to="grades"/>
              <q-route-tab label="Отсъствия" to="absences"/>
              <q-route-tab label="Отзиви" to="feedbacks"/>
            </q-tabs>
          </div>
          <div class="col-12">
            <router-view v-slot="{ Component }"
                         :evaluations="subjectWithEvaluationDTO"
            >
              <template v-if="Component">
                <suspense>
                  <component :is="Component"></component>
                  <template #fallback>
                    <div class="centered-div">
                      <q-spinner
                              :thickness="2"
                              color="primary"
                              size="5.5em"
                      />
                    </div>
                  </template>
                </suspense>
              </template>
            </router-view>
          </div>
        </div>
      </div>
    </q-page>
  </div>
</template>

<script lang="ts" setup>
import {getStudentsSubjectsAndEvaluations} from "../../services/RequestService";
import {$ref} from "vue/macros";

const props = defineProps<{
  schoolClassId: number,
  studentId: number,
  periodId: number,
  schoolId: number
}>()

let subjectWithEvaluationDTO = $ref(await getStudentsSubjectsAndEvaluations(props.studentId, props.periodId, props.schoolId, props.schoolClassId))

</script>

<style scoped>

</style>