<template>
    <q-page class="q-pa-sm bg-sms">
        <q-card>
            <q-expansion-item v-model="expansionItem[Semester.FIRST]"
                              header-class="text-primary"
                              icon="book"
                              label="Първи срок">
                <student-grades-table :evaluations="evaluations" :semester="Semester.FIRST"/>
            </q-expansion-item>
            <q-expansion-item v-model="expansionItem[Semester.SECOND]"
                              header-class="text-primary"
                              icon="book"
                              label="Втори срок">
                <student-grades-table :evaluations="evaluations" :semester="Semester.SECOND"/>
            </q-expansion-item>
            <q-expansion-item v-model="expansionItem[Semester.YEARLY]"
                              header-class="text-primary"
                              icon="book"
                              label="Годишни оценки">
                <student-grades-table :evaluations="evaluations" :semester="Semester.YEARLY"/>
            </q-expansion-item>
        </q-card>
    </q-page>
</template>

<script lang="ts" setup>
import {SubjectWithEvaluationDTO} from "../../model/SubjectWithEvaluationDTO";
import {Semester} from "../../model/SchoolPeriod";
import StudentGradesTable from "./student-grades-table.vue";
import {$ref} from "vue/macros";

const props = defineProps<{
    evaluations: SubjectWithEvaluationDTO[],
}>()
const expansionItem = $ref({"FIRST": false, "SECOND": false, "YEARLY": false})
</script>

<style scoped>

</style>