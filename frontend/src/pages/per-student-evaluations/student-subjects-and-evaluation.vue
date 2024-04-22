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
                        <router-link :to="`/school-class/${schoolClass.id}/${periodId}/${schoolId}/students`"
                                     active-class="text-negative" class="text-primary"
                                     exact-active-class="text-negative">
                            {{ schoolClass.name }}
                        </router-link>
                    </div>
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
    </div>
</template>

<script lang="ts" setup>
import {fetchStudentById, getSchoolClassById, getStudentsSubjectsAndEvaluations} from "../../services/RequestService";
import {$ref} from "vue/macros";

const props = defineProps<{
    schoolClassId: number,
    studentId: number,
    periodId: number,
    schoolId: number
}>()
const student = $ref(await fetchStudentById(props.studentId,
        props.schoolClassId,
        props.periodId))
const schoolClass = $ref(await getSchoolClassById(props.schoolClassId, props.periodId))
let subjectWithEvaluationDTO = $ref(await getStudentsSubjectsAndEvaluations(props.studentId, props.periodId, props.schoolId, props.schoolClassId))

</script>

<style scoped>

</style>