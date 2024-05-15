<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <div class="row">
        <div class="col-3"></div>
        <q-separator class="q-ma-sm" vertical/>
        <div class="col q-pb-sm">
          <q-btn class="float-right q-ma-sm" color="primary" icon="edit" round @click="saveSchoolChanges"/>
          <div class="text-h6 q-pb-lg q-pt-sm">
            Информация за училището
          </div>
          Име: <span class="text-primary">{{
            currentSchool.schoolName
            }}</span><br>
            Град:
            <span class="text-primary">{{
                currentSchool.city
                }}</span>
            <br>
            Адрес: <span class="text-primary">{{
            currentSchool.address
            }}</span><br>
            <q-separator/>
            <span class="text-bold">
                Стаи/зали в училището:</span>
            <q-scroll-area style="height: 40vh">
                <div v-for="room in currentSchool.rooms">
                    Стая:
                    <span class="text-primary">
                {{ room.room }}
            </span><br>
                    Предмети, които могат да се провеждат в нея:
                    <span class="text-primary">
                {{ room.subjects.length > 0 ? room.subjects.join(',') : 'Всички' }}
            </span>
                    <q-separator/>
                </div>
            </q-scroll-area>
        </div>
        <q-separator class="q-ma-sm" vertical/>
        <div class="col-3"></div>
      </div>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {useQuasar} from "quasar";
import {$ref} from "vue/macros";
import {fetchSchoolById, getAllSubjects, updateSchool} from "../../services/RequestService";
import {School} from "../../model/School";
import SchoolEditDialog from "./dialogs/school-edit-dialog.vue";

const props = defineProps<{
  schoolId: number
}>()
const quasar = useQuasar()
let currentSchool = $ref(await fetchSchoolById(props.schoolId))
const subjects = $ref(await getAllSubjects())

const saveSchoolChanges = async () => quasar.dialog(
        {
          component: SchoolEditDialog,
          componentProps: {
            school: currentSchool,
              subjects: subjects
          },
        }).onOk(async (payload) => {
          const school = payload.item as School
          await updateSchool(school).then(r =>
                  currentSchool = school
          )
        }
)
</script>

<style scoped>

</style>