<template>
    <div>
        <q-input :label="label"
                 :model-value="formatDate(modelValue)"
                 :readonly="props.readonly"
                 dense
                 fill-mask
                 hide-bottom-space
                 mask="##.##.####"
                 @update:model-value="updateModelValue($event)">
            <template v-slot:append>
                <slot name="input-append"></slot>
                <q-icon class="cursor-pointer" name="event">
                    <q-popup-proxy ref="qDateProxy"
                                   transition-hide="scale"
                                   transition-show="scale">
                        <q-date :locale="{days: daysAndMonthsInBulgarian.days,
                            daysShort: daysAndMonthsInBulgarian.daysShort,
                            months: daysAndMonthsInBulgarian.months,
                            monthsShort: daysAndMonthsInBulgarian.monthsShort}"
                                :model-value="formatDate(modelValue)"
                                :readonly="readonly"
                                mask="DD.MM.YYYY"
                                @update:model-value="updateModelValue($event)">
                            <div class="row items-center justify-end">
                                <q-btn v-close-popup color="primary" flat label="Затвори"/>
                            </div>
                        </q-date>
                    </q-popup-proxy>
                </q-icon>
            </template>
        </q-input>
    </div>
</template>
<script lang="ts" setup>
import {date} from "quasar";

import {formatToBulgarian} from "../../utils";

const props = defineProps<{
    modelValue: string | null
    label: string
    readonly?: boolean
}>();

const emits = defineEmits<{
    (e: 'update:modelValue', value: string | null): void
}>();

const validDateInputCheck = new RegExp(/^\d{2}\.\d{2}\.\d{4}$/)
const updateModelValue = (ev) => {
    if (validDateInputCheck.test(ev) || ev == null) {
        emits("update:modelValue", formatToDate(ev))
    }
}

const formatDate = (dateToFormat: string | null) => {
    return formatToBulgarian(dateToFormat)
};

const formatToDate = (ev) => {
    if (ev == null || ev == '') {
        return null
    }
    return date.formatDate(date.extractDate(ev, 'DD.MM.YYYY'), "YYYY-MM-DD");
}

const daysAndMonthsInBulgarian = {
    days: ['неделя', 'понеделник', 'вторник', 'сряда', 'четвъртък', 'петък', 'събота'],
    daysShort: ['нед', 'пон', 'вт', 'ср', 'чет', 'пет', 'съб'],
    months: ['януари', 'февруари', 'март', 'април', 'май', 'юни', 'юли', 'август', 'септември', 'октомври', 'ноември', 'декември'],
    monthsShort: ['ян', 'фев', 'мар', 'апр', 'май', 'юни', 'юли', 'авг', 'сеп', 'окт', 'ное', 'дек']
}
</script>
<style scoped>
input {
    margin-bottom: 20px;
}
</style>
