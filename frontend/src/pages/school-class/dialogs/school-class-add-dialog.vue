<template>
    <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
        <q-card class="q-dialog-plugin" style="width: 500px;">
            <q-card-section class="dialog-header">
          <span class="text-h6">Добави клас
        <q-btn v-close-popup
               class="float-right text-black"
               dense
               flat
               icon="close"
               round/>
            </span>
            </q-card-section>
            <q-card-section>
                <q-form class="q-gutter-md" @submit="submit()">
                    <q-select v-model="classNumber"
                              :options="schoolClassesNumberOptions"
                              :rules="[val=>val !== null && val !== '' || 'Задължително поле',val=>!alreadyExistingSchoolClassesNames.includes(val+classAlpha) || 'Такъв клас вече съществува']"
                              label="Клас"
                              reactive-rules/>
                    <q-input v-model="classAlpha"
                             :rules="[val=>val !== null && val !== '' || 'Задължително поле',val=>!alreadyExistingSchoolClassesNames.includes(classNumber + val) || 'Такъв клас вече съществува']"
                             label="Паралелка"
                             mask="S"
                             reactive-rules/>
                    <q-select v-model="schoolClass.mainTeacher"
                              :option-label="(option:UserView) => `${option.firstName} ${option.middleName} ${option.lastName}`"
                              :options="teacherOptions"
                              :rules="[val=>val !== null && val !== '' || 'Задължително поле']"
                              label="Класен ръководител"/>
                    <q-select v-model="schoolClass.shifts.firstSemester"
                              :option-label="(option:Shift) => shiftTranslation[option]"
                              :options="Object.keys(Shift)"
                              :rules="[val=>val !== null && val !== '' || 'Задължително поле']"
                              label="Смяна първи срок"/>
                    <q-select v-model="schoolClass.shifts.secondSemester"
                              :option-label="(option:Shift) => shiftTranslation[option]"
                              :options="Object.keys(Shift)"
                              :rules="[val=>val !== null && val !== '' || 'Задължително поле']"
                              label="Смяна втори срок"/>
                    <single-file-picker
                            v-model="studentsFromSchoolFile"
                            accept-file-format=".xlsx"
                            class="q-mt-md q-mr-sm"
                            label="Файл с ученици"
                            remove-action-button
                    />
                    <q-card-actions align="right">
                        <q-btn color="primary" label="Добави" @click="submit"/>
                        <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
                    </q-card-actions>
                </q-form>
            </q-card-section>
        </q-card>
    </q-dialog>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {SchoolClass, Shift, shiftTranslation} from "../../../model/SchoolClass";
import {UserView} from "../../../model/User";
import {useDialogPluginComponent, useQuasar} from "quasar";
import {getRangeOf} from "../../../utils";
import SingleFilePicker from "../../common/single-file-picker.vue";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
    schoolClass: SchoolClass,
    alreadyExistingSchoolClasses: SchoolClass[]
    teacherOptions: UserView[]
}>()

const schoolClass = $ref({...props.schoolClass, shifts: {firstSemester: Shift.FIRST, secondSemester: Shift.FIRST}})
const classNumber = $ref<number | null>(null)
const schoolClassesNumberOptions = getRangeOf(12, 1, -1).map(it => it.toString())
const classAlpha = $ref<string | null>(null)
const alreadyExistingSchoolClassesNames = props.alreadyExistingSchoolClasses.map(it => it.name)
const studentsFromSchoolFile = $ref<File | null>(null)
const submit = () => {
    onDialogOK({
        item: <Pair<SchoolClass, Blob>>{
            first: {...schoolClass, name: classNumber!! + classAlpha!!},
            second: studentsFromSchoolFile
        }
    })
}
</script>

<style scoped>

</style>