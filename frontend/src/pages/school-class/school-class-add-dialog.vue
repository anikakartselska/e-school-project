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
import {SchoolClass} from "../../model/SchoolClass";
import {UserView} from "../../model/User";
import {useDialogPluginComponent, useQuasar} from "quasar";
import {getRangeOf} from "../../utils";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
    schoolClass: SchoolClass,
    alreadyExistingSchoolClasses: SchoolClass[]
    teacherOptions: UserView[]
}>()

const schoolClass = $ref(props.schoolClass)
const classNumber = $ref<number | null>(null)
const schoolClassesNumberOptions = getRangeOf(12, 1, -1).map(it => it.toString())
const classAlpha = $ref<string | null>(null)
const alreadyExistingSchoolClassesNames = props.alreadyExistingSchoolClasses.map(it => it.name)
const submit = () => {
    onDialogOK({
        item: {...schoolClass, name: classNumber!! + classAlpha!!}
    })
}
</script>

<style scoped>

</style>