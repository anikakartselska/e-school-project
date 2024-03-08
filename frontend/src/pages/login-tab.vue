<template>
  <q-card-section>
    <q-stepper
            v-model="step"
            vertical
            color="primary"
            animated
    >
      <q-step
              :name="1"
              icon="login"
              title="Въвеждане на парола и имейл"
              :done="step > 1"
      >
        <q-form class="q-gutter-md">
          <q-input filled v-model="username" label="Потребителско име" lazy-rules/>
          <q-input
                  type="password"
                  filled
                  v-model="password"
                  label="Парола"
                  lazy-rules
          />
        </q-form>
        <q-stepper-navigation>
          <q-btn @click="loginClick()" color="primary" label="Напред"/>
        </q-stepper-navigation>
      </q-step>

      <q-step
              :name="2"
              title="Избиране на роля"
              icon="vpn_key"
              :done="step > 2"
      >
        <q-select v-model="selectedPeriod"
                  :option-label="(option:SchoolPeriod) => `${option.startYear.substring(0,4)}/${option.endYear.substring(0,4)}`"
                  :options="schoolPeriods"
                  label="Учебна година"/>
        <q-select v-model="selectedRole" :disable="selectedPeriod==null"
                  :option-label="option => constructSchoolUserRoleMessage(option)"
                  :options="userRolesFilteredBySelectedPeriod"
                  label="Роля"/>

        <q-stepper-navigation>
          <q-btn :disable="!selectedRole" color="primary" label="Напред" @click="loginAfterSelectingRole"/>
        </q-stepper-navigation>
      </q-step>
    </q-stepper>
  </q-card-section>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {getAllSchoolPeriods, getAllUserRoles, login, loginAfterSelectedRole} from "../services/RequestService";
import {constructSchoolUserRoleMessage, SchoolUserRole} from "../model/SchoolUserRole";
import {router} from "../router";
import {storeUser, updateUserInLocalStorage} from "../services/LocalStorageService";
import {AuthenticationResponse, Success} from "../model/AuthenticationResponse";
import {SchoolPeriod} from "../model/SchoolPeriod";
import {watch} from "vue";

let step = $ref(1)
const username = $ref(null);
const password = $ref(null);
let userRoles = $ref<SchoolUserRole[]>([])
const selectedRole = $ref<SchoolUserRole | null>(null)
let schoolPeriods = $ref<SchoolPeriod[]>([])
const selectedPeriod = $ref<SchoolPeriod | null>(null)
let userRolesFilteredBySelectedPeriod = $ref<SchoolUserRole[]>([])

watch(() => selectedPeriod, () => {
  userRolesFilteredBySelectedPeriod = userRoles.filter(role => role.period.id == selectedPeriod?.id)
})
const loginClick = async () => {
  await login(username, password)
          .then(async r => {
            const authResponse: AuthenticationResponse = <Success>r.data
            storeUser(authResponse.user)
            // applicationInReadOnlyMode.value = authResponse.user.role == RvmRole.VIEWER;
            // loading = false
            step = 2
            userRoles = await getAllUserRoles(authResponse.user.id)
            schoolPeriods = await getAllSchoolPeriods()
          }).catch(e => {
            Promise.reject("Unauthenticated");
          })
}
const loginAfterSelectingRole = async () => {
  await loginAfterSelectedRole(selectedRole?.id!!, selectedPeriod?.id!!).then(r => {
            const authResponse: AuthenticationResponse = <Success>r.data
            updateUserInLocalStorage(authResponse.user)
            router.push({path: '/'})
          }
  )

}
</script>

<style scoped>

</style>