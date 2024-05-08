<template>
  <div class="bg-sms">
    <div class="col-2"></div>
    <div class="col-8">
      <q-page class="page-content" padding>
        <div style="margin-top: 30px;">
          <div class="row">
            <div class="col-12" style="text-align: center">
              <!--                            <div class="text-h4">-->
              <!--                                Клас: {{ schoolClass.name }}-->
              <!--                            </div>-->
              <!--                            <div class="text-h5">-->
              <!--                                Класен ръководител:-->
              <!--                                <router-link :to="`/user/${schoolClass.mainTeacher.id}/${periodId}/${schoolId}`"-->
              <!--                                             active-class="text-negative" class="text-primary"-->
              <!--                                             exact-active-class="text-negative">-->
              <!--                                    {{ schoolClass.mainTeacher.firstName }} {{ schoolClass.mainTeacher.lastName }}-->
              <!--                                </router-link>-->
              <!--                                <q-btn class="q-mr-xs" color="primary" flat icon="edit" round-->
              <!--                                       @click="updateMainTeacher"/>-->
              <!--                            </div>-->
              <q-tabs dense>
                <q-route-tab label="Урок" to="lesson"/>
                <q-route-tab label="Оценки" to="grades"/>
                <q-route-tab label="Отзиви" to="feedbacks"/>
                <q-route-tab label="Отсъствия" to="absences"/>
              </q-tabs>
            </div>
            <div class="col-12">
              <router-view v-slot="{ Component }"
                           :evaluations="studentWithEvaluationDTO"
                           :lesson="lesson"
                           :subject="lesson.subject"
                           :periodId="periodId"
                           :schoolId="schoolId"
              >
              </router-view>
            </div>
          </div>
        </div>
      </q-page>
    </div>
  </div>
</template>

<script lang="ts" setup>

import {fetchSchoolLessonById, getEvaluationForSubjectAndSchoolClass} from "../../services/RequestService";
import {$ref} from "vue/macros";
import {useQuasar} from "quasar";

const props = defineProps<{
  periodId: number,
  schoolId: number,
  schoolLessonId: number
}>()

const lesson = $ref(await fetchSchoolLessonById(props.schoolLessonId))
const quasar = useQuasar()
let studentWithEvaluationDTO = $ref(await getEvaluationForSubjectAndSchoolClass(lesson.subject.id, props.periodId, props.schoolId, lesson.schoolClass.id, props.schoolLessonId))


</script>

<style scoped>

</style>