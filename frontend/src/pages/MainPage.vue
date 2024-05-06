<template>
  <q-layout view="lHh LpR lFf">
    <q-ajax-bar
            ref="bar"
            color="bg-red-6"
            position="bottom"
            size="5px"

    />
    <q-header
            reveal
            :class="$q.dark.isActive ? 'header_dark' : 'header_normal'"
    >
      <q-toolbar>
        <q-btn
                class="q-mr-sm"
                dense
                flat
                icon="menu"
                round
                @click="left = !left"
        />
        <q-toolbar-title>Електронен дневник</q-toolbar-title>
        <q-btn :label="`${getCurrentUser().role.period.startYear.substring(0,4)}/${getCurrentUser().role.period.endYear.substring(0,4)}`"
               dense flat
               icon="switch_account">
          <q-menu>
            <div class="row no-wrap q-pa-md">
              <div class="column">
                <div class="text-h6 q-mb-md">Смяна на роля</div>
                <q-select v-model="selectedPeriod"
                          :option-label="(option:SchoolPeriod) => `${option.startYear.substring(0,4)}/${option.endYear.substring(0,4)}`"
                          :options="schoolPeriods"
                          label="Учебна година"/>
                <q-select v-model="selectedRole" :disable="selectedPeriod==null"
                          :option-label="option => constructSchoolUserRoleMessage(option)"
                          :options="userRolesFilteredBySelectedPeriod"
                          label="Роля"/>
                <q-item>
                  <q-item-section>
                    <q-btn color="primary" dense flat label="Промени роля" @click="changeUserRole()"/>
                  </q-item-section>
                </q-item>
                <q-separator/>
                <q-item>
                  <q-item-section>
                    <q-btn color="primary" dense flat label="Промени парола" @click="resetUserPassword()"/>
                  </q-item-section>
                </q-item>
              </div>
            </div>
          </q-menu>
        </q-btn>
        <q-btn
                :icon="$q.dark.isActive ? 'nights_stay' : 'wb_sunny'"
                class="q-mr-xs"
                flat
                round
                @click="$q.dark.toggle()"
        />
        <q-btn class="q-mr-xs" dense flat icon="search" round/>
        <q-btn
                flat
                round
                dense
                icon="logout"
                @click="onLogoutClick()"
        />
      </q-toolbar>
    </q-header>
    <q-drawer
            class="left-navigation text-white"
            show-if-above
            v-model="left"
            style="background-image: url('../src/assets/schoolMenuBackground.png'); background-size: cover;"
            side="left"
            elevated
    >
      <div
              class="full-height drawer_normal"
      >
        <div style="height: calc(100% - 117px);padding:10px;">
          <q-toolbar>
            <q-avatar v-if="currentUserFile!=null"
                      text-color="white">
              <q-img
                      :src="imageUrl"
              ></q-img>
            </q-avatar>
            <q-avatar v-else color="cyan-2" text-color="white">
              {{ getCurrentUser().firstName[0] }}{{ getCurrentUser().lastName[0] }}
            </q-avatar>
            <q-toolbar-title>{{ currentUser.firstName }} {{ currentUser.lastName }}</q-toolbar-title>
          </q-toolbar>
          <hr/>
          <q-scroll-area style="height:100%;">
            <q-list v-for="page in pages" padding>
              <q-item
                      active-class="tab-active"
                      :to="page.to"
                      exact
                      class="navigation-item"
                      clickable
                      v-ripple
              >
                <q-item-section avatar>
                  <q-icon name="dashboard"/>
                </q-item-section>
                <q-item-section>
                  {{ page.label }}
                </q-item-section>
              </q-item>
            </q-list>
          </q-scroll-area>
        </div>
      </div>
    </q-drawer>
    <q-page-container>
      <router-view v-slot="{ Component }">
        <template v-if="Component">
          <suspense>
            <component :is="Component"></component>
            <template #fallback>
              <div class="fixed-center">
                <q-spinner
                        :thickness="2"
                        color="primary"
                        size="5.5em"
                />
              </div>
            </template>
          </suspense>
        </template>
      </router-view>
    </q-page-container>
  </q-layout>
</template>

<script lang="ts" setup>
import {useRouter} from "vue-router";
import {$ref} from "vue/macros";
import {clearUserStorage, getCurrentUser, updateUserInLocalStorage} from "../services/LocalStorageService";
import {
    getAllSchoolPeriods,
    getAllUserRoles,
    getUserProfilePicture,
    loginAfterSelectedRole,
    logout
} from "../services/RequestService";
import {onBeforeMount, watch} from "vue";
import {constructSchoolUserRoleMessage, SchoolUserRole} from "../model/SchoolUserRole";
import {SchoolPeriod} from "../model/SchoolPeriod";
import {confirmActionPromiseDialogWithCancelButton} from "../utils";
import {AuthenticationResponse, Success} from "../model/AuthenticationResponse";
import {periodId, schoolId} from "../model/constants";
import {useQuasar} from "quasar";
import PasswordChangeDialog from "./reset-password/password-change-dialog.vue";

const router = useRouter();
const quasar = useQuasar()
const onLogoutClick = async () => {
  await logout().then(async r => {
    clearUserStorage()
    await router.push('/login')
  })
}

let currentUser = getCurrentUser();
let currentUserFile = $ref<File | null>(null)
let imageUrl = $ref<string | null>(null)
let userRoles = $ref(<SchoolUserRole[]>[]);
let schoolPeriods = $ref(<SchoolPeriod[]>[]);
onBeforeMount(async () => {
  currentUser = getCurrentUser()
  await load()
  imageUrl = currentUserFile ? window.URL.createObjectURL(currentUserFile) : ''
  console.log(currentUserFile)

})

const load = async () => {
  userRoles = await getAllUserRoles(currentUser.id)
  schoolPeriods = await getAllSchoolPeriods()
  userRolesFilteredBySelectedPeriod = userRoles.filter(role => role.period.id == selectedPeriod?.id)
  currentUserFile = await getUserProfilePicture(currentUser.id)
}

let userRolesFilteredBySelectedPeriod = $ref<SchoolUserRole[]>([])
let selectedRole = $ref<SchoolUserRole | null>(currentUser.role)
const selectedPeriod = $ref<SchoolPeriod | null>(currentUser.role.period)
watch(() => selectedPeriod, () => {
  selectedRole = null
  userRolesFilteredBySelectedPeriod = userRoles.filter(role => role.period.id == selectedPeriod?.id)
})

const changeUserRole = async () => {
  await confirmActionPromiseDialogWithCancelButton("Смяна на роля", `Сигурни ли сте, че искате да влезнете в приложението като ${constructSchoolUserRoleMessage(selectedRole)}`)
  await loginAfterSelectedRole(selectedRole?.id!!, selectedPeriod?.id!!).then(async r => {
            const authResponse: AuthenticationResponse = <Success>r.data
            updateUserInLocalStorage(authResponse.user)
            await router.push({path: '/'})
          }
  )
}
const resetUserPassword = () => {
  quasar.dialog({
    component: PasswordChangeDialog,
  }).onOk(async (payload) => {

  })
}
const left = $ref(true)
const pages = [
    {to: `/school-page/${schoolId.value}`, label: "Училище", show: true},
    {to: `/users/${periodId.value}/${schoolId.value}/all`, label: "Потребители", show: true},
    {to: `/requests/${periodId.value}/${schoolId.value}/user-requests`, label: "Заявки", show: true},
    {to: `/school-classes/${periodId.value}/${schoolId.value}`, label: "Класове", show: true},
    {to: `/school-classes-plans/${schoolId.value}`, label: "Учебни планове", show: true},
    {to: `/calendar`, label: "Учебен Календар", show: true},
    {to: `/program`, label: "Седмичен разпис", show: true},
]

</script>


<style>
.drawer_normal {
  background-color: rgba(1, 1, 1, 0.75);
}

.drawer_dark {
  background-color: #010101f2;
}

.navigation-item {
  border-radius: 5px;
}

.tab-active {
  background-color: green;
}

body {
  background: #f1f1f1 !important;
}

.header_normal {
  background: linear-gradient(
          145deg,
          rgb(32, 106, 80) 15%,
          rgb(21, 57, 102) 70%
  );
}

.header_dark {
  background: linear-gradient(145deg, rgb(61, 14, 42) 15%, rgb(14, 43, 78) 70%);
}

</style>

<style lang="sass" scoped>
.box-shadow
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2)

.toggle-boarder
  border: 1px solid
  color: #0e6be7

.body--light
  .GNL
    &__toolbar
      height: 64px

    &__toolbar-input
      width: 55%

    &__drawer-item
      color: #606061
      line-height: 24px
      border-radius: 0 24px 24px 0
      margin-right: 12px

      .q-item__label
        letter-spacing: .01785714em
        font-size: .875rem
        font-weight: 500
        line-height: 1.25rem


    &__drawer-footer-link
      text-decoration: none
      font-weight: 500
      font-size: .75rem

      &:hover
        color: #000

.body--dark
  .GNL
    &__toolbar
      height: 64px

    &__toolbar-input
      width: 55%

    &__drawer-item
      line-height: 24px
      border-radius: 0 24px 24px 0
      margin-right: 12px

      .q-item__label
        color: white
        letter-spacing: .01785714em
        font-size: .875rem
        font-weight: 500
        line-height: 1.25rem


    &__drawer-footer-link
      color: lightgrey
      text-decoration: none
      font-weight: 500
      font-size: .75rem

      &:hover
        color: #000
</style>
