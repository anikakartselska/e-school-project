<template>
  <div class="bg-sms">
    <div class="col-2"></div>
    <div class="col-8">
      <q-page class="page-content" padding>
        <div style="margin-top: 30px;">
          <div class="row">
            <div class="col-9">
                <div class="text-h4">
                    Клас: {{ schoolClass.name }}
                </div>
                <div class="text-h5">
                    Класен ръководител:
                    <router-link :to="`/user/${schoolClass.mainTeacher.id}/${periodId}/${schoolId}`"
                                 active-class="text-negative" class="text-primary"
                                 exact-active-class="text-negative">
                        {{ schoolClass.mainTeacher.firstName }} {{ schoolClass.mainTeacher.lastName }}
                    </router-link>
                    <q-btn class="q-mr-xs" color="primary" flat icon="edit" round
                           @click="updateMainTeacher"/>
                </div>
                <q-tabs v-model="tab" dense>
                    <q-route-tab label="Ученици" name="students" to="students"/>
                    <q-route-tab label="Предмети" name="subjects" to="subjects"/>
                    <q-route-tab label="Учебен план" name="plan" to="plan"/>
                    <q-route-tab label="Програма" name="program" to="program"/>
                    <q-route-tab label="Оценки" name="grades" to="grades"/>
                    <q-route-tab label="Отзиви" name="remarks" to="remarks"/>
                    <q-route-tab label="Отсъствия" name="absences" to="absences"/>
                    <q-route-tab :name="AssignmentType.EXAMINATION" label="Контролни" to="examinations"/>
                    <q-route-tab :name="AssignmentType.HOMEWORK" label="Домашни работи" to="homeworks"/>
                    <q-route-tab :name="AssignmentType.EVENT" label="Събития" to="events"/>
                </q-tabs>
            </div>
            <div class="col-12">
                <router-view v-slot="{ Component }"
                             :assignments="assignments"
                             :periodId="props.periodId"
                             :schoolClass="schoolClass"
                             :schoolId="props.schoolId"
                             :studentsFromSchoolClass="studentsFromSchoolClass"
                             :subjectsTaughtInSchoolClassForYear="subjectsTaughtInSchoolClass"
                             :tab="tab">
                    <template v-if="Component">
                        <suspense>
                            <component :is="Component">
                            </component>
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

import ChangeMainTeacherDialog from "./dialogs/change-main-teacher-dialog.vue";
import {
    fetchAllAssignmentsForSchoolClassPeriodAndSchool,
    getAllStudentsFromSchoolClass,
    getAllSubjectsInSchoolClass,
    getAllTeachersThatDoNotHaveSchoolClass,
    getSchoolClassById,
    saveSchoolClass
} from "../../services/RequestService";
import {UserView} from "../../model/User";
import {$ref} from "vue/macros";
import {useQuasar} from "quasar";
import {AssignmentType} from "../../model/Assignments";

const props = defineProps<{
    schoolClassId: number
    periodId: number,
    schoolId: number
}>()

const tab = $ref('students')
const schoolClass = $ref(await getSchoolClassById(props.schoolClassId, props.periodId))
const quasar = useQuasar()
let subjectsTaughtInSchoolClass = $ref(await getAllSubjectsInSchoolClass(props.schoolClassId, props.periodId, props.schoolId))
let studentsFromSchoolClass = $ref(await getAllStudentsFromSchoolClass(props.schoolClassId, props.periodId))
let assignments = $ref(await fetchAllAssignmentsForSchoolClassPeriodAndSchool(props.schoolId, props.periodId, props.schoolClassId))
const updateMainTeacher = async () =>
        quasar.dialog({
            component: ChangeMainTeacherDialog,
            componentProps: {
                title: `Смяна на класен ръководител на ${schoolClass.name} клас`,
                mainTeacher: schoolClass.mainTeacher,
                teacherOptions: await getAllTeachersThatDoNotHaveSchoolClass(props.schoolId, props.periodId)
            },
        }).onOk(async (payload) => {
            schoolClass.mainTeacher = payload.item as UserView
          await saveSchoolClass(schoolClass)
        })


</script>

<style scoped>

</style>