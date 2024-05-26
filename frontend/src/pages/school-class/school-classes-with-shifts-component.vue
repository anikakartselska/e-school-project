<template>
  <q-card>
    <div class="text-primary text-h6 q-pl-sm">
      <div class="row">
        График за смените на класовете
        <q-space/>
        <q-btn color="primary" dense icon="open_in_new" label="Към страницата с класовете" outline
               @click="openSchoolClasses()"></q-btn>
      </div>
    </div>
    <q-separator/>
    <div class="text-primary text-h6 q-pl-sm">
      Първи срок
    </div>
    <div class="row">
      <div class="col-6 q-mr-lg">
        <q-toolbar class="bg-primary text-white shadow-3">
          <q-toolbar-title style="font-size: 2vh">Първа смяна</q-toolbar-title>
        </q-toolbar>
        <q-scroll-area class="scroll-area-for-find-by">
          <q-list bordered separator>
            <q-item v-for="schoolClass in schoolClasses.filter(it=> it.shifts.firstSemester === Shift.FIRST)"
                    active-class="menu"
                    clickable
                    @click="openSchoolClass(schoolClass)"
            >
              <q-item-section>
                <router-link :to="`/school-class/${props.periodId}/${props.schoolId}/${schoolClass.id}/students`"
                             active-class="text-negative" class="text-primary"
                             exact-active-class="text-negative">
                  {{ schoolClass.name }}
                </router-link>
              </q-item-section>
              <q-item-section avatar>
                <q-btn color="secondary" label="Промени смяна" size="sm"
                       @click="changeShift(schoolClass,Shift.SECOND,Semester.FIRST)"></q-btn>
              </q-item-section>
            </q-item>
          </q-list>
        </q-scroll-area>
      </div>
      <div class="col">
        <q-toolbar class="bg-primary text-white shadow-3">
          <q-toolbar-title style="font-size: 2vh">Втора смяна</q-toolbar-title>
        </q-toolbar>
        <q-scroll-area class="scroll-area-for-find-by">
          <q-list bordered separator>
            <q-item v-for="schoolClass in schoolClasses.filter(it=> it.shifts.firstSemester === Shift.SECOND)"
                    active-class="menu"
                    clickable>
              <q-item-section>
                <router-link :to="`/school-class/${props.periodId}/${props.schoolId}/${schoolClass.id}/students`"
                             active-class="text-negative" class="text-primary"
                             exact-active-class="text-negative">
                  {{ schoolClass.name }}
                </router-link>
              </q-item-section>
              <q-item-section avatar>
                <q-btn color="secondary" label="Промени смяна" size="sm"
                       @click="changeShift(schoolClass,Shift.FIRST,Semester.FIRST)"></q-btn>
              </q-item-section>
            </q-item>
          </q-list>
        </q-scroll-area>
      </div>
    </div>
    <q-separator/>
    <div class="text-primary text-h6 q-pl-sm">
      Втори срок
    </div>
    <div class="row">
      <div class="col-6 q-mr-lg">
        <q-toolbar class="bg-primary text-white shadow-3">
          <q-toolbar-title style="font-size: 2vh">Първа смяна</q-toolbar-title>
        </q-toolbar>
        <q-scroll-area class="scroll-area-for-find-by">
          <q-list bordered separator>
            <q-item v-for="schoolClass in schoolClasses.filter(it=> it.shifts.secondSemester === Shift.FIRST)"
                    active-class="menu"
                    clickable
            >
              <q-item-section>
                <router-link :to="`/school-class/${props.periodId}/${props.schoolId}/${schoolClass.id}/students`"
                             active-class="text-negative" class="text-primary"
                             exact-active-class="text-negative">
                  {{ schoolClass.name }}
                </router-link>
              </q-item-section>
              <q-item-section avatar>
                <q-btn color="secondary" label="Промени смяна" size="sm"
                       @click="changeShift(schoolClass,Shift.SECOND,Semester.SECOND)"></q-btn>
              </q-item-section>
            </q-item>
          </q-list>
        </q-scroll-area>
      </div>
      <div class="col">
        <q-toolbar class="bg-primary text-white shadow-3">
          <q-toolbar-title style="font-size: 2vh">Втора смяна</q-toolbar-title>
        </q-toolbar>
        <q-scroll-area class="scroll-area-for-find-by">
          <q-list bordered separator>
            <q-item v-for="schoolClass in schoolClasses.filter(it=> it.shifts.secondSemester === Shift.SECOND)"
                    active-class="menu"
                    clickable>
              <q-item-section>
                <router-link :to="`/school-class/${props.periodId}/${props.schoolId}/${schoolClass.id}/students`"
                             active-class="text-negative" class="text-primary"
                             exact-active-class="text-negative">
                  {{ schoolClass.name }}
                </router-link>
              </q-item-section>
              <q-item-section avatar>
                <q-btn color="secondary" label="Промени смяна" size="sm"
                       @click="changeShift(schoolClass,Shift.FIRST,Semester.SECOND)"></q-btn>
              </q-item-section>
            </q-item>
          </q-list>
        </q-scroll-area>
      </div>
    </div>
  </q-card>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {useRouter} from "vue-router";
import {getSchoolClassesFromSchool, saveSchoolClass} from "../../services/RequestService";
import {SchoolClass, Shift} from "../../model/SchoolClass";
import {useQuasar} from "quasar";
import {Semester} from "../../model/SchoolPeriod";
import {confirmActionPromiseDialog} from "../../utils";

const props = defineProps<{
  periodId: number,
  schoolId: number
}>()
const quasar = useQuasar()
const router = useRouter()
const schoolClasses = $ref(await getSchoolClassesFromSchool(props.schoolId, props.periodId))

const openSchoolClasses = () => {
  router.push({
    path: `/school-classes/${props.periodId}/${props.schoolId}`,
  });
}

const changeShift = async (selectedSchoolClass: SchoolClass, selectedShift: Shift, semester: Semester) => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  if (semester == Semester.FIRST) {
    selectedSchoolClass.shifts.firstSemester = selectedShift
  } else {
    selectedSchoolClass.shifts.secondSemester = selectedShift
  }
  await saveSchoolClass(selectedSchoolClass)
}
</script>

<style scoped>
.page-content {
  box-shadow: 0 30px 25px rgba(0, 0, 0, 0.4);
}

.menu {
  background-color: rgba(46, 185, 246, 0.27);
  color: black;
}

.scroll-area-for-find-by {
  height: 25vh;
}

.find-by-section {
  max-width: 36vh;
  margin-left: 7vh
}

q-toolbar {
  width: 36vh;
}

</style>