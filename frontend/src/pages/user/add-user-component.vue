<template>
  <q-stepper
          v-model="step"
          animated
          color="primary"
          vertical
  >
    <q-step
            :done="step > 1"
            :name="1"
            icon="call"
            title="Мобилен телефон"
    >
      <q-card-section>
        <div class="q-gutter-md">
            <q-input v-model="user.phoneNumber" filled label="Мобилен телефон" mask="##########"
                     @update:model-value="existingUserByPhoneNumber = false"/>
        </div>
          <span v-if="!inDialog && existingUserByPhoneNumber" class="text-negative">Потребител с посочения телефонен номер вече съществува</span>
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
      <q-input v-if="user.id == null && !props.inDialog" v-model="user.password" class="q-pa-sm" filled
               label="Парола"
               type="password"/>
      <q-select v-model="user.gender" :option-label="option=>translationOfGender[option]" :options="Object.keys(Gender)"
                :readonly="user.id != null" class="q-pa-sm"
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
        <q-btn :disable="!enableRequestSending" :label="inDialog ? 'Изпрати заявки':'Регистрирай се'"
               color="primary"
               @click="sendRegistrationRequest()"/>
        <q-btn class="q-ml-sm" color="primary" flat label="Назад" @click="step = 2"/>
      </q-stepper-navigation>
    </q-step>
  </q-stepper>
</template>

<script lang="ts" setup>


import {constructSchoolUserRoleMessageWithSchoolAndPeriodInformation, SchoolUserRole} from "../../model/SchoolUserRole";
import {DetailsForParent, DetailsForStudent, Gender, SchoolRole, User} from "../../model/User";
import {useQuasar} from "quasar";
import {useRouter} from "vue-router";
import {$computed, $ref} from "vue/macros";
import {
    createRequestFromUser,
    createUser,
    findIfThereIsAnExistingUserByPhoneNumber,
    findStudentByPhoneNumberPeriodAndSchoolClass,
    findUserWithAllItsRolesByPhoneNumber,
    getAllSchoolClasses,
    getAllSchoolPeriodsWithTheSchoolsTheyAreStarted,
    getAllSchools,
    getAllSubjects
} from "../../services/RequestService";
import AddRoleDialog from "../add-role-dialog.vue";
import {confirmActionPromiseDialogWithCancelButton, translationOfGender} from "../../utils";
import {getCurrentUser} from "../../services/LocalStorageService";
import {RequestStatus} from "../../model/RequestStatus";

const props = defineProps<{
  inDialog?: boolean;
  submitted?: boolean;
  newUser: User
}>()

const emits = defineEmits<{
  (e: 'update:submitted', value: boolean): void
  (e: 'update:newUser', value: User): void
}>();

const router = useRouter()
const quasar = useQuasar()
let user = $ref<Partial<User>>({
  ...props.newUser,
  roles: <SchoolUserRole[]>[],
  status: RequestStatus.PENDING
})
const enableGoingToSchoolInformationStep = $computed(() => {
  return user.firstName &&
          user.middleName &&
          user.lastName &&
          user.personalNumber &&
          user.email &&
          user.address &&
          user.username && user.gender &&
          (user.id != null || user.password != null || props.inDialog)
})


const enableGoingToPersonalInformationStep = $computed(() => {
  return !(user.phoneNumber && user.phoneNumber?.length == 10)
})

const enableRequestSending = $computed(() => {
  if (user.roles) {
    return user.roles.length > 0
  } else {
    return false
  }
})
let step = $ref(1)
const allSchools = await getAllSchools()
const allSchoolClasses = await getAllSchoolClasses()
const allSchoolPeriods = await getAllSchoolPeriodsWithTheSchoolsTheyAreStarted()
const allSubjects = await getAllSubjects()
const addNewRole = async () => quasar.dialog({
  component: AddRoleDialog,
  componentProps: {
    role: <SchoolUserRole>{},
    schoolOptions: allSchools,
    allSchoolClassesOptions: allSchoolClasses,
    schoolPeriodsWithSchoolIds: allSchoolPeriods,
    subjects: allSubjects,
    userId: null
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
      schoolPeriodsWithSchoolIds: allSchoolPeriods,
      subjects: allSubjects,
      userId: null
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
let existingUserByPhoneNumber = $ref(false)

const continuingToPersonalInformationStep = async () => {
    if (!props.inDialog) {
        existingUserByPhoneNumber = await findIfThereIsAnExistingUserByPhoneNumber(user!!.phoneNumber)
        if (!existingUserByPhoneNumber) {
            step = 2
        }
    } else {
        const userFetchedFromDatabase = await findUserWithAllItsRolesByPhoneNumber(user!!.phoneNumber)
        if (userFetchedFromDatabase?.id) {
            user = userFetchedFromDatabase
        }
        step = 2
    }

}

const sendRegistrationRequest = async () => {
    if (props.inDialog) {
        await createUser(user, getCurrentUser().id).then(async response => {
            await confirmActionPromiseDialogWithCancelButton("Заявките за регистрация/нова роля са изпратени",
                    "Друг админ трябва да одобри създадените от вас заявки")
            emits("update:submitted", true)
            emits("update:newUser", <User>response)
        })
    } else {
    await createRequestFromUser(user).then(
            async () => {
              await confirmActionPromiseDialogWithCancelButton("Заявките за регистрация/нова роля са изпратени",
                      "Можете да влезнете в Е-училище, когато имате одобрени заявки за роля и регистрация")
              await router.push({path: "/login"})
            }
    )
  }
}
const goBackToPhoneNumberInputStep = () => {
  step = 1
}
</script>

<style scoped>

</style>