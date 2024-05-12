<template>
  <div class="bg-sms">
    <div class="col-2"></div>
    <div class="col-8">
      <q-page class="page-content" padding>
        <div>
          <div class="text-h4">
            Ученик:
            <router-link :to="`/user/${student.id}/${periodId}/${schoolId}`"
                         active-class="text-negative" class="text-primary"
                         exact-active-class="text-negative">
              {{ student.firstName }} {{ student.lastName }}
            </router-link>
          </div>
          <div class="text-h5">
            Номер в клас:
            <span class="text-primary">
            {{ student.numberInClass }}
          </span>
          </div>
          <div class="text-h5">
            Класен ръководител:
            <router-link :to="`/user/${schoolClass.mainTeacher.id}/${periodId}/${schoolId}`"
                         active-class="text-negative" class="text-primary"
                         exact-active-class="text-negative">
              {{ schoolClass.mainTeacher.firstName }} {{ schoolClass.mainTeacher.lastName }}
            </router-link>
          </div>
          <div class="text-h5">
            Клас:
            <router-link :to="`/school-class/${periodId}/${schoolId}/${schoolClass.id}/students`"
                         active-class="text-negative" class="text-primary"
                         exact-active-class="text-negative">
              {{ schoolClass.name }}
            </router-link>
          </div>
          <div class="row">
            <div class="col-12">
              <q-tabs v-model="tab" dense>
                <q-route-tab label="Оценки" name="grades" to="grades"/>
                <q-route-tab label="Отсъствия" name="absences" to="absences"/>
                <q-route-tab label="Отзиви" name="feedbacks" to="feedbacks"/>
                <q-route-tab label="Програма" name="program" to="program"/>
                <q-route-tab :name="AssignmentType.EXAMINATION" label="Контролни" to="examinations"/>
                <q-route-tab :name="AssignmentType.HOMEWORK" label="Домашни работи" to="homeworks"/>
                <q-route-tab :name="AssignmentType.EVENT" label="Събития" to="events"/>
              </q-tabs>
            </div>
            <div class="col-12">
              <router-view v-slot="{ Component }"
                           :assignments="assignments"
                           :evaluations="subjectWithEvaluationDTO"
                           :periodId="periodId"
                           :schoolClass="schoolClass"
                           :schoolId="schoolId"
                           :tab="tab"

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
  </div>
</template>

<script lang="ts" setup>
import {
    fetchAllAssignmentsForSchoolClassPeriodAndSchool,
    fetchStudentById,
    getSchoolClassById,
    getStudentsSubjectsAndEvaluations
} from "../../services/RequestService";
import {$ref} from "vue/macros";
import {AssignmentType} from "../../model/Assignments";

const props = defineProps<{
  schoolClassId: number,
  studentId: number,
  periodId: number,
  schoolId: number
}>()
const tab = $ref('grades')
const student = $ref(await fetchStudentById(props.studentId,
        props.schoolClassId,
        props.periodId))
const schoolClass = $ref(await getSchoolClassById(props.schoolClassId, props.periodId))
let subjectWithEvaluationDTO = $ref(await getStudentsSubjectsAndEvaluations(props.studentId, props.periodId, props.schoolId, props.schoolClassId))
const assignments = $ref(await fetchAllAssignmentsForSchoolClassPeriodAndSchool(props.schoolId, props.periodId, props.schoolClassId))
</script>

<style scoped>

</style>