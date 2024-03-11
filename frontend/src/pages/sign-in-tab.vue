<template>
  <q-stepper
          v-model="step"
          vertical
          color="primary"
          animated
  >
      <q-step
              :done="step > 1"
              :name="1"
              icon="call"
              title="Мобилен телефон"
    >
          <q-card-section>
              <div class="q-gutter-md">
                  <q-input v-model="user.phoneNumber" filled label="Мобилен телефон" mask="##########"/>
              </div>
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
          <div v-if="user.id != null" class="text-negative">Потребител с посочения телефонен номер вече съществува</div>
          {{ user }}
          <q-input v-model="user.firstName" :readonly="user.id != null" class="q-pa-sm" filled
                   label="Име"/>
          <q-input v-model="user.middleName" :readonly="user.id != null" class="q-pa-sm" filled
                   label="Презиме"/>
          <q-input v-model="user.lastName" :readonly="user.id != null" class="q-pa-sm" filled
                   label="Фамилия"/>
          <q-input v-model="user.personalNumber" :readonly="user.id != null" class="q-pa-sm" filled
                   label="ЕГН"/>
          <q-input v-model="user.email" :readonly="user.id != null" class="q-pa-sm" filled
                   label="Имейл"/>
          <q-input v-model="user.address" :readonly="user.id != null" class="q-pa-sm" filled
                   label="Адрес"/>
          <q-input v-model="user.username" :readonly="user.id != null" class="q-pa-sm"
                   filled
                   label="Потребителско име"/>
          <q-input v-if="user.id == null" v-model="user.password" class="q-pa-sm" filled
                   label="Парола"
                   type="password"/>
          <q-select v-model="user.gender" :option-label="option=>translationOfGender[option]" :options="Object.keys(Gender)" :readonly="user.id != null" class="q-pa-sm"
                    filled
                    label="Пол"/>
          <q-stepper-navigation>
              <q-btn :disable="!enableGoingToSchoolInformationStep" color="primary" label="Напред" @click="step = 3"/>
              <q-btn class="q-ml-sm" color="primary" flat label="Назад" @click="goBackToPhoneNumberInputStep()"/>
          </q-stepper-navigation>
      </q-step>

      <q-step
              :name="3"
              disable
              icon="school"
              title="Данни за ролята"
      >
          <q-list v-if="user.roles?.length>0" bordered dense padding>
              <q-item-label header>Роли</q-item-label>
              <q-separator/>
              <q-item v-for="role in user.roles" v-ripple class="q-pb-none">
                  <q-item-section>
                      <div class="row q-pt-xs q-pl-sm" style="text-align: center">
                          <div :class="`col-9 ${role.id==null ? 'text-primary' : ''}`">{{
                              constructSchoolUserRoleMessageWithSchoolAndPeriodInformation(role)
                              }}
                          </div>
                          <div class="col"/>
                          <div class="col-2">
                              <q-btn :disable="role.id != null" flat icon="delete" round size="sm" text-color="negative"
                                     @click="deleteRole(role)"/>
                              <q-btn :disable="role.id != null" flat icon="edit" round size="sm" text-color="secondary"
                                     @click="updateRole(role)"/>
                              <q-tooltip v-if="role.id != null" anchor="center middle" self="top middle" type="a">Не е
                                  възможно да
                                  редактирате данни, които са въведени в базата данни
                              </q-tooltip>

                          </div>
                      </div>
                      <q-separator class="q-ma-none"/>
                  </q-item-section>
              </q-item>
          </q-list>
          <q-btn color="primary"
                 flat icon="add_circle_outline"
                 label="Добави нова роля"
                 @click="addNewRole"
          />


          <q-stepper-navigation>
              <q-btn color="primary" label="Регистрирай се" @click="sendRegistrationRequest()"/>
              <q-btn class="q-ml-sm" color="primary" flat label="Назад" @click="step = 2"/>
          </q-stepper-navigation>
      </q-step>
  </q-stepper>
</template>

<script lang="ts" setup>

import {$computed, $ref} from "vue/macros";
import {DetailsForParent, DetailsForStudent, Gender, SchoolRole, User} from "../model/User";
import {useQuasar} from "quasar";
import AddRoleDialog from "./add-role-dialog.vue";
import {
    createRequestFromUser,
    findStudentByPhoneNumberPeriodAndSchoolClass,
    findUserWithAllItsRolesByPhoneNumber,
    getAllSchoolClasses,
    getAllSchoolPeriodsWithTheSchoolsTheyAreStarted,
    getAllSchools
} from "../services/RequestService";
import {constructSchoolUserRoleMessageWithSchoolAndPeriodInformation, SchoolUserRole} from "../model/SchoolUserRole";
import {confirmActionPromiseDialogWithCancelButton, translationOfGender} from "../utils";
import {useRouter} from "vue-router";

const router = useRouter()
const quasar = useQuasar()
let user = $ref<Partial<User>>({
    roles: <SchoolUserRole[]>[]
})
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
const enableGoingToPersonalInformationStep = $computed(() => {
    return !(user.phoneNumber && user.phoneNumber?.length == 10)
})
let step = $ref(1)
const allSchools = await getAllSchools()
const allSchoolClasses = await getAllSchoolClasses()
const allSchoolPeriods = await getAllSchoolPeriodsWithTheSchoolsTheyAreStarted()
const addNewRole = async () => quasar.dialog({
    component: AddRoleDialog,
    componentProps: {
        role: <SchoolUserRole>{},
        schoolOptions: allSchools,
        allSchoolClassesOptions: allSchoolClasses,
        schoolPeriodsWithSchoolIds: allSchoolPeriods
    },
}).onOk(async (payload) => {
    const schoolUserRole = payload.item as SchoolUserRole
    if (schoolUserRole.role == SchoolRole.PARENT) {
        const detailsForParent = schoolUserRole.detailsForUser as DetailsForParent
        schoolUserRole.detailsForUser = new DetailsForParent(await findStudentByPhoneNumberPeriodAndSchoolClass(detailsForParent.child?.phoneNumber, schoolUserRole.period.id, (detailsForParent.child?.role.detailsForUser as DetailsForStudent).schoolClass?.id))
        user?.roles!!.push(schoolUserRole)
    } else {
        user?.roles!!.push(payload.item)
    }
})

const deleteRole = (role) => {
    user.roles?.splice(user.roles.indexOf(role), 1);
}
const updateRole = async (role) => {
    const roleIndex = user.roles?.indexOf(role)
    await quasar.dialog({
        component: AddRoleDialog,
        componentProps: {
            role: role,
            schoolOptions: allSchools,
            allSchoolClassesOptions: allSchoolClasses,
            schoolPeriodsWithSchoolIds: allSchoolPeriods
        },
    }).onOk(async (payload) => {
        const schoolUserRole = payload.item as SchoolUserRole
        if (schoolUserRole.role == SchoolRole.PARENT) {
            const detailsForParent = schoolUserRole.detailsForUser as DetailsForParent
            schoolUserRole.detailsForUser = new DetailsForParent(await findStudentByPhoneNumberPeriodAndSchoolClass(detailsForParent.child?.phoneNumber, schoolUserRole.period.id, (detailsForParent.child?.role.detailsForUser as DetailsForStudent).schoolClass?.id))
            user.roles = user.roles?.map((role, index) => {
                if (index == roleIndex) {
                    return schoolUserRole
                } else {
                    return role
                }
            })
        } else {
            user.roles = user.roles?.map((role, index) => {
                if (index == roleIndex) {
                    return payload.item
                } else {
                    return role
                }
            })
        }
    })
}
const continuingToPersonalInformationStep = async () => {
    const userFetchedFromDatabase = await findUserWithAllItsRolesByPhoneNumber(user!!.phoneNumber)
    if (userFetchedFromDatabase?.id) {
        user = userFetchedFromDatabase
    }
    step = 2
}

const sendRegistrationRequest = async () => {
    await createRequestFromUser(user).then(
            async () => {
                await confirmActionPromiseDialogWithCancelButton("Вашата заявка е изпратена",
                        "Можете да влезнете в Е-училище, когато админ одобри вашата заявка")
                await router.push({path: "/login"})
            }
    )
}
const goBackToPhoneNumberInputStep = () => {
    step = 1
}

</script>

<style scoped>

</style>