<template>
    <q-page class="q-pa-sm bg-sms">
        <q-card>
            <q-expansion-item v-model="expansionItem[Semester.FIRST]"
                              header-class="text-primary"
                              icon="book"
                              label="Първи срок">
                <remarks-table :feedbacks="remarks" :semester="Semester.FIRST" :students="studentsFromSchoolClass"
                               :subjects="subjectsTaughtInSchoolClassForYear"/>
            </q-expansion-item>
            <q-expansion-item v-model="expansionItem[Semester.SECOND]"
                              header-class="text-primary"
                              icon="book"
                              label="Втори срок">
                <remarks-table :feedbacks="remarks" :semester="Semester.SECOND" :students="studentsFromSchoolClass"
                               :subjects="subjectsTaughtInSchoolClassForYear"/>
            </q-expansion-item>
            <q-expansion-item v-model="expansionItem[Semester.YEARLY]"
                              header-class="text-primary"
                              icon="book"
                              label="Общ брой забележки за учебната година">
                <remarks-table :feedbacks="remarks" :semester="Semester.YEARLY" :students="studentsFromSchoolClass"
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
import RemarksTable from "./evaluation-tables/feedbacks-table.vue";

const props = defineProps<{
    periodId: number,
    schoolId: number,
    schoolClass: SchoolClass,
    subjectsTaughtInSchoolClassForYear: Subject[]
    studentsFromSchoolClass: StudentView[]
}>()

const remarks = $ref(await fetchAllStudentSubjectEvaluationsFromSchoolClass(props.schoolClass, EvaluationType.FEEDBACK).then(e => e.data))
const expansionItem = $ref({"FIRST": false, "SECOND": false, "YEARLY": false})

</script>

