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
              icon="vpn_key"
              :done="step > 2"
      >
        <q-select v-model="selectedRole" :options="userRoles"
                  :option-label="option => option.schoolName"
                  label="Роля"/>

        <q-stepper-navigation>
          <q-btn @click="loginAfterSelectingRole" color="primary" :disable="!selectedRole" label="Напред"/>
        </q-stepper-navigation>
      </q-step>
    </q-stepper>
  </q-card-section>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {getAllUserRoles, login, loginAfterSelectedRole} from "../services/RequestService";
import {SchoolUserRole} from "../model/SchoolUserRole";
import {router} from "../router";

let step = $ref(1)
const username = $ref(null);
const password = $ref(null);
let userRoles = $ref<SchoolUserRole[]>([])
const selectedRole = $ref<SchoolUserRole | null>(null)
const loginClick = async () => {
  await login(username, password)
          .then(async r => {
            // const authResponse: AuthenticationResponse = <Success>r.data
            // storeUser(authResponse.user)
            // applicationInReadOnlyMode.value = authResponse.user.role == RvmRole.VIEWER;
            // loading = false
            //
            step = 2
            userRoles = await getAllUserRoles()
          }).catch(e => {
            Promise.reject("Unauthenticated");
          })
}
const loginAfterSelectingRole = async () => {
    console.log(selectedRole)
  await loginAfterSelectedRole(selectedRole?.id!!).then(r =>
          router.push({path: '/'})
  )

}
</script>

<style scoped>

</style>