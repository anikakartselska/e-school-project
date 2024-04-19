<template>
    <q-table
            :columns="columns"
            :rows="rows"
            hide-pagination
            no-data-label="Няма данни в таблицата"
            no-results-label="Няма резултати от вашето търсене"
            :pagination="{rowsPerPage:20}"
            row-key="id"
            separator="cell"
            title="Оценки"
            virtual-scroll
    >
        <template v-slot:header-cell-average="props">
            <q-th>
                <div>{{ props.col.label }}</div>
                <q-separator v-if="semester !== Semester.YEARLY"/>
                <div class="row" style="width: 20vh">
                    <div v-if="semester !== Semester.YEARLY" class="col text-center">
                        Текущи оценки
                    </div>
                    <q-separator vertical/>
                    <div v-if="semester !== Semester.YEARLY" class="col-6 text-center">
                        {{ semester !== Semester.YEARLY ? 'Срочна' : 'Годишна' }}
                    </div>
                </div>
            </q-th>
        </template>
        <template v-for="column in columns.slice(3,columns.length)" :key="column.name"
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
                        Средно
                    </div>
                    <q-separator vertical/>
                    <div v-if="semester !== Semester.YEARLY" class="col-2 text-center">
                        {{ semester !== Semester.YEARLY ? 'Срочна' : 'Годишна' }}
                    </div>
                </div>
            </q-th>
        </template>
        <template v-slot:body-cell-average="props">
            <q-td>
                <div class="row">
                    <div v-if="semester !== Semester.YEARLY" class="col-6 text-center">
                        <q-btn v-if="!isNaN(props.row.average?.first)"
                               :class="`q-ma-xs ${getAverageGradeColorClass(props.row.average?.first)}`"
                               :label="props.row.average?.first ? props.row.average?.first : ''"
                               flat
                               rounded>
                            <q-tooltip>
                                Средноаритметична оценка
                            </q-tooltip>
                        </q-btn>
                    </div>
                    <q-separator v-if="semester !== Semester.YEARLY" vertical/>
                    <div class="col text-center">
                        <q-btn v-if="!isNaN(props.row.average?.second)"
                               :class="`q-ma-xs ${getAverageGradeColorClass(props.row.average?.second)}`"
                               :label="props.row.average?.second ? props.row.average?.second : ''"
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
        <template v-for="column in columns.slice(3,columns.length)" :key="column.name"
                  v-slot:[`body-cell-${column.name}`]="props">
            <q-td :props="props">
              <div v-if="props.row.student?.firstName=== undefined" class="row">
                <div v-if="semester !== Semester.YEARLY" class="col-8"/>
                <q-separator v-if="semester !== Semester.YEARLY" vertical/>
                <div v-if="semester !== Semester.YEARLY" class="col-2">
                  <q-btn v-if="!isNaN(props.row[column.name]?.first)"
                         :class="`q-ma-xs ${getAverageGradeColorClass(props.row[column.name]?.first)}`"
                         :label="props.row[column.name]?.first ? props.row[column.name]?.first : ''"
                         flat
                         rounded>
                    <q-tooltip>
                                Средноаритметична оценка
                            </q-tooltip>
                        </q-btn>
                    </div>
                    <q-separator v-if="semester !== Semester.YEARLY" vertical/>
                    <div class="col text-center">
                        <q-btn v-if="!isNaN(props.row[column.name]?.second)"
                               :class="`q-ma-xs ${getAverageGradeColorClass(props.row[column.name]?.second)}`"
                               :label="props.row[column.name]?.second ? props.row[column.name]?.second : ''"
                               flat
                               rounded>
                            <q-tooltip>
                                Средноаритметична оценка
                            </q-tooltip>
                        </q-btn>
                    </div>
                </div>
                <div v-else class="row" style="width: 20vw">
                    <div v-if="semester !== Semester.YEARLY" class="col-8 text-center">
                        <q-btn v-for="grade in props.row[column.name]?.filter(it=> !it.evaluationValue.finalGrade)"
                               :class="`q-ma-xs ${gradeBackgroundColorMap.get(grade.evaluationValue.grade)}`"
                               :label="gradeMap.get(grade.evaluationValue.grade)?.toString()"
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
                    <q-separator v-if="semester !== Semester.YEARLY" vertical/>
                    <div v-if="semester !== Semester.YEARLY" class="col-2 text-center">
                        <q-btn v-if="!isNaN(calculateAverageGrade(props.row[column.name]))"
                               :class="`q-ma-xs ${getAverageGradeColorClass(calculateAverageGrade(props.row[column.name]))}`"
                               :label="calculateAverageGrade(props.row[column.name])"
                               flat
                               rounded>
                            <q-tooltip>
                                Средноаритметична оценка
                            </q-tooltip>
                        </q-btn>
                    </div>
                    <q-separator v-if="semester!== Semester.YEARLY" vertical/>
                    <div class="col text-center">
                        <q-btn v-for="grade in props.row[column.name]?.filter(it=> it.evaluationValue.finalGrade===true)"
                               :class="`q-ma-xs ${gradeBackgroundColorMap.get(grade.evaluationValue.grade)}`"
                               :label="`${gradeMap.get(grade.evaluationValue.grade)?.toString()}`"
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
                </div>
            </q-td>
        </template>
    </q-table>
</template>
<script lang="ts" setup>

import {
    calculateAverageGrade,
    getAverageGradeColorClass,
    gradeBackgroundColorMap,
    gradeMap
} from "../../../services/helper-services/EvaluationService";
import {Evaluation} from "../../../model/Evaluation";
import {StudentView} from "../../../model/User";
import {Subject} from "../../../model/Subject";
import {Semester} from "../../../model/SchoolPeriod";

const props = defineProps<{
    students: StudentView[],
    subjects: Subject[],
    grades: any
    semester: Semester
}>()

const rows = props.students.map(student => {
            const object = {student: student, numberInClass: student.numberInClass}
            const evaluations = props.subjects.map(subject =>
                    props.grades[subject.id][student.id]?.filter(it => it.semester == props.semester)
            ).flat(1)
            object["average"] = <Pair<string, string>>{
                first: calculateAverageGrade(evaluations),
                second: calculateAverageGrade(evaluations, true)
            }
            props.subjects.forEach(subject =>
                    object[subject.id] = props.grades[subject.id][student.id]?.filter(it => it.semester == props.semester)
            )
            return object
        }
)

const object = <any>{}
props.subjects.forEach(subject => {
    const evaluations: Evaluation[] = <Evaluation[]>Object.values(props.grades[subject.id]).flat(1).filter(it => it.semester == props.semester)
    object[subject.id] = <Pair<string, string>>{
        first: calculateAverageGrade(evaluations),
        second: calculateAverageGrade(evaluations, true)
    }
})
const allEvaluations = props.subjects.map(subject =>
        <Evaluation[]>Object.values(props.grades[subject.id]).flat(1).filter(it => it.semester == props.semester)
).flat(1)
object['average'] = <Pair<string, string>>{
    first: calculateAverageGrade(allEvaluations),
    second: calculateAverageGrade(allEvaluations, true)
}
rows.push(object)

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
    name: "average",
    align: "center",
    label: "Среден успех",
    field: (row) => row["student"]?.average,
    sortable: true
})
columns.unshift({
    name: "student",
    align: "center",
    label: "Ученик",
    //@ts-ignore
    field: (row) => row["student"] ? `${row["student"]?.firstName} ${row["student"]?.middleName} ${row["student"]?.lastName}` : "Средноаритметично",
    sortable: true
})
columns.unshift({
    name: "numberInClass",
    align: "center",
    label: "Номер",
    field: (row) => row["student"]?.numberInClass,
    sortable: true
})

</script>
