<template>
  <div class="bg-sms">
    <div class="col-2"></div>
    <div class="col-8">
      <q-page class="page-content" padding>
        <div style="margin-top: 30px;">
          <div class="row">
            <div class="col-8">
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
              <q-tabs dense>
                <q-route-tab label="Ученици" to="students"/>
                <q-route-tab label="Предмети" to="subjects"/>
                <q-route-tab label="Учебен план" to="plan"/>
                <q-route-tab label="Програма" to="program"/>
                <q-route-tab label="Оценки" to="grades"/>
                <q-route-tab label="Отзиви" to="remarks"/>
                <q-route-tab label="Отсъствия" to="absences"/>
              </q-tabs>
            </div>
            <div class="col-12">
              <router-view v-slot="{ Component }"
                           :periodId="props.periodId"
                           :schoolClass="schoolClass"
                           :schoolId="props.schoolId"
                           :studentsFromSchoolClass="studentsFromSchoolClass"
                           :subjectsTaughtInSchoolClassForYear="subjectsTaughtInSchoolClass">
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

import ChangeMainTeacherDialog from "./change-main-teacher-dialog.vue";
import {
    getAllStudentsFromSchoolClass,
    getAllSubjectsInSchoolClass,
    getAllTeachersThatDoNotHaveSchoolClass,
    getSchoolClassById,
    saveSchoolClass
} from "../../services/RequestService";
import {UserView} from "../../model/User";
import {$ref} from "vue/macros";
import {useQuasar} from "quasar";

const props = defineProps<{
  periodId: number,
  schoolId: number,
  schoolClassId: number
}>()

const schoolClass = $ref(await getSchoolClassById(props.schoolClassId, props.periodId))
const quasar = useQuasar()
let subjectsTaughtInSchoolClass = $ref(await getAllSubjectsInSchoolClass(props.schoolClassId, props.periodId, props.schoolId))
let studentsFromSchoolClass = $ref(await getAllStudentsFromSchoolClass(props.schoolClassId, props.periodId))

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