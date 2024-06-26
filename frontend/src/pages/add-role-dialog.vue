<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Добави роля
        <q-btn v-close-popup
               class="float-right text-black"
               dense
               flat
               icon="close"
               round/>
            </span>
      </q-card-section>
      <q-card-section>
        <q-form class="q-gutter-md" @submit="submit">
          <q-select v-model="schoolUserRole.school" :options="schoolOptions"
                    :option-label="option => option.schoolName"
                    :disable="disablePeriodAndSchoolSelections"
                    label="Училище">
            <template v-slot:prepend>
              <q-icon name="school"/>
            </template>
          </q-select>
          <q-select v-model="schoolUserRole.period"
                    :disable="disablePeriodAndSchoolSelections"
                    :option-label="(option:SchoolPeriodWithSchoolIds) => `${option.startYear}/${option.endYear}`"
                    :option-value="(option: SchoolPeriodWithSchoolIds) => {return {
    id: option.id,
    startYear: option.startYear,
    endYear: option.endYear,
    firstSemester: option.firstSemester,
    secondSemester: option.secondSemester
  }
}"
                    :options="schoolPeriodOptions"
                    emit-value
                    label="Учебна година"
                    map-options>
            <template v-slot:prepend>
              <q-icon name="calendar_month"/>
            </template>
            <template v-slot:no-option>
              <q-item>
                <q-item-section class="text-italic text-grey">
                  Изберете училище,за да се покажат учебните години
                </q-item-section>
              </q-item>
            </template>
          </q-select>
          <q-select v-model="schoolUserRole.role" :option-label="option => translationOfRoles[option]"
                    :options="roleOptions" label="Роля">
            <template v-slot:prepend>
              <q-icon name="settings_accessibility"/>
            </template>
          </q-select>
          <div v-if="isDetailsForStudent(schoolUserRole.detailsForUser)">
            <q-select v-model="schoolUserRole.detailsForUser.schoolClass" :option-label="option => option.name"
                      :options="schoolClassesOptions" label="Клас">
              <template v-slot:prepend>
                <q-icon name="settings_accessibility"/>
              </template>
            </q-select>
          </div>
          <div v-if="isDetailsForTeacher(schoolUserRole.detailsForUser)">
            <q-select v-model="schoolUserRole.detailsForUser.qualifiedSubjects"
                      :options="subjects" label="Квалифициран учител по:" multiple stack-labels
                      use-chips>
              <template v-slot:prepend>
                <q-icon name="settings_accessibility"/>
              </template>
            </q-select>
          </div>
          <div v-if="isDetailsForParent(schoolUserRole.detailsForUser)">
            <q-input v-model="schoolUserRole.detailsForUser.child.phoneNumber" class="q-pa-sm"
                     label="Телефонен номер на ученика"
                     mask="##########"/>
            <q-select v-model="schoolUserRole.detailsForUser.child.role.detailsForUser.schoolClass"
                      :option-label="option => option.name"
                      :options="schoolClassesOptions" label="Клас">
              <template v-slot:prepend>
                <q-icon name="settings_accessibility"/>
              </template>
            </q-select>
          </div>

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
import {useDialogPluginComponent, useQuasar} from "quasar";
import {
    DetailsForParent,
    DetailsForStudent,
    DetailsForTeacher,
    isDetailsForParent,
    isDetailsForStudent,
    isDetailsForTeacher,
    OneRoleUser,
    SchoolRole
} from "../model/User";
import {$ref} from "vue/macros";
import {translationOfRoles} from "../utils";
import {SchoolClass} from "../model/SchoolClass";
import {watch} from "vue";
import {SchoolUserRole} from "../model/SchoolUserRole";
import {School} from "../model/School";
import {RequestStatus} from "../model/RequestStatus";
import {SchoolPeriodWithSchoolIds} from "../model/SchoolPeriod";


const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  role: SchoolUserRole,
  schoolOptions: School[],
  schoolPeriodsWithSchoolIds: SchoolPeriodWithSchoolIds[],
  allSchoolClassesOptions: SchoolClass[],
  disablePeriodAndSchoolSelections?: boolean,
  subjects: string[],
  userId: number
}>()

const roleOptions = Object.keys(SchoolRole)
const schoolUserRole = $ref(<SchoolUserRole>{...props.role, status: RequestStatus.PENDING})
let schoolClassesOptions = $ref([...props.allSchoolClassesOptions].filter(it => schoolUserRole?.school?.id ? it.schoolId == schoolUserRole.school.id : false))
let schoolPeriodOptions = $ref([...props.schoolPeriodsWithSchoolIds].filter(it => it.schoolIds.find(schoolId => schoolUserRole?.school?.id ? schoolId == schoolUserRole.school.id : false)))

watch(() => schoolUserRole.school, async () => {
          schoolClassesOptions = [...props.allSchoolClassesOptions].filter(it => schoolUserRole?.school?.id ? it.schoolId == schoolUserRole?.school?.id : false)
          schoolPeriodOptions = [...props.schoolPeriodsWithSchoolIds].filter(it => it.schoolIds.find(schoolId => schoolUserRole?.school?.id ? schoolId == schoolUserRole.school.id : false))
        }
)
watch(() => schoolUserRole.role, () => {
  switch (schoolUserRole.role) {
    case SchoolRole.STUDENT: {
      schoolUserRole.detailsForUser = new DetailsForStudent(<SchoolClass>{}, null)
      break
    }
    case SchoolRole.PARENT: {
      schoolUserRole.detailsForUser = new DetailsForParent(<OneRoleUser><unknown>{role: <SchoolUserRole><unknown>{detailsForUser: new DetailsForStudent(<SchoolClass>{}, null)}})
      break
    }
    case SchoolRole.TEACHER: {
      schoolUserRole.detailsForUser = new DetailsForTeacher([])
      break
    }
    case SchoolRole.ADMIN: {
      schoolUserRole.detailsForUser = null
      break
    }
  }
})

const submit = () => {
  onDialogOK({
    item: {...schoolUserRole, userId: props.userId}
  })
}

</script>

<style scoped>

</style>