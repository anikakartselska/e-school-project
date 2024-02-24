<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
        <div class="text-h6">Добави роля</div>
        <q-btn v-close-popup dense flat icon="close" round/>
      </q-card-section>
      <q-card-section>
        <q-form class="q-gutter-md" @submit="submit">
          <q-select v-model="schoolUserRole.school" :options="schoolOptions"
                    :option-label="option => option.schoolName"
                    label="Училище">
            <template v-slot:prepend>
              <q-icon name="school"/>
            </template>
          </q-select>
          <q-select v-model="schoolUserRole.role" :options="roleOptions"
                    :option-label="option => translationOfRoles[option]" label="Роля">
            <template v-slot:prepend>
              <q-icon name="settings_accessibility"/>
            </template>
          </q-select>
          <div v-if="isDetailsForStudent(schoolUserRole.detailsForUser)">
            <q-select v-model="schoolUserRole.detailsForUser.schoolClass" :options="schoolClassesOptions"
                      :option-label="option => option.name" label="Клас">
              <template v-slot:prepend>
                <q-icon name="settings_accessibility"/>
              </template>
            </q-select>
          </div>
          <div v-if="isDetailsForParent(schoolUserRole.detailsForUser)">
            <q-input filled v-model="schoolUserRole.detailsForUser.children.firstName" label="Име" class="q-pa-sm"/>
            <q-input filled v-model="schoolUserRole.detailsForUser.children.lastName" label="Фамилия" class="q-pa-sm"/>
            <q-input filled v-model="schoolUserRole.detailsForUser.children.phoneNumber" label="Адрес" class="q-pa-sm"/>
            <q-select v-model="schoolUserRole.detailsForUser.children.details.schoolClass"
                      :options="schoolClassesOptions"
                      :option-label="option => option.name" label="Клас">
              <template v-slot:prepend>
                <q-icon name="settings_accessibility"/>
              </template>
            </q-select>
          </div>

          <q-card-actions align="right">
            <q-btn label="Добави" color="primary" @click="submit"/>
            <q-btn label="Отказ" class="q-ml-sm" color="primary" flat @click="onDialogCancel"/>
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
  isDetailsForParent,
  isDetailsForStudent,
  SchoolRole,
  User
} from "../model/User";
import {$ref} from "vue/macros";
import {translationOfRoles} from "../utils";
import {SchoolClass} from "../model/SchoolClass";
import {watch} from "vue";
import {SchoolUserRole} from "../model/SchoolUserRole";
import {School} from "../model/School";
import {RequestStatus} from "../model/RequestStatus";


const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const props = defineProps<{
  schoolOptions: School[],
  allSchoolClassesOptions: SchoolClass[]
}>()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const roleOptions = Object.keys(SchoolRole)
let schoolClassesOptions = $ref(<SchoolClass[]>[])
const schoolUserRole = $ref(<SchoolUserRole>{status: RequestStatus.PENDING})

watch(() => schoolUserRole.school, async () => {
          schoolClassesOptions = [...props.allSchoolClassesOptions].filter(it => it.schoolId == schoolUserRole.school.id)
        }
)
watch(() => schoolUserRole.role, () => {
  switch (schoolUserRole.role) {
    case SchoolRole.STUDENT: {
      schoolUserRole.detailsForUser = new DetailsForStudent(<SchoolClass>{}, null)
      break
    }
    case SchoolRole.PARENT: {
      schoolUserRole.detailsForUser = new DetailsForParent(<User><unknown>{details: new DetailsForStudent(<SchoolClass>{}, null)})
      break
    }
    case SchoolRole.TEACHER:
    case SchoolRole.ADMIN: {
      schoolUserRole.detailsForUser = null
      break
    }
  }
})

const submit = () => {
  onDialogOK({
    item: schoolUserRole
  })
}

</script>

<style scoped>

</style>