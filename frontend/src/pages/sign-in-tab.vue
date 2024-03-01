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
          <q-input filled v-model="newUser.phoneNumber" label="Мобилен телефон" prefix="+359" mask="#########"/>
        </q-form>
      </q-card-section>
      <q-stepper-navigation>
        <q-btn @click="step = 2" color="primary" label="Напред"
               :disable="enableGoingToPersonalInformationStep"/>
      </q-stepper-navigation>
    </q-step>

    <q-step
            :name="2"
            title="Лична информация"
            icon="settings_accessibility"
            :done="step > 2"
    >
      <q-input filled v-model="newUser.firstName" label="Име" class="q-pa-sm"/>
      <q-input filled v-model="newUser.middleName" label="Презиме" class="q-pa-sm"/>
      <q-input filled v-model="newUser.lastName" label="Фамилия" class="q-pa-sm"/>
      <q-input filled v-model="newUser.personalNumber" label="ЕГН" class="q-pa-sm"/>
      <q-input filled v-model="newUser.email" label="Имейл" class="q-pa-sm"/>
      <q-input filled v-model="newUser.address" label="Адрес" class="q-pa-sm"/>
      <q-input filled v-model="newUser.username" label="Потребителско име" class="q-pa-sm"/>
      <q-input filled v-model="newUser.password" label="Парола" class="q-pa-sm"/>

      <q-stepper-navigation>
        <q-btn @click="step = 3" color="primary" label="Напред" :disable="!enableGoingToSchoolInformationStep"/>
        <q-btn flat @click="step = 1" color="primary" label="Назад" class="q-ml-sm"/>
      </q-stepper-navigation>
    </q-step>

    <q-step
            :name="3"
            title="Данни за ролята"
            icon="school"
            disable
    >
      <q-btn label="Добави нова роля"
             color="primary" flat
             icon="add_circle_outline"
             @click="addNewRole"
      />
      <div v-for="role in schoolUserRoles">
        {{ role }}
      </div>

      <q-stepper-navigation>
        <q-btn color="primary" label="Регистрирай се"/>
        <q-btn flat @click="step = 2" color="primary" label="Назад" class="q-ml-sm"/>
      </q-stepper-navigation>
    </q-step>
  </q-stepper>
</template>

<script lang="ts" setup>

import {$computed, $ref} from "vue/macros";
import {OneRoleUser} from "../model/User";
import {useQuasar} from "quasar";
import AddRoleDialog from "./add-role-dialog.vue";
import {getAllSchoolClasses, getAllSchools} from "../services/RequestService";
import {SchoolUserRole} from "../model/SchoolUserRole";

const quasar = useQuasar()
const newUser = $ref(<OneRoleUser>{})
const enableGoingToSchoolInformationStep = $computed(() =>
        newUser.firstName &&
        newUser.middleName &&
        newUser.lastName &&
        newUser.personalNumber &&
        newUser.email &&
        newUser.address &&
        newUser.username &&
        newUser.password)
const enableGoingToPersonalInformationStep = $computed(() => !newUser.phoneNumber && newUser.phoneNumber?.length !== 8)
const step = $ref(1)
const schoolUserRoles: SchoolUserRole[] = $ref([])

const addNewRole = async () => quasar.dialog({
  component: AddRoleDialog,
  componentProps: {
    schoolOptions: await getAllSchools(),
    allSchoolClassesOptions: await getAllSchoolClasses()
  },
}).onOk(async (payload) => {
  schoolUserRoles.push(payload.item)
})

</script>

<style scoped>

</style>