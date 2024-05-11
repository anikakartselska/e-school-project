<template>
    <q-page class="q-pa-sm bg-sms">
        <q-card>
            <q-expansion-item v-model="expansionItem[Semester.FIRST]"
                              header-class="text-primary"
                              icon="book"
                              label="Първи срок">
                <assignments-table :assignments="props.assignments" :period-id="periodId" :school-class="schoolClass"
                                   :school-id="schoolId"
                                   :school-lesson="null" :semester="Semester.FIRST" :tab="tab"/>
            </q-expansion-item>
            <q-expansion-item v-model="expansionItem[Semester.SECOND]"
                              header-class="text-primary"
                              icon="book"
                              label="Втори срок">
                <assignments-table :assignments="props.assignments" :period-id="periodId" :school-class="schoolClass"
                                   :school-id="schoolId"
                                   :school-lesson="null" :semester="Semester.SECOND" :tab="tab"/>
            </q-expansion-item>
        </q-card>
    </q-page>
</template>

<script lang="ts" setup>
import {SchoolClass} from "../../model/SchoolClass";
import {$ref} from "vue/macros";
import {Semester} from "../../model/SchoolPeriod";
import {Assignments, AssignmentType} from "../../model/Assignments";
import AssignmentsTable from "./evaluation-tables/assignments-table.vue";

const props = defineProps<{
    periodId: number,
    schoolId: number,
    schoolClass: SchoolClass,
    assignments: Assignments[]
    tab: AssignmentType,
}>()

const expansionItem = $ref({"FIRST": false, "SECOND": false, "YEARLY": false})

</script>

