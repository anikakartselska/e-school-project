<template>
    <div class="bg-sms">
        <div class="col-2"></div>
        <div class="col-8">
            <q-page class="page-content" padding>
                <div>
                    <div class="text-h4">
                        Предмет: {{ subject.name }}
                    </div>
                    <div class="text-h5">
                        Преподавател:
                        <router-link :to="`/user/${subject.teacher.id}/${periodId}/${schoolId}`"
                                     active-class="text-negative" class="text-primary"
                                     exact-active-class="text-negative">
                            {{ subject.teacher.firstName }} {{ subject.teacher.lastName }}
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
                                         :evaluations="studentWithEvaluationDTO"
                                         :subject="subject"
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
    fetchSubjectById,
    getEvaluationForSubjectAndSchoolClass,
    getSchoolClassById
} from "../../services/RequestService";
import {$ref} from "vue/macros";

const props = defineProps<{
    schoolClassId: number,
    subjectId: number,
    periodId: number,
    schoolId: number
}>()
const schoolClass = $ref(await getSchoolClassById(props.schoolClassId, props.periodId))
let subject = $ref(await fetchSubjectById(props.subjectId, props.periodId, props.schoolId))
let studentWithEvaluationDTO = $ref(await getEvaluationForSubjectAndSchoolClass(props.subjectId, props.periodId, props.schoolId, props.schoolClassId))

</script>

<style scoped>

</style>