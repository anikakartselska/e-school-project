<template>
    <q-table
            :columns="columns"
            :rows="rows"
            hide-pagination
            no-data-label="Няма данни в таблицата"
            no-results-label="Няма резултати от вашето търсене"
            row-key="id"
            separator="cell"
            title="Отзиви"
            virtual-scroll
    >
        <template v-slot:header-cell-total="props">
            <q-th>
                <div>{{ props.col.label }}</div>
                <q-separator/>
                <div class="row" style="width: 20vh">
                    <div class="col text-center">
                        Позитивни
                    </div>
                    <q-separator vertical/>
                    <div class="col-4 text-center">
                        Негативни
                    </div>
                    <q-separator vertical/>
                    <div class="col-4 text-center">
                        Общо
                    </div>
                </div>
            </q-th>
        </template>
        <template v-for="column in columns.slice(3,columns.length)" :key="column.name"
                  v-slot:[`header-cell-${column.name}`]="props">
            <q-th>
                <div>{{ props.col.label }}</div>
                <q-separator/>
                <div class="row">
                    <div v-if="semester !== Semester.YEARLY" class="col-8 text-center">
                        Отзиви
                    </div>
                    <q-separator vertical/>
                    <div class="col row">
                        <div class="col text-center">
                            Позитивни
                        </div>
                        <q-separator vertical/>
                        <div class="col-6 text-center">
                            Негативни
                        </div>
                    </div>
                </div>
            </q-th>
        </template>
        <template v-slot:body-cell-total="props">
            <q-td>
                <div class="row">
                    <div class="col-4 text-center">
                        <q-btn :class="`q-ma-xs bg-green-3`"
                               :label="props.row.total?.second.first ? props.row.total?.second.first : 0"
                               flat
                               rounded>
                        </q-btn>
                    </div>
                    <q-separator vertical/>
                    <div class="col-4 text-center">
                        <q-btn :class="`q-ma-xs bg-red-3`"
                               :label="props.row.total?.second.second ? props.row.total?.second.second : 0"
                               flat
                               rounded>
                        </q-btn>
                    </div>
                    <q-separator vertical/>
                    <div class="col text-center">
                        <q-btn :class="`q-ma-xs bg-blue-3`"
                               :label="props.row.total?.first ? props.row.total?.first : 0"
                               flat
                               rounded>
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
                    <div class="col row">
                        <div class="col-6">
                            <q-btn :class="`q-ma-xs bg-green-3`"
                                   :label="props.row[column.name]?.second.first ? props.row[column.name]?.second.first : 0"
                                   flat
                                   rounded>
                            </q-btn>
                        </div>
                        <q-separator vertical/>
                        <div class="col text-center">
                            <q-btn :class="`q-ma-xs bg-red-3`"
                                   :label="props.row[column.name]?.second.second ? props.row[column.name]?.second.second : 0"
                                   flat
                                   rounded>
                            </q-btn>
                        </div>
                    </div>
                </div>
                <div v-else class="row" style="width: 20vw">
                    <div v-if="semester !== Semester.YEARLY" class="col-8 text-center">
                        <q-btn v-for="feedback in props.row[column.name]"
                               :class="feedbacksMap.get(feedback.evaluationValue.feedback)===true? `text-green-3` : `text-red-3`" :icon="feedbacksMap.get(feedback.evaluationValue.feedback)===true? 'thumb_up_alt' : 'thumb_down_alt'"
                               flat
                               rounded
                        >
                            <q-tooltip>
                                {{ feedbacksMapTranslation.get(feedback.evaluationValue.feedback) }}
                            </q-tooltip>
                            <q-popup-proxy>
                                <q-banner>
                                    Въведен от:<span class="text-primary">{{
                                    feedback.createdBy.firstName
                                    }} {{ feedback.createdBy.lastName }}</span><br/>
                                    Дата:<span class="text-primary">{{
                                    feedback.evaluationDate
                                    }}</span><br/>
                                </q-banner>
                            </q-popup-proxy>
                        </q-btn>
                    </div>
                    <q-separator v-if="semester !== Semester.YEARLY" vertical/>
                    <div class="col row">
                        <div class="col text-center">
                            <q-btn :class="`q-ma-xs bg-green-2`"
                                   :label="countFeedbacksSum(props.row[column.name],true)"
                                   flat
                                   rounded>
                            </q-btn>
                        </div>
                        <q-separator vertical/>
                        <div class="col-6 text-center">
                            <q-btn :class="`q-ma-xs bg-red-2`"
                                   :label="countFeedbacksSum(props.row[column.name],false)"
                                   flat
                                   rounded>
                            </q-btn>
                        </div>
                    </div>
                </div>
            </q-td>
        </template>
    </q-table>
</template>
<script lang="ts" setup>

import {
    countFeedbacksSum,
    feedbacksMap,
    feedbacksMapTranslation
} from "../../../services/helper-services/EvaluationService";
import {Evaluation} from "../../../model/Evaluation";
import {StudentView} from "../../../model/User";
import {Subject} from "../../../model/Subject";
import {Semester} from "../../../model/SchoolPeriod";

const props = defineProps<{
    students: StudentView[],
    subjects: Subject[],
    feedbacks: any
    semester: Semester
}>()

const rows = props.students.map(student => {
            const object = {student: student, numberInClass: student.numberInClass}
            const evaluations = props.subjects.map(subject =>
                    props.feedbacks[subject.id][student.id]?.filter(it => props.semester == Semester.YEARLY || it.semester == props.semester)
            ).flat(1)
            object["total"] = <Pair<number, Pair<number, number>>>{
                first: countFeedbacksSum(evaluations),
                second: <Pair<number, number>>{
                    first: countFeedbacksSum(evaluations, true),
                    second: countFeedbacksSum(evaluations, false)
                }
            }
            props.subjects.forEach(subject =>
                    object[subject.id] = props.feedbacks[subject.id][student.id]?.filter(it => props.semester == Semester.YEARLY || it.semester == props.semester)
            )
            return object
        }
)

const object = <any>{}
props.subjects.forEach(subject => {
    const evaluations: Evaluation[] = <Evaluation[]>Object.values(props.feedbacks[subject.id]).flat(1).filter(it => props.semester == Semester.YEARLY || it.semester == props.semester)
    object[subject.id] = <Pair<number, Pair<number, number>>>{
        first: countFeedbacksSum(evaluations),
        second: <Pair<number, number>>{
            first: countFeedbacksSum(evaluations, true),
            second: countFeedbacksSum(evaluations, false)
        }
    }
})
const allEvaluations = props.subjects.map(subject =>
        <Evaluation[]>Object.values(props.feedbacks[subject.id]).flat(1).filter(it => props.semester == Semester.YEARLY || it.semester == props.semester)
).flat(1)
object['total'] = <Pair<number, Pair<number, number>>>{
    first: countFeedbacksSum(allEvaluations),
    second: <Pair<number, number>>{
        first: countFeedbacksSum(allEvaluations, true),
        second: countFeedbacksSum(allEvaluations, false)
    }
}
rows.push(object)
console.log(object)

const columns = (props.feedbacks ? Object.keys(props.feedbacks).map(subjectId => {
    return {
        name: subjectId,
        align: "center",
        label: props.subjects.find(subject => subject.id.toString() == subjectId)?.name,
        field: (row) => (<Evaluation[]>row[subjectId]).map(it => it.semester),
        sortable: true
    }
}) : [])
columns.unshift({
    name: "total",
    align: "center",
    label: "Обобщени",
    field: (row) => row["student"]?.total,
    sortable: true
})
columns.unshift({
    name: "student",
    align: "center",
    label: "Ученик",
    //@ts-ignore
    field: (row) => row["student"] ? `${row["student"]?.firstName} ${row["student"]?.middleName} ${row["student"]?.lastName}` : "Общ брой",
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
