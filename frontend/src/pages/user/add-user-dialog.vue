<template>
  <Suspense>
    <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
      <q-card class="q-dialog-plugin" style="width: 500px;">
        <q-card-section class="dialog-header">
          <span class="text-h6">Добави потребител
        <q-btn v-close-popup
               class="float-right text-black"
               dense
               flat
               icon="close"
               round/>
            </span>
        </q-card-section>
        <add-user-component v-model:new-user="user" v-model:submitted="submitted" :in-dialog="true"/>
      </q-card>
    </q-dialog>
  </Suspense>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent, useQuasar} from "quasar";
import {$ref} from "vue/macros";
import {watch} from "vue";
import AddUserComponent from "./add-user-component.vue";
import {User} from "../../model/User";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()

const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const submitted = $ref(false)
const user = $ref(<User>{})


watch(() => submitted, () => {
  console.log(submitted)
  if (submitted == true) {
    onDialogOK({
      item: user
    })
  }
})

</script>

<style scoped>

</style>