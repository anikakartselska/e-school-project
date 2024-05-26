<template>
    <div v-if="!hasPlannedLessons">
        <div v-if="currentUserHasAnyRole([SchoolRole.ADMIN])" class="q-pl-md text-negative">Все още няма генерирана
            програма
            за срока
        </div>
    </div>
    <div v-else>
        <div class="row">
            <q-select v-model="selectedSchoolClass"
                      :option-label="option => option.name"
                      :options="schoolClasses"
                      class="q-pl-md"
                      dense
                      label="Клас"
                      reactive-rules
                      style="width: 250px"/>
            <q-space/>
            <q-btn v-if="!hasLessons && currentUserHasAnyRole([SchoolRole.ADMIN])" class="q-mr-md text-negative"
                   label="Генетирай програмите за всички класове отново"
                   outline
                   @click="generateProgram()"
            ></q-btn>
            <q-btn v-if="!hasLessons && currentUserHasAnyRole([SchoolRole.ADMIN])" class="q-mr-md text-secondary"
                   label="Генерирай седмични разписи"
                   outline
                   @click="generateSchoolLessonsForAllClasses()"
            ></q-btn>
        </div>
        <q-separator class="q-mt-md q-mb-md"/>
        <div class="row">
            <div v-for="day in Object.keys(WorkingDays)" class="col-2 q-ma-sm">
                <div class=" text-center text-primary text-h6">
                    {{ workingDaysTranslation.get(day) }}
                </div>
                <q-card v-for="lesson in plannedLessonsGroupedByDay.get(WorkingDays[day])" class="q-mb-sm"
                        style="height: 15vh">
                    <q-card-section>
                        Предмет:<span class="text-primary">
              {{
                        lesson.subject
                        }}</span><br>
                        Учител:<span class="text-primary">
              {{ lesson.teacher.firstName }} {{ lesson.teacher.lastName }}</span>
                        <br>
                        Стая:<span class="text-primary">
              {{
                        lesson.room.room
                        }}
                  </span>
                    </q-card-section>
                </q-card>
            </div>
        </div>
        <div v-if="!selectedSchoolClass" class="q-pl-md text-negative">Изберете, клас, за да видите програмата</div>
    </div>
</template>
<script lang="ts" setup>
import {
    checkExistingPlannedSchoolLessonsForSemester,
    checkExistingSchoolLessonsForSemester,
    fetchPlannedSchoolLessonsForSchool,
    generatePlannedSchoolLessonsForSchool,
    generateSchoolLessons,
    getSchoolClassesFromSchool
} from "../../../services/RequestService";
import {Semester} from "../../../model/SchoolPeriod";
import {SchoolClass} from "../../../model/SchoolClass";
import {
    PlannedSchoolLesson,
    WorkingDays,
    workingDaysOrder,
    workingDaysTranslation
} from "../../../model/PlannedSchoolLesson";
import {watch} from "vue";
import {$ref} from "vue/macros";
import {currentUserHasAnyRole} from "../../../services/LocalStorageService";
import {SchoolRole} from "../../../model/User";
import {confirmActionPromiseDialog} from "../../../utils";

const props = defineProps<{
    periodId: number,
    schoolId: number,
    semester: Semester
}>()
let schoolPlannedLessons = $ref(await fetchPlannedSchoolLessonsForSchool(props.schoolId, props.periodId, props.semester))
const schoolClasses = await getSchoolClassesFromSchool(props.schoolId, props.periodId)
let selectedSchoolClass = $ref<SchoolClass | null>(null)
const plannedLessonsGroupedByDay = $ref(new Map<WorkingDays, PlannedSchoolLesson[]>())
let hasPlannedLessons = $ref(await checkExistingPlannedSchoolLessonsForSemester(props.schoolId, props.periodId, props.semester))
let hasLessons = $ref(await checkExistingSchoolLessonsForSemester(props.schoolId, props.periodId, props.semester))
const generateProgram = async () => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
    await generatePlannedSchoolLessonsForSchool(props.schoolId, props.periodId, props.semester).then(
            r => {
                schoolPlannedLessons = r
                hasPlannedLessons = true
            }
    )
}

const generateSchoolLessonsForAllClasses = async () => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?", "Генерирането на седмичните разписи няма да ви позволи да правите цялостни промени по програмата, учебния план за годината и промяната на смените на класовете.")
    await generateSchoolLessons(props.schoolId, props.periodId, props.semester).then(
            r => {
                hasLessons = true
            }
    )
}
let plannedLessons = $ref<PlannedSchoolLesson[]>([])
watch(() => selectedSchoolClass, () => {
            if (selectedSchoolClass == null) {
                plannedLessons = []
            } else {
                plannedLessons = schoolPlannedLessons.filter(it => it.schoolClass.id == selectedSchoolClass!!.id).sort(function (a, b) {
                    return workingDaysOrder.get(a.workingHour.workingDays)!! - workingDaysOrder.get(b.workingHour.workingDays)!! || a.workingHour.hour - b.workingHour.hour;
                })
                Object.keys(WorkingDays).forEach(day => {
                    plannedLessonsGroupedByDay.set(day, plannedLessons.filter(lesson => lesson.workingHour.workingDays == day))

                })
            }
        }
)
</script>
