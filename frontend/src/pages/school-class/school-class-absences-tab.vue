<template>
    <q-page class="q-pa-sm bg-sms">
        <q-card>
            <q-expansion-item v-model="expansionItem[Semester.FIRST]"
                              header-class="text-primary"
                              icon="book"
                              label="Първи срок">
                <absences-table :absences="absences" :semester="Semester.FIRST" :students="studentsFromSchoolClass"
                                :subjects="subjectsTaughtInSchoolClassForYear"/>
            </q-expansion-item>
            <q-expansion-item v-model="expansionItem[Semester.SECOND]"
                              header-class="text-primary"
                              icon="book"
                              label="Втори срок">
                <absences-table :absences="absences" :semester="Semester.SECOND" :students="studentsFromSchoolClass"
                                :subjects="subjectsTaughtInSchoolClassForYear"/>
            </q-expansion-item>
            <q-expansion-item v-model="expansionItem[Semester.YEARLY]"
                              header-class="text-primary"
                              icon="book"
                              label="Общ брой отсъствия за учебната година">
                <absences-table :absences="absences" :semester="Semester.YEARLY" :students="studentsFromSchoolClass"
                                :subjects="subjectsTaughtInSchoolClassForYear"/>
            </q-expansion-item>
        </q-card>
    </q-page>
</template>

<script lang="ts" setup>
import {SchoolClass} from "../../model/SchoolClass";
import {fetchAllStudentSubjectEvaluationsFromSchoolClass} from "../../services/RequestService";
import {EvaluationType} from "../../model/Evaluation";
import {$ref} from "vue/macros";
import {Semester} from "../../model/SchoolPeriod";
import {Subject} from "../../model/Subject";
import {StudentView} from "../../model/User";
import AbsencesTable from "./evaluation-tables/absences-table.vue";

const props = defineProps<{
    periodId: number,
    schoolId: number,
    schoolClass: SchoolClass,
    subjectsTaughtInSchoolClassForYear: Subject[]
    studentsFromSchoolClass: StudentView[]
}>()

const absences = $ref(await fetchAllStudentSubjectEvaluationsFromSchoolClass(props.schoolClass, EvaluationType.ABSENCE).then(e => e.data))
const expansionItem = $ref({"FIRST": true, "SECOND": false, "YEARLY": false})

</script>

