<template>
    <q-table :columns="columns"
             :pagination="{rowsPerPage:20}"
             :rows="rows"
             hide-pagination
             no-data-label="Няма данни в таблицата"
             no-results-label="Няма резултати от вашето търсене"
             row-key="id"
             separator="cell"
             title="Оценки"
             virtual-scroll
             id="test"
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
                               @click="updateEvaluationDialog(grade)"
                               flat
                               rounded>
                            <q-tooltip>Кликни за повече информация</q-tooltip>
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
                               @click="updateEvaluationDialog(grade)"
                               flat
                               rounded>
                            <q-tooltip>Кликни за повече информация</q-tooltip>
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
import html2pdf from "html2pdf.js";
import {useQuasar} from "quasar";
import EvaluationDialog from "./evaluation-dialog.vue";
import {periodId, schoolId} from "../../../model/constants";
import {$ref} from "vue/macros";
import {deleteEvaluation, updateEvaluation} from "../../../services/RequestService";
import {watch} from "vue";

const props = defineProps<{
    students: StudentView[],
    subjects: Subject[],
    grades: any
    semester: Semester
}>()

let currentGrades = $ref<any>({...props.grades});
let rows = $ref([])
const exportToPDF = () => {
    html2pdf(document.getElementById("test"), {
        margin: 1,
        filename: "test",
        image: {type: 'jpeg', quality: 0.98},
        html2canvas: {scale: 2, useCORS: true},
        jsPDF: {unit: 'in', format: 'a4', orientation: 'landscape'}
    });
}

watch(currentGrades, () => {
    defineRows()
})

const defineRows = () => {
    rows = props.students.map(student => {
                const object = {student: student, numberInClass: student.numberInClass}
                const evaluations = props.subjects.map(subject =>
                        currentGrades[subject.id][student.id]?.filter(it => it.semester == props.semester)
                ).flat(1)
                object["average"] = <Pair<string, string>>{
                    first: calculateAverageGrade(evaluations),
                    second: calculateAverageGrade(evaluations, true)
                }
                props.subjects.forEach(subject =>
                        object[subject.id] = currentGrades[subject.id][student.id]?.filter(it => it.semester == props.semester)
                )
                return object
            }
    )

    const object = <any>{}
    props.subjects.forEach(subject => {
        const evaluations: Evaluation[] = <Evaluation[]>Object.values(currentGrades[subject.id]).flat(1).filter(it => it.semester == props.semester)
        object[subject.id] = <Pair<string, string>>{
            first: calculateAverageGrade(evaluations),
            second: calculateAverageGrade(evaluations, true)
        }
    })
    const allEvaluations = props.subjects.map(subject =>
            <Evaluation[]>Object.values(currentGrades[subject.id]).flat(1).filter(it => it.semester == props.semester)
    ).flat(1)
    object['average'] = <Pair<string, string>>{
        first: calculateAverageGrade(allEvaluations),
        second: calculateAverageGrade(allEvaluations, true)
    }
    rows.push(object)
}
defineRows()
const quasar = useQuasar()
const updateEvaluationDialog = (evaluation: Evaluation) => {
    quasar.dialog({
        component: EvaluationDialog,
        componentProps: {
            evaluation: evaluation,
            periodId: periodId.value,
            schoolId: schoolId.value,
            readonly: false
        },
    }).onOk(async (payload) => {
        const updatedGrade = payload.item.evaluation as Evaluation
        if (payload.item.delete == false) {
            await updateEvaluation(updatedGrade, periodId.value, schoolId.value).then(r => {
                currentGrades[updatedGrade.subject.id][updatedGrade.student.id] = currentGrades[updatedGrade.subject.id][updatedGrade.student.id]?.map(
                        it => {
                            if (it.id == updatedGrade.id) {
                                return updatedGrade
                            } else {
                                return it
                            }
                        })
            })
        } else {
            await deleteEvaluation(updatedGrade, periodId.value, schoolId.value).then(e => {
                currentGrades[updatedGrade.subject.id][updatedGrade.student.id] = currentGrades[updatedGrade.subject.id][updatedGrade.student.id]?.filter(it => it.id !== updatedGrade.id)
            })
        }
    })
}

const columns = (currentGrades ? Object.keys(currentGrades).map(subjectId => {
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
