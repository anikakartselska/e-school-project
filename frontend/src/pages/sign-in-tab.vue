<template>
    <q-stepper
          v-model="step"
          vertical
          color="primary"
          animated
  >
    <q-step
            :name="1"
            title="Мобилен телефон"
            icon="call"
            :done="step > 1"
    >
      <q-card-section>
        <q-form class="q-gutter-md">
          <q-input v-model="newUser.phoneNumber" filled label="Мобилен телефон" mask="##########"/>
        </q-form>
      </q-card-section>
      <q-stepper-navigation>
          <q-btn :disable="enableGoingToPersonalInformationStep" color="primary" label="Напред"
                 @click="continuingToPersonalInformationStep"/>
      </q-stepper-navigation>
    </q-step>

        <q-step
                :done="step > 2"
                :name="2"
                icon="settings_accessibility"
                title="Лична информация"
        >
            <div v-if="existingUserWithInputtedPhoneNumber">
                <div class="text-negative">Потребител с посочения телефонен номер вече съществува</div>
                <q-input v-model="existingUserWithInputtedPhoneNumber.firstName" class="q-pa-sm" filled label="Име"
                         readonly/>
                <q-input v-model="existingUserWithInputtedPhoneNumber.middleName" class="q-pa-sm" filled label="Презиме"
                         readonly/>
                <q-input v-model="existingUserWithInputtedPhoneNumber.lastName" class="q-pa-sm" filled label="Фамилия"
                         readonly/>
                <q-input v-model="existingUserWithInputtedPhoneNumber.personalNumber" class="q-pa-sm" filled label="ЕГН"
                         readonly/>
                <q-input v-model="existingUserWithInputtedPhoneNumber.email" class="q-pa-sm" filled label="Имейл"
                         readonly/>
                <q-input v-model="existingUserWithInputtedPhoneNumber.address" class="q-pa-sm" filled label="Адрес"
                         readonly/>
                <q-input v-model="existingUserWithInputtedPhoneNumber.username" class="q-pa-sm" filled
                         label="Потребителско име"
                         readonly/>
                <q-input v-model="existingUserWithInputtedPhoneNumber.gender" class="q-pa-sm" filled label="Потребителско име"
                         readonly/>
            </div>
            <div v-else>
                <q-input v-model="newUser.firstName" class="q-pa-sm" filled label="Име"/>
                <q-input v-model="newUser.middleName" class="q-pa-sm" filled label="Презиме"/>
                <q-input v-model="newUser.lastName" class="q-pa-sm" filled label="Фамилия"/>
                <q-input v-model="newUser.personalNumber" class="q-pa-sm" filled label="ЕГН"/>
                <q-input v-model="newUser.email" class="q-pa-sm" filled label="Имейл"/>
                <q-input v-model="newUser.address" class="q-pa-sm" filled label="Адрес"/>
                <q-input v-model="newUser.username" class="q-pa-sm" filled label="Потребителско име"/>
                <q-input v-model="newUser.password" class="q-pa-sm" filled label="Парола"/>
                <q-select v-model="existingUserWithInputtedPhoneNumber.gender" :options="Object.keys(Gender)" class="q-pa-sm"
                          filled
                          label="Пол"/>
            </div>
            <q-stepper-navigation>
                <q-btn :disable="!enableGoingToSchoolInformationStep" color="primary" label="Напред" @click="step = 3"/>
                <q-btn class="q-ml-sm" color="primary" flat label="Назад" @click="step = 1"/>
            </q-stepper-navigation>
        </q-step>

        <q-step
                :name="3"
                disable
                icon="school"
                title="Данни за ролята"
        >
            <q-list v-if="schoolUserRoles.length>0" bordered dense padding>
                <q-item-label header>Роли</q-item-label>
                <q-separator/>
                <q-item v-for="role in schoolUserRoles" v-ripple>
                    <q-item-section>
                        <div class="row">
                            <q-btn flat icon="delete" round size="sm" text-color="negative"/>
                            <q-btn flat icon="edit" round size="sm" text-color="secondary"/>
                            <div class="q-pt-xs q-pl-sm">{{
                                constructSchoolUserRoleMessageWithSchoolAndPeriodInformation(role)
                                }}
                            </div>
                        </div>
                    </q-item-section>
                    <q-separator/>
                </q-item>
            </q-list>
            <q-btn color="primary"
                   flat icon="add_circle_outline"
                   label="Добави нова роля"
                   @click="addNewRole"
            />


            <q-stepper-navigation>
                <q-btn color="primary" label="Регистрирай се"/>
                <q-btn class="q-ml-sm" color="primary" flat label="Назад" @click="step = 2"/>
      </q-stepper-navigation>
    </q-step>
  </q-stepper>
</template>

<script lang="ts" setup>

import {$computed, $ref} from "vue/macros";
import {DetailsForParent, DetailsForStudent, Gender, OneRoleUser, SchoolRole, User} from "../model/User";
import {useQuasar} from "quasar";
import AddRoleDialog from "./add-role-dialog.vue";
import {
    findStudentByPhoneNumberPeriodAndSchoolClass,
    findUserWithAllItsRolesByPhoneNumber,
    getAllSchoolClasses,
    getAllSchoolPeriodsWithTheSchoolsTheyAreStarted,
    getAllSchools
} from "../services/RequestService";
import {constructSchoolUserRoleMessageWithSchoolAndPeriodInformation, SchoolUserRole} from "../model/SchoolUserRole";

const quasar = useQuasar()
let newUser = $ref(<OneRoleUser>{})
let existingUserWithInputtedPhoneNumber = $ref(<User | null>null)
const enableGoingToSchoolInformationStep = $computed(() =>
        true)
// newUser.firstName &&
// newUser.middleName &&
// newUser.lastName &&
// newUser.personalNumber &&
// newUser.email &&
// newUser.address &&
// newUser.username &&
// newUser.password)
const enableGoingToPersonalInformationStep = $computed(() => !newUser.phoneNumber && newUser.phoneNumber?.length !== 8)
let step = $ref(1)
const schoolUserRoles: SchoolUserRole[] = $ref([])

const addNewRole = async () => quasar.dialog({
    component: AddRoleDialog,
    componentProps: {
        schoolOptions: await getAllSchools(),
        allSchoolClassesOptions: await getAllSchoolClasses(),
        schoolPeriodsWithSchoolIds: await getAllSchoolPeriodsWithTheSchoolsTheyAreStarted()
    },
}).onOk(async (payload) => {
    debugger
    const schoolUserRole = payload.item as SchoolUserRole
    if (schoolUserRole.role == SchoolRole.PARENT) {
        const detailsForParent = schoolUserRole.detailsForUser as DetailsForParent
        schoolUserRole.detailsForUser = new DetailsForParent(await findStudentByPhoneNumberPeriodAndSchoolClass(detailsForParent.child?.phoneNumber, schoolUserRole.period.id, (detailsForParent.child?.role.detailsForUser as DetailsForStudent).schoolClass?.id))
        schoolUserRoles.push(schoolUserRole)
    } else {
        schoolUserRoles.push(payload.item)
    }
})

const continuingToPersonalInformationStep = async () => {
    const userFetchedFromDatabase = await findUserWithAllItsRolesByPhoneNumber(newUser.phoneNumber)
    if (userFetchedFromDatabase != null) {
        existingUserWithInputtedPhoneNumber = userFetchedFromDatabase
        console.log(existingUserWithInputtedPhoneNumber)
    }
    step = 2
}

</script>

<style scoped>

</style>