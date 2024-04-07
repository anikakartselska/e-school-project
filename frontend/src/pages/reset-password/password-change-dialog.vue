<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin">
      <q-card-section class="dialog-header">
        <div class="text-h6">Смяна на парола</div>
        <q-btn v-close-popup class="absolute-top-right" dense flat icon="close" round/>
      </q-card-section>
      <q-card-section>
        <q-form class="q-gutter-md" @submit="submit">
          <q-input v-model="oldPassword" :rules="[val=>val !== null && val !== '' || 'Задължително поле']"
                   class="q-pb-xs" filled label="Стара парола" lazy-rules
                   type="password"/>
          <q-separator/>
          <q-input v-model="newPassword" :rules="[val=>val !== null && val !== '' || 'Задължително поле']"
                   class="q-pb-xs" filled label="Нова парола" lazy-rules
                   type="password"/>
          <q-input v-model="confirmNewPassword"
                   :rules="[val=> val===newPassword || 'Паролите не съвпадат',val=>val !== null && val !== '' || 'Задължително поле']"
                   filled label="Потвърди нова парола"
                   type="password"/>
          <q-card-actions align="right">
            <q-btn color="primary" label="Смени парола" type="update"/>
            <q-btn color="primary" flat label="Отказ" @click="onDialogCancel"/>
          </q-card-actions>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent} from "quasar";
import {$ref} from "vue/macros";
import {changeUserPasswordWithOldPasswordProvided, loginAfterSelectedRole} from "../../services/RequestService";
import {getCurrentUser} from "../../services/LocalStorageService";
import {periodId} from "../../model/constants";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()

defineEmits([...useDialogPluginComponent.emits])
const newPassword = $ref('')
const confirmNewPassword = $ref('')
const oldPassword = $ref('')


const submit = async () => {
  onDialogOK(
          await changeUserPasswordWithOldPasswordProvided(newPassword, oldPassword).then(async e =>
                  await loginAfterSelectedRole(getCurrentUser().role.id!!, periodId.value)
          )
  )
}
</script>

<style scoped>

</style>