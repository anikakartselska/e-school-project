<template>
  <q-page>
    <div class="row q-col-gutter-sm q-ma-xs">
      <div class="col-lg-6 col-md-6 col-sm-12 col-xs-12">
        <q-card class="col-6" flat bordered>
          <q-card-section horizontal>
            <q-card-section class="col-8">
              <q-input v-model="user.firstName" label="Име" readonly stack-label/>
              <q-input v-model="user.middleName" label="Презиме" readonly stack-label/>
              <q-input v-model="user.lastName" label="Фамилия" readonly stack-label/>
              <q-input v-model="user.username" label="Потребителско име" readonly stack-label/>
              <q-input v-model="user.personalNumber" label="ЕГН" readonly stack-label/>
              <q-input v-model="user.email" label="Имейл" readonly stack-label/>
              <q-input v-model="user.phoneNumber" label="Телефонен номер" readonly stack-label/>
              <q-input v-model="user.address" label="Адрес" readonly stack-label/>
              <q-input v-model="user.role" label="Роля" readonly stack-label/>
            </q-card-section>
            <div class="col-1"/>
            <q-card-section class="col-3">
              <q-img
                      class="rounded-borders"
                      src="https://cdn.quasar.dev/img/boy-avatar.png"
              />
            </q-card-section>
          </q-card-section>

          <q-separator/>
        </q-card>
      </div>
      <div class="col-lg-6 col-md-6 col-sm-12 col-xs-12" v-if="user.details">
        <q-card>
          <div v-for="details in user.details">
            <q-card-section v-if="isDetailsForStudent(details)">
              <q-btn label="Дневник"
                     :to="`/diary/${details.schoolClass.id}/${user.id}/${props.periodId}/${props.schoolId}/absences`"/>
              <q-input v-model="details.numberInClass" label="Номер в клас" readonly stack-label/>
              <q-input v-model="details.schoolClass.name" label="Клас" readonly stack-label/>
              <q-field label="Класен ръководител" readonly stack-label>
                <template v-slot:default>
                  <router-link
                          :to="`/user/${details.schoolClass.mainTeacher.id}/${props.periodId}/${props.schoolId}`"
                          class="text-primary" active-class="text-negative" exact-active-class="text-negative">
                    {{ details.schoolClass.mainTeacher.firstName }}
                    {{ details.schoolClass.mainTeacher.lastName }}
                  </router-link>
                </template>
              </q-field>
            </q-card-section>
            <q-card-section v-if="isDetailsForParent(details)">
                <div class="text-h4">Ученик</div>
              <q-input v-model="details.children.firstName" label="Име" readonly stack-label/>
              <q-input v-model="details.children.middleName" label="Презиме" readonly stack-label/>
              <q-input v-model="details.children.lastName" label="Фамилия" readonly stack-label/>
              <q-input v-model="details.children.username" label="Потребителско име" readonly stack-label/>
              <q-input v-model="details.children.email" label="Имейл" readonly stack-label/>
            </q-card-section>
          </div>
        </q-card>
      </div>
    </div>
  </q-page>
</template>

<script lang="ts" setup>
import {getUserWithDetailsByUserId} from "../services/RequestService";
import {$ref} from "vue/macros";
import {isDetailsForParent, isDetailsForStudent, UserView} from "../model/User";
import {watch} from "vue";
import {useRouter} from "vue-router";

const props = defineProps<{
  id: number,
  periodId: number,
  schoolId: number
}>()
let user = $ref(await getUserWithDetailsByUserId(props.id, props.periodId, props.schoolId))
const router = useRouter()
watch(props, async () => {
  user = await getUserWithDetailsByUserId(props.id, props.periodId, props.schoolId)
})
const openUser = async (row: UserView) => {
    await router.push({
        name: "user",
        params: {
            id: row.id,
            periodId: props.periodId,
            schoolId: props.schoolId,
        }
    })
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

</style>