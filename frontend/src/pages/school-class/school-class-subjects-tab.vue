<template>
  <q-page class="q-pa-sm bg-sms">
    <q-card>
      <div class="text-h5 q-pa-lg text-primary">
        Предмети
      </div>
      <q-list bordered separator>
        <q-item v-for="subject in subjectsTaughtInSchoolClassForYear" clickable @click="openSubjectDiary(subject.id)">
            <q-item-section avatar>
                <q-avatar class="bg-primary text-white" icon="import_contacts" size="md"/>
            </q-item-section>
            <q-item-section>
                <q-item-label>{{ subject.name }}</q-item-label>
                <q-item-label caption>Преподавател:
                    <router-link :to="`/user/${subject.teacher.id}/${periodId}/${schoolId}`"
                                 active-class="text-negative" class="text-primary" exact-active-class="text-negative">
                        {{ subject.teacher.firstName }} {{ subject.teacher.lastName }}
                        <q-tooltip>
                  Кликни за повече детайли
                </q-tooltip>
              </router-link>
            </q-item-label>
          </q-item-section>
        </q-item>
      </q-list>
    </q-card>
  </q-page>
</template>

<script lang="ts" setup>
import {useRouter} from "vue-router";
import {useQuasar} from "quasar";
import {SchoolClass} from "../../model/SchoolClass";
import {Subject} from "../../model/Subject";
import {StudentView} from "../../model/User";

const props = defineProps<{
    periodId: number,
    schoolId: number,
    schoolClass: SchoolClass
    subjectsTaughtInSchoolClassForYear: Subject[]
    studentsFromSchoolClass: StudentView[]
}>()

const router = useRouter()
const quasar = useQuasar()

const openSubjectDiary = (subjectId: number) => {
    router.push(`/subject-diary/${props.schoolClass.id}/${subjectId}/${props.periodId}/${props.schoolId}/grades`)
}


</script>

<style scoped>

</style>