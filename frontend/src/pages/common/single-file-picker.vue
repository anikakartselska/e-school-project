<template>
  <q-file :accept="props.acceptFileFormat"
          :disable="props.disablePicker ?? false"
          :label="props.label"
          :model-value="props.modelValue"
          bottom-slots
          counter
          dense
          max-files="1"
          @rejected="onRejected"
          @update:model-value="$emit('update:modelValue',$event)"
  >
    <template v-slot:before>
      <q-icon name="attachment"/>
    </template>
    <template v-slot:append>
      <q-icon v-if="props.modelValue !== null" class="cursor-pointer" name="close"
              @click.prevent="$emit('update:modelValue', null)"/>
      <q-icon name="add" @click.stop/>
    </template>
    <template v-slot:hint>
      {{ props.hint }}
    </template>
    <template v-if="!props.removeActionButton" v-slot:after>
      <q-btn :color="!props.modelValue ? 'grey' : props.actionColor ?? 'primary'"
             :disable="!props.modelValue || props.disablePicker"
             :icon="props.actionIcon ?? 'file_upload'"
             :label="props.actionLabel"
             dense
             @click="emits('action-button-event')"
      >
        <slot name="actionButtonSlot"></slot>
      </q-btn>
    </template>
  </q-file>
</template>

<script lang="ts" setup>
import {useQuasar} from "quasar";
import {$ref} from "vue/macros";

const quasar = useQuasar()

const props = defineProps<{
  modelValue: File,
  acceptFileFormat?: '.xlsx' | '.csv' | '.jpg' | '.pdf' | 'image/*', // if null All files are accepted
  label: string,
  hint?: string,
  disablePicker?: boolean,
  removeActionButton?: false,
  actionIcon?: string,
  actionLabel?: string,
  actionColor?: string
}>()
const emits = defineEmits<{
  (e: 'update:modelValue', value: File): void,
  (e: 'action-button-event'): void
}>();

const file = $ref(null)

const onRejected = (rejectedFiles) => {
  quasar.notify({
    type: 'negative',
    message: 'Грешен формат на файловете'
  })
}

</script>


