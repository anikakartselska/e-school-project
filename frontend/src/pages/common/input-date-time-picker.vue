<template>
  <div class="q-pa-md" style="max-width: 300px">
    <q-input :model-value="formatDate(modelValue)" filled mask="##.##.#### ##:##:##"
             @update:model-value="updateModelValue($event)">
      <template v-slot:prepend>
        <q-icon class="cursor-pointer" name="event">
          <q-popup-proxy cover transition-hide="scale" transition-show="scale">
            <q-date :locale="{days: daysAndMonthsInBulgarian.days,
                            daysShort: daysAndMonthsInBulgarian.daysShort,
                            months: daysAndMonthsInBulgarian.months,
                            monthsShort: daysAndMonthsInBulgarian.monthsShort}"
                    :model-value="formatDate(modelValue)"
                    mask="DD.MM.YYYY HH:mm:ss"
                    @update:model-value="updateModelValue($event)">
              <div class="row items-center justify-end">
                <q-btn v-close-popup color="primary" flat label="Затвори"/>
              </div>
            </q-date>
          </q-popup-proxy>
        </q-icon>
      </template>

      <template v-slot:append>
        <q-icon class="cursor-pointer" name="access_time">
          <q-popup-proxy cover transition-hide="scale" transition-show="scale">
            <q-time :model-value="formatDate(modelValue)" mask="DD.MM.YYYY HH:mm:ss"
                    @update:model-value="updateModelValue($event)">
              <div class="row items-center justify-end">
                <q-btn v-close-popup color="primary" flat label="Затвори"/>
              </div>
            </q-time>
          </q-popup-proxy>
        </q-icon>
      </template>
    </q-input>
  </div>
</template>

<script lang="ts" setup>
import {formatToBulgarian} from "../../utils";
import {date} from "quasar";

const props = defineProps<{
  modelValue: string | null
  label: string
  readonly?: boolean
}>();

const emits = defineEmits<{
  (e: 'update:modelValue', value: string | null): void
}>();


const updateModelValue = (ev) => {
  console.log(ev)
  if (ev != null) {
    emits("update:modelValue", formatToDate(ev))
  }
}

const formatDate = (dateToFormat: string | null) => {
  console.log(dateToFormat)
  return formatToBulgarian(dateToFormat)
};

const formatToDate = (ev) => {
  console.log(ev)
  if (ev == null || ev == '') {
    return null
  }
  return date.extractDate(ev, 'DD.MM.YYYY HH:mm:ss').toISOString();
}

const daysAndMonthsInBulgarian = {
  days: ['неделя', 'понеделник', 'вторник', 'сряда', 'четвъртък', 'петък', 'събота'],
  daysShort: ['нед', 'пон', 'вт', 'ср', 'чет', 'пет', 'съб'],
  months: ['януари', 'февруари', 'март', 'април', 'май', 'юни', 'юли', 'август', 'септември', 'октомври', 'ноември', 'декември'],
  monthsShort: ['ян', 'фев', 'мар', 'апр', 'май', 'юни', 'юли', 'авг', 'сеп', 'окт', 'ное', 'дек']
}
</script>

<style scoped>

</style>