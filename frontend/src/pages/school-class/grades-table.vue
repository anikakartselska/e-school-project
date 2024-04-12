<template>
    <q-table
            :columns="columns"
            :rows="rows"
            :separator="'cell'"
            no-data-label="Няма данни в таблицата"
            no-results-label="Няма резултати от вашето търсене"
            row-key="id"
            rows-per-page-label="Редове на страница"
            title="Ученици"
    >
        <template v-for="column in columns.slice(2,columns.length)" :key="column.name"
                  v-slot:[`header-cell-${column.name}`]="props">
            <q-th>
                <div>{{ props.col.label }}</div>
                <q-separator v-if="semester !== Semester.YEARLY"/>
                <div class="row">
                    <div v-if="semester !== Semester.YEARLY" class="col-8 text-center">
                        Оценки
                    </div>
                    <q-separator vertical/>
                    <div v-if="semester !== Semester.YEARLY" class="col text-center">
                        {{ semester !== Semester.YEARLY ? 'Срочна' : 'Годишна' }}
                    </div>
                    <q-separator vertical/>
                    <div v-if="semester !== Semester.YEARLY" class="col-2 text-center">
                        Средноаритм.
                    </div>
                </div>
            </q-th>
        </template>
        <template v-for="column in columns.slice(2,columns.length)" :key="column.name"
                  v-slot:[`body-cell-${column.name}`]="props">
            <q-td :props="props">
                <div class="row">
                    <div v-if="semester !== Semester.YEARLY" class="col-8 text-center">
                        <q-btn v-for="grade in props.row[column.name]?.filter(it=> !it.evaluationValue.finalGrade)"
                               :class="`q-ma-xs ${gradeBackgroundColorMap.get(grade.evaluationValue.grade)}`" :label="gradeMap.get(grade.evaluationValue.grade).toString()"
                               flat
                               rounded>
                            <q-popup-proxy>
                                <q-banner>
                                    Въведен от:<span class="text-primary">{{
                                    grade.createdBy.firstName
                                    }} {{ grade.createdBy.lastName }}</span><br/>
                                    Дата:<span class="text-primary">{{
                                    grade.evaluationDate
                                    }}</span><br/>
                                </q-banner>
                            </q-popup-proxy>
                        </q-btn>
                    </div>
                    <q-separator vertical/>
                    <div v-if="props.row[column.name]?.filter(it=> it.evaluationValue.finalGrade).length>0"
                         class="col-2 text-center">
                        <span v-if="semester===Semester.YEARLY">Годишна:</span>
                        <span v-else>Срочна:</span>
                        <q-btn v-for="grade in props.row[column.name]?.filter(it=> it.evaluationValue.finalGrade)"
                               :class="`q-ma-xs ${gradeBackgroundColorMap.get(grade.evaluationValue.grade)}`" :label="`${gradeMap.get(grade.evaluationValue.grade).toString()}`"
                               flat
                               rounded>
                            <q-popup-proxy>
                                <q-banner>
                                    Въведен от:<span class="text-primary">{{
                                    grade.createdBy.firstName
                                    }} {{ grade.createdBy.lastName }}</span><br/>
                                    Дата:<span class="text-primary">{{
                                    grade.evaluationDate
                                    }}</span><br/>
                                </q-banner>
                            </q-popup-proxy>
                        </q-btn>
                    </div>
                    <q-separator vertical/>
                    <div v-if="semester !== Semester.YEARLY" class="col text-center">
                        <q-btn v-if="!isNaN(calculateAverageGrade(props.row[column.name]))" :class="`q-ma-xs bg-grey-3`" :label="calculateAverageGrade(props.row[column.name])"
                               flat
                               rounded>
                            <q-tooltip>
                                Средноаритметична оценка
                            </q-tooltip>
                        </q-btn>
                    </div>
                </div>
            </q-td>
        </template>
    </q-table>
</template>
<script lang="ts" setup>

import {
    calculateAverageGrade,
    gradeBackgroundColorMap,
    gradeMap
} from "../../services/helper-services/EvaluationService";
import {Evaluation} from "../../model/Evaluation";
import {StudentView} from "../../model/User";
import {Subject} from "../../model/Subject";
import {Semester} from "../../model/SchoolPeriod";

const props = defineProps<{
    students: StudentView[],
    subjects: Subject[],
    grades: any
    semester: Semester
}>()

const rows = props.students.map(student => {
            const object = {student: student, numberInClass: student.numberInClass}
            props.subjects.forEach(subject =>
                    object[subject.id] = props.grades[subject.id][student.id]?.filter(it => it.semester == props.semester)
            )
            return object
        }
)


const columns = (props.grades ? Object.keys(props.grades).map(subjectId => {
    return {
        name: subjectId,
        align: "center",
        label: props.subjects.find(subject => subject.id.toString() == subjectId)?.name,
        field: (row) => (<Evaluation[]>row[subjectId]).map(it => it.semester),
        sortable: true
    }
}) : [])
columns.unshift({
    name: "student",
    align: "center",
    label: "Ученик",
    //@ts-ignore
    field: (row) => `${row["student"].firstName} ${row["student"].middleName} ${row["student"].lastName}`,
    sortable: true
})
columns.unshift({
    name: "numberInClass",
    align: "center",
    label: "Номер",
    field: (row) => row["student"].numberInClass,
    sortable: true
})
</script>
