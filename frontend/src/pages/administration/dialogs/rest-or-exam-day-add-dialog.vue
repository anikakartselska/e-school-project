<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">{{ title }}
        <q-btn v-close-popup
               class="float-right text-black"
               dense
               flat
               icon="close"
               round/>
            </span>
      </q-card-section>
      <q-card-section>
        <q-form class="q-gutter-md" @submit="submit">
          <q-input v-model="updatedOrNewRestDay.holidayName" :label="type"
                   dense/>
          <q-field dense label="Период" stack-label>
            <div class="text-black" v-html="visualizeDateRange(dateRange)"/>
            <template v-slot:append>
              <q-btn color="grey-7" flat icon="clear" round size="xs" @click="dateRange = null"/>
              <q-icon class="cursor-pointer" name="event">
                <q-popup-proxy cover transition-hide="scale" transition-show="scale">
                  <q-date v-model="dateRange" range title="Период">
                    <div class="row items-center justify-end">
                      <q-btn v-close-popup color="primary" flat label="Затвори"/>
                    </div>
                  </q-date>
                </q-popup-proxy>
              </q-icon>
            </template>
          </q-field>
          <q-card-actions align="right">
            <q-btn color="primary" label="Запази" @click="submit"/>
            <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
          </q-card-actions>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent, useQuasar} from "quasar";
import {RestDay} from "../../../model/Calendar";
import {$ref} from "vue/macros";
import {formatToBulgarian} from "../../../utils";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  title: string,
  type: string,
  restDay: RestDay | null,
}>()

const updatedOrNewRestDay = $ref<RestDay>(props.restDay ? {...props.restDay} : <RestDay>{})

const dateRange = $ref<any>({from: updatedOrNewRestDay.from, to: updatedOrNewRestDay.to})
const visualizeDateRange = (dateRange) => {
  if (dateRange?.hasOwnProperty('from') && dateRange?.hasOwnProperty('to')) {
    return `${dateRange.from ? formatToBulgarian(dateRange.from) : ""} - ${dateRange.to ? formatToBulgarian(dateRange.to) : ""}`
  } else {
    return formatToBulgarian(dateRange)
  }
}

const submit = () => {
  const date = dateRange?.hasOwnProperty('from') && dateRange?.hasOwnProperty('to') ? {
    from: dateRange.from,
    to: dateRange.to
  } : {from: dateRange, to: dateRange}
  onDialogOK({
    item: {...updatedOrNewRestDay, from: date.from, to: date.to}
  })
}


</script>

<style scoped>

</style>