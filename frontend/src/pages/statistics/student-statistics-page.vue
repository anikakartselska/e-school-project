<template>
    <q-page class="q-pa-sm bg-sms">
        <div class="row">
            <div class="col-1"/>
            <div class="text-h4 q-ml-md q-mb-md text-primary">Статистики</div>
        </div>
        <div class="row">
            <div class="col-1"/>
            <q-card
                    class="my-card text-white col-3 q-ml-md q-mr-md"
                    style="background: radial-gradient(circle, #35a2ff 0%, #014a88 100%)"
                    @click="reRouteToGradesTap()"
            >
                <q-card-section>
                    <div class="text-h6">Среден успех</div>
                    <div class="text-h1">{{ statistics.success?.toPrecision(3) }}</div>
                </q-card-section>
            </q-card>
            <q-card
                    class="my-card text-white col-3 q-ml-md q-mr-md"
                    style="height: 20vh;background: radial-gradient(circle, #7835ff 0%, #370188 100%)"
                    @click="reRouteToGradesTap()"
            >
                <q-card-section>
                    <div class="text-h6">Брой оценки</div>
                    <div class="text-h1">{{ statistics.grades }}</div>
                </q-card-section>
            </q-card>
            <q-card
                    class="my-card text-white col-3 q-ml-md q-mr-md"
                    style="background: radial-gradient(circle, #ff353c 0%, #880105 100%)"
                    @click="reRouteToAbsencesTap()"
            >
                <q-card-section>
                    <div class="text-h6">Брой отсъствия</div>
                    <div class="text-h1">{{ statistics.absences }}</div>
                </q-card-section>
            </q-card>
        </div>
        <div class="row q-mt-xl">
            <div class="col-1"/>
            <q-card
                    class="my-card text-white col-3 q-ml-md q-mr-md"
                    style="background: radial-gradient(circle, #35ff75 0%, #1e8801 100%)"
                    @click="reRouteToFeedbacksTap()"
            >
                <q-card-section>
                    <div class="text-h6">Брой отзиви</div>
                    <div class="text-h1">{{ statistics.feedback }}</div>
                </q-card-section>
            </q-card>
            <q-card
                    class="my-card text-white col-3 q-ml-md q-mr-md"
                    style="background: radial-gradient(circle, #ff8235 0%, #884901 100%)"
                    @click="reRouteToEventsTap()"
            >
                <q-card-section>
                    <div class="text-h6">Брой събития</div>
                    <div class="text-h1">{{ statistics.events }}</div>
                </q-card-section>
            </q-card>
            <q-card
                    class="my-card text-white col-3 q-ml-md q-mr-md"
                    style="height: 20vh;
            background: radial-gradient(circle, #ffe435 0%, #7b8801 100%)"
                    @click="reRouteToExaminationsTap()"
            >
                <q-card-section>
                    <div class="text-h6">Брой изпитвания</div>
                    <div class="text-h1">{{ statistics.examinations }}</div>
                </q-card-section>
            </q-card>
        </div>
        <q-separator class="q-ma-lg"/>
        <div class="row q-mt-xl">
            <div class="col-1"/>
            <q-card
                    class="my-card text-primary col-3 q-ml-md q-mr-md"
            >
                <q-card-section>
                    <div class="text-h6">Място в класа</div>
                    <div class="text-h1">{{ statistics.placeInClass !== 0 ? statistics.placeInClass : '_' }}
                        <q-icon name="bar_chart"/>
                    </div>
                </q-card-section>
            </q-card>
            <q-card
                    class="my-card text-primary col-3 q-ml-md q-mr-md">
                <q-card-section>
                    <div class="text-h6">Място във випуска</div>
                    <div class="text-h1">{{
                        statistics.placeInGraduationClass !== 0 ? statistics.placeInGraduationClass : '_'
                        }}
                        <q-icon name="bar_chart"/>
                    </div>
                </q-card-section>
            </q-card>
            <q-card
                    class="my-card text-primary col-3 q-ml-md q-mr-md"
                    style="height: 20vh;"
            >
                <q-card-section>
                    <div class="text-h6">Място в училище</div>
                    <div class="text-h1">{{ statistics.placeInSchool !== 0 ? statistics.placeInSchool : '_' }}
                        <q-icon name="bar_chart"/>
                    </div>
                </q-card-section>
            </q-card>
        </div>
    </q-page>
</template>

<script lang="ts" setup>
import {getCurrentUser, mapUserSecurityToOneRoleUser} from "../../services/LocalStorageService";
import {DetailsForParent, DetailsForStudent, OneRoleUser, SchoolRole} from "../../model/User";
import {$ref} from "vue/macros";
import {fetchStatisticsForStudent} from "../../services/RequestService";
import {router} from "../../router";

const props = defineProps<{
    periodId: number
    schoolId: number
}>()

const currentUser = getCurrentUser()
let student = $ref<OneRoleUser | null>(null)
if (currentUser.role.role == SchoolRole.PARENT) {
    student = (currentUser.role.detailsForUser as DetailsForParent).child
} else if (currentUser.role.role == SchoolRole.STUDENT) {
    student = mapUserSecurityToOneRoleUser(currentUser)
}

const statistics = await fetchStatisticsForStudent(student!!, props.schoolId, props.periodId)

const reRouteToGradesTap = async () => await router.push(`/student-diary/${(student?.role.detailsForUser as DetailsForStudent).schoolClass?.id}/${student?.id}/${props.periodId}/${props.schoolId}/grades`)

const reRouteToAbsencesTap = async () => await router.push(`/student-diary/${(student?.role.detailsForUser as DetailsForStudent).schoolClass?.id}/${student?.id}/${props.periodId}/${props.schoolId}/absences`)

const reRouteToFeedbacksTap = async () => await router.push(`/student-diary/${(student?.role.detailsForUser as DetailsForStudent).schoolClass?.id}/${student?.id}/${props.periodId}/${props.schoolId}/feedbacks`)

const reRouteToExaminationsTap = async () => await router.push(`/student-diary/${(student?.role.detailsForUser as DetailsForStudent).schoolClass?.id}/${student?.id}/${props.periodId}/${props.schoolId}/examinations`)

const reRouteToEventsTap = async () => await router.push(`/student-diary/${(student?.role.detailsForUser as DetailsForStudent).schoolClass?.id}/${student?.id}/${props.periodId}/${props.schoolId}/events`)

</script>

<style scoped>

</style>