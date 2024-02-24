<template>
  <div class="col-2"></div>
  <div class="col-8">
    <q-page class="page-content" padding>
      <div style="margin-top: 30px;">
        <div class="row">
          <div class="col-1">
            <q-select v-model="selectedSubject" :options="subjects"
                      :option-label="option => option.name" label="Предмет">
              <template v-slot:prepend>
                <q-icon name="menu_book"/>
              </template>
            </q-select>
          </div>
          <div class="col-10">
            <q-tabs dense>
              <q-route-tab label="Оценки" to="grades"/>
              <q-route-tab label="Отсъствия" to="absences"/>
              <q-route-tab label="Отзиви" to="feedbacks"/>
            </q-tabs>
          </div>
          <div class="col-1"></div>
          <div class="col-12">
            <router-view v-slot="{ Component }"
                         :evaluations="evaluationsOfTheSelectedSubject"
                         :subject="selectedSubject"
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
import {getAllSubjectsInSchoolClass, getSubjectsEvaluationsBySchoolClass} from "../../services/RequestService";
import {$ref} from "vue/macros";
import {watch} from "vue";
import {Subject} from "../../model/Subject";
import {SubjectWithEvaluationDTO} from "../../model/SubjectWithEvaluationDTO";

const props = defineProps<{
  schoolClassId: number,
  periodId: number,
  schoolId: number
}>()

const subjects = await getAllSubjectsInSchoolClass(props.schoolClassId, props.periodId, props.schoolId)
const selectedSubject = $ref(<Subject>{})
let evaluationsOfTheSelectedSubject = $ref(<SubjectWithEvaluationDTO[]>[])
watch(() => selectedSubject, async () => {
  evaluationsOfTheSelectedSubject = await getSubjectsEvaluationsBySchoolClass(selectedSubject.id, props.schoolClassId, props.periodId, props.schoolId)
  console.log(evaluationsOfTheSelectedSubject)
})


</script>
<style scoped>

</style>