<template>
  <div class="row q-col-gutter-lg bg-sms">
    <div class="col-1"></div>
    <div class="col-10">
      <q-page class="page-content" padding>
        <div class="row">
          <div class="text-h4 q-mb-md">Информация за потребителя</div>
          <q-space/>
          <div v-if="!isNotCurrentUserAndNotAdmin">
            <q-btn class="q-mr-sm" color="negative" label="Върни промените" outline
                   rounded @click="reset()"/>
            <q-btn color="primary" label="Запази промените" outline rounded
                   @click="update()"/>
          </div>
        </div>
        <q-separator/>
        <div class="row q-col-gutter-sm q-ma-xs" style="margin-top: 30px;">
          <div class="col-lg-6 col-md-6 col-sm-12 col-xs-12">
            <q-card class="col-6" style="height: 78vh">
              <q-card-section horizontal>
                <q-card-section class="col-8">
                  <span class="text-h4">Лична информация</span>
                  <q-input v-model="user.firstName" :readonly="isNotCurrentUserAndNotAdmin" class="q-pa-sm"
                           label="Име"/>
                  <q-input v-model="user.middleName" :readonly="isNotCurrentUserAndNotAdmin" class="q-pa-sm"
                           label="Презиме"/>
                  <q-input v-model="user.lastName" :readonly="isNotCurrentUserAndNotAdmin" class="q-pa-sm"
                           label="Фамилия"/>
                  <q-input v-model="user.personalNumber" :readonly="isNotCurrentUserAndNotAdmin" class="q-pa-sm"
                           label="ЕГН"/>
                  <q-input v-model="user.email" :readonly="isNotCurrentUserAndNotAdmin" class="q-pa-sm"
                           label="Имейл"/>
                  <q-input v-model="user.address" :readonly="isNotCurrentUserAndNotAdmin" class="q-pa-sm"
                           label="Адрес"/>
                  <q-input v-model="user.username" :readonly="isNotCurrentUserAndNotAdmin" class="q-pa-sm"
                           label="Потребителско име"/>
                  <q-select v-model="user.gender" :option-label="option=>translationOfGender[option]"
                            :options="Object.keys(Gender)"
                            :readonly="isNotCurrentUserAndNotAdmin" class="q-pa-sm"
                            label="Пол"/>
                </q-card-section>
                <q-card-section class="col">
                  <div>
                    <q-avatar v-if="imageUrl!==''" font-size="155x" size="180px" square
                              text-color="white">
                      <q-img
                              :src="imageUrl"
                              fit="contain"
                              ratio="1"
                              spinner-color="white"
                      ></q-img>
                    </q-avatar>
                    <q-avatar v-else color="cyan-2" font-size="155x" size="180px" square
                              text-color="white">
                      {{ user.firstName[0] }}{{ user.lastName[0] }}
                    </q-avatar>
                  </div>
                  <div>
                    <q-file v-model="profilePictureFile"
                            accept="image/*"
                            dense
                            display-value="Смени снимка"
                            outlined
                            @update:model-value="handleUpload()"
                    >
                      <template v-slot:prepend>
                        <q-icon name="attach_file"/>
                      </template>
                    </q-file>
                  </div>
                </q-card-section>
              </q-card-section>
              <q-separator/>
              <div class="q-pa-sm q-pl-lg text-h6 row">
                Статус:<span :class="getRequestStatusColorClass(user.status)">{{
                  translationOfRequestStatusForUser[user.status]
                }}</span><br/>

                <q-space/>
                <div v-if="isAdminAndNotCurrentUser">
                  <q-btn v-if="user.status===RequestStatus.REJECTED" class="float-right"
                         color="secondary" icon="done"
                         label="Активирай потребител"
                         outline rounded
                         @click="createRequestForStatusChange(RequestStatus.APPROVED)"/>
                  <q-btn v-if="user.status===RequestStatus.APPROVED" class="float-right"
                         color="negative" icon="cancel"
                         label="Деактивирай потребител"
                         outline rounded
                         @click="createRequestForStatusChange(RequestStatus.REJECTED)"/>
                </div>
              </div>

            </q-card>
          </div>
          <div v-if="isAdminCurrentUserOrCurrentUserParent" class="col-lg-6 col-md-6 col-sm-12 col-xs-12">
            <q-card style="height: 78vh">
              <q-card-section>
                <div class="row">
                  <div class="col-4">
                    <span class="text-h4">Роли</span>
                  </div>
                  <div class="col-3">
                    <q-select v-if="isCurrentUser"
                              v-model="selectedPeriod"
                              :option-label="(option:SchoolPeriod) => `${option.startYear}/${option.endYear}`"
                              :options="allSchoolPeriods"
                              dense
                              label="Учебна година"/>
                  </div>

                  <div class="col-5">
                    <q-btn v-if="!isNotCurrentUserAndNotAdmin" class="float-right" color="primary"
                           icon="add"
                           label="Заяви нова роля"
                           outline rounded
                           @click="addNewRole()"/>
                  </div>
                </div>
              </q-card-section>
              <q-scroll-area style="height: 62vh" visible>
                <div v-for="role in userRolesFilteredBySelectedPeriod"
                     v-if="userRolesFilteredBySelectedPeriod !=null && userRolesFilteredBySelectedPeriod.length>0">
                  <q-separator/>
                  <q-card-section class="row">
                    <q-expansion-item class="full-width">
                      <template v-slot:header>
                        <div class="q-pt-xs q-pl-sm full-width">
                          <div :class="`${role.id==null ? 'text-primary' : ''} row`">
                            <q-btn :disable="role.id != null" flat icon="delete" round size="sm"
                                   text-color="negative"
                                   @click="deleteRole(role)">
                              <q-tooltip v-if="role.id != null" type="a">Не е
                                възможно да
                                редактирате данни, които са въведени в базата данни
                              </q-tooltip>
                            </q-btn>
                            <q-btn :disable="role.id != null" flat icon="edit" round size="sm"
                                   text-color="secondary"
                                   @click="updateRole(role)">
                              <q-tooltip v-if="role.id != null" type="a">Не е
                                възможно да
                                редактирате данни, които са въведени в базата данни
                              </q-tooltip>
                            </q-btn>
                            <span class="q-pt-xs q-pl-lg col-7">
                            {{
                                constructSchoolUserRoleMessage(role)
                              }}
                               <q-tooltip v-if="role.role === SchoolRole.PARENT || role.role === SchoolRole.STUDENT">
                            Кликни за повече информация
                          </q-tooltip></span>
                            <q-space/>
                            <div style="vertical-align: middle">
                            <span :class="`${getRequestStatusColorClass(role.status)} q-pt-xs q-pr-sm`"
                                  style="vertical-align: middle">
                                  {{ translationOfRequestStatusForRole[role.status] }}
                              </span>
                            </div>
                            <div v-if="isAdminAndNotCurrentUser">
                              <q-btn v-if="role.status===RequestStatus.REJECTED" class="float-right"
                                     color="secondary" icon="done"
                                     outline rounded
                                     size="sm"
                                     @click="createRequestForRoleStatusChange(role,RequestStatus.APPROVED)">
                                <q-tooltip>
                                  Активирай роля
                                </q-tooltip>
                              </q-btn>
                              <q-btn v-if="role.status===RequestStatus.APPROVED" class="float-right"
                                     color="negative" icon="cancel"
                                     outline
                                     rounded size="sm"
                                     @click="createRequestForRoleStatusChange(role,RequestStatus.REJECTED)">
                                <q-tooltip>
                                  Деактивирай роля
                                </q-tooltip>
                              </q-btn>
                            </div>
                          </div>
                        </div>
                      </template>
                      <q-card-section v-if="role.role === SchoolRole.STUDENT">
                        <q-btn :to="`/student-diary/${role.detailsForUser.schoolClass.id}/${user.id}/${props.periodId}/${props.schoolId}/grades`"
                               :disable="role.status!==RequestStatus.APPROVED" label="Дневник"/>
                        <q-input v-model="role.detailsForUser.numberInClass" label="Номер в клас" readonly stack-label/>
                        <q-input v-model="role.detailsForUser.schoolClass.name" label="Клас" readonly stack-label/>
                        <q-field label="Класен ръководител" readonly stack-label>
                          <template v-slot:default>
                            <router-link
                                    :to="`/user/${role.detailsForUser.schoolClass.mainTeacher.id}/${props.periodId}/${props.schoolId}`"
                                    active-class="text-negative" class="text-primary"
                                    exact-active-class="text-negative">
                              {{ role.detailsForUser.schoolClass.mainTeacher.firstName }}
                              {{ role.detailsForUser.schoolClass.mainTeacher.lastName }}
                            </router-link>
                          </template>
                        </q-field>
                        <div class="text-h6 q-pt-sm q-pb-none">Родители</div>
                        <div v-for="parent in role.detailsForUser.parents">
                          <q-field label="Родител" readonly stack-label>
                            <template v-slot:default>
                              <router-link
                                      :to="`/user/${parent.id}/${props.periodId}/${props.schoolId}`"
                                      active-class="text-negative" class="text-primary"
                                      exact-active-class="text-negative">
                                {{ parent.firstName }}
                                {{ parent.middleName }}
                                {{ parent.lastName }}
                              </router-link>
                            </template>
                          </q-field>
                        </div>
                      </q-card-section>
                      <q-card-section v-if="role.role === SchoolRole.TEACHER">
                        <q-btn :disable="role.status !== RequestStatus.APPROVED"
                               :to="`/teacher-lessons/${periodId}/${schoolId}/${user.id}`" label="Програма"/>
                        <div class="text-h6 q-pt-sm q-pb-none">Предмети</div>
                        <div v-for="subject in role.detailsForUser.teachingSubjects">
                          <q-field label="Предмет" readonly stack-label>
                            <template v-slot:default>
                              <router-link
                                      :to="`/subject-diary/${subject.schoolClass?.id}/${subject.id}/${props.periodId}/${props.schoolId}/grades`"
                                      active-class="text-negative" class="text-primary"
                                      exact-active-class="text-negative">
                                {{ subject.name }} - {{ subject.schoolClass.name }} - {{
                                  translationOfSemester[subject.semester]
                                }}
                              </router-link>
                            </template>
                          </q-field>
                        </div>
                      </q-card-section>
                      <q-card-section v-if="role.role===SchoolRole.PARENT">
                        <div class="text-h4">Ученик</div>
                        <q-btn :disable="role.status!==RequestStatus.APPROVED"
                               :to="`/student-diary/${role.detailsForUser.child.role.detailsForUser.schoolClass.id}/${role.detailsForUser.child.id}/${props.periodId}/${props.schoolId}/grades`"
                               label="Дневник"/>
                        <q-field label="Име" readonly stack-label>
                          <template v-slot:default>
                            <router-link
                                    :to="`/user/${role.detailsForUser.child.id}/${props.periodId}/${props.schoolId}`"
                                    active-class="text-negative" class="text-primary"
                                    exact-active-class="text-negative">
                              {{ role.detailsForUser.child.firstName }}
                              {{ role.detailsForUser.child.middleName }}
                              {{ role.detailsForUser.child.lastName }}
                            </router-link>
                          </template>
                        </q-field>
                        <q-input v-model="role.detailsForUser.child.username" label="Потребителско име" readonly
                                 stack-label/>
                        <q-input v-model="role.detailsForUser.child.email" label="Имейл" readonly stack-label/>
                      </q-card-section>
                    </q-expansion-item>
                  </q-card-section>
                </div>
                <div v-else>
                  <q-separator/>
                  <span class="text-h6 text-grey q-pl-md">Потребителят все още няма роли за избрания период</span>
                </div>
              </q-scroll-area>
            </q-card>
          </div>
        </div>
      </q-page>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {
    createRoleChangeStatusRequest,
    createUserChangeStatusRequest,
    fetchUserWithAllItsRolesById,
    findStudentByPhoneNumberPeriodAndSchoolClass,
    getAllSchoolClasses,
    getAllSchoolPeriodsWithTheSchoolsTheyAreStarted,
    getAllSchools,
    getAllSubjects,
    getUserProfilePicture,
    updateUser,
    updateUserProfilePicture
} from "../../services/RequestService";
import {$computed, $ref} from "vue/macros";
import {DetailsForParent, DetailsForStudent, Gender, SchoolRole} from "../../model/User";
import {onUnmounted, watch} from "vue";
import {useRouter} from "vue-router";
import {
    confirmActionPromiseDialog,
    getRequestStatusColorClass,
    translationOfGender,
    translationOfRequestStatusForRole,
    translationOfRequestStatusForUser,
    translationOfSemester
} from "../../utils";
import AddRoleDialog from "../add-role-dialog.vue";
import {constructSchoolUserRoleMessage, SchoolUserRole} from "../../model/SchoolUserRole";
import {QFile, useQuasar} from "quasar";
import {getCurrentUser, updateOneRoleUserInLocalStorage} from "../../services/LocalStorageService";
import {SchoolPeriod} from "../../model/SchoolPeriod";
import {cloneDeep} from "lodash-es";
import {RequestStatus} from "../../model/RequestStatus";

const props = defineProps<{
  id: number,
  periodId: number,
  schoolId: number
}>()
const router = useRouter()
const quasar = useQuasar()
let databaseUser = $ref(await fetchUserWithAllItsRolesById(props.id, props.periodId, props.schoolId))
let user = $ref(cloneDeep(databaseUser))
const allSchools = await getAllSchools()
const allSchoolClasses = await getAllSchoolClasses()
const allSchoolPeriods = await getAllSchoolPeriodsWithTheSchoolsTheyAreStarted()
const allSubjects = await getAllSubjects()
const currentUser = getCurrentUser()
const isCurrentUser = $computed(() => currentUser.id == user.id)
const isCurrentUsersParent = $computed(() => currentUser.role.role == SchoolRole.PARENT && (currentUser.role.detailsForUser as DetailsForParent).child?.id == currentUser.id)
const isAdminAndNotCurrentUser = $computed(() => !isCurrentUser && currentUser.role.role == SchoolRole.ADMIN)
const isNotCurrentUserAndNotAdmin = $computed(() => !isCurrentUser && !isAdminAndNotCurrentUser)
const isAdminCurrentUserOrCurrentUserParent = $computed(() => isCurrentUsersParent || isCurrentUser || currentUser.role.role == SchoolRole.ADMIN)
const selectedPeriod = $ref(currentUser.role.period)
let userRolesFilteredBySelectedPeriod = $ref<SchoolUserRole[]>(user?.roles?.filter(role => role.period.id == selectedPeriod?.id))

watch(props, async () => {
  databaseUser = await fetchUserWithAllItsRolesById(props.id, props.periodId, props.schoolId)
  user = cloneDeep(databaseUser)
  userRolesFilteredBySelectedPeriod = user?.roles?.filter(role => role.period.id == selectedPeriod?.id)
})

watch(() => selectedPeriod, () => {
  userRolesFilteredBySelectedPeriod = user?.roles?.filter(role => role.period.id == selectedPeriod?.id)
},)

const profilePictureBase64 = $ref(await getUserProfilePicture(props.id).then(e => e.data))
const profilePictureFile = $ref(profilePictureBase64 ? base64ToImageFile(profilePictureBase64, "profile") : null)

let imageUrl = $ref(profilePictureFile ? window.URL.createObjectURL(profilePictureFile) : '');

function base64ToImageFile(base64String: string, fileName: string): File {
  const arr = base64String.split(",");
  const mimeType = arr[0].match(/:(.*?);/)?.[1] || "image/png";
  const byteCharacters = atob(arr[1]); // Decode Base64
  const byteNumbers = new Uint8Array(byteCharacters.length);

  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }

  const blob = new Blob([byteNumbers], {type: mimeType});
  return new File([blob], fileName, {type: mimeType});
}

function imageFileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string); // Base64 Data URL
    reader.onerror = reject;
    reader.readAsDataURL(file); // Read file as Base64
  });
}


const handleUpload = async () => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  if (profilePictureFile) {
    imageUrl = window.URL.createObjectURL(profilePictureFile)
    await updateUserProfilePicture(await imageFileToBase64(profilePictureFile), user.id)

  }
};

onUnmounted(() => {
  if (imageUrl) {
    URL.revokeObjectURL(imageUrl);
  }
});

const update = async () => {
  const currentUserId = currentUser.id
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  await updateUser(user, currentUserId)
  databaseUser = cloneDeep(user)
  if (currentUserId == user.id) {
    updateOneRoleUserInLocalStorage(user)
  }
}

const addNewRole = async () => quasar.dialog({
  component: AddRoleDialog,
  componentProps: {
    role: <SchoolUserRole>{period: currentUser.role.period, school: currentUser.role.school},
    schoolOptions: allSchools,
    allSchoolClassesOptions: allSchoolClasses,
    schoolPeriodsWithSchoolIds: allSchoolPeriods,
    disablePeriodAndSchoolSelections: isAdminAndNotCurrentUser,
    subjects: allSubjects,
    userId: user.id
  },
}).onOk(async (payload) => {
  const schoolUserRole = payload.item as SchoolUserRole
  if (schoolUserRole.role == SchoolRole.PARENT) {
    const detailsForParent = schoolUserRole.detailsForUser as DetailsForParent
    schoolUserRole.detailsForUser = new DetailsForParent(await findStudentByPhoneNumberPeriodAndSchoolClass(detailsForParent.child?.phoneNumber, schoolUserRole.period.id, (detailsForParent.child?.role.detailsForUser as DetailsForStudent).schoolClass?.id)
    )
    user?.roles!!.push(schoolUserRole)
  } else {
    user?.roles!!.push(payload.item)
  }
  userRolesFilteredBySelectedPeriod = user?.roles?.filter(role => role.period.id == selectedPeriod?.id)

})

const deleteRole = (role) => {
  user.roles?.splice(user.roles.indexOf(role), 1);
  userRolesFilteredBySelectedPeriod = user?.roles?.filter(role => role.period.id == selectedPeriod?.id)
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
      userId: user.id
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
    userRolesFilteredBySelectedPeriod = user?.roles?.filter(role => role.period.id == selectedPeriod?.id)
  })
}
const reset = () => {
  user = cloneDeep(databaseUser)
  userRolesFilteredBySelectedPeriod = user?.roles?.filter(role => role.period.id == selectedPeriod?.id)
}

const createRequestForStatusChange = async (status: RequestStatus) => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  await createUserChangeStatusRequest(user.id, status, props.periodId, props.schoolId, currentUser.id)
}
const createRequestForRoleStatusChange = async (role: SchoolUserRole, status: RequestStatus) => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  await createRoleChangeStatusRequest(role.id, status, props.periodId, props.schoolId, currentUser.id)
}
const columns = [
  {name: 'edit'},
  {
    name: "firstName",
    label: "Име",
    align: "left",
    field: 'firstName',
    sortable: true
  },
  {
    name: "middleName",
    align: "left",
    label: "Презиме",
    field: 'middleName',
    sortable: true
  },
  {
    name: "lastName",
    align: "left",
    label: "Фамилия",
    field: 'lastName',
    sortable: true
  },
  {
    name: "username",
    align: "left",
    label: "Потребителско име",
    field: 'username',
    sortable: true
  },
  {
    name: "email",
    align: "left",
    label: "Е-майл",
    field: 'email',
    sortable: true
  },
  {
    name: "role",
    align: "left",
    label: "Роля",
    field: 'role',
    sortable: true
  }
]
</script>

<style scoped>
.page-content {
  box-shadow: 0 30px 25px rgba(0, 0, 0, 0.4);
}
</style>