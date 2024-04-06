<template>
  <q-layout>
    <q-page-container>
      <q-page class="flex flex-center">
        <div
                id="particles-js"
                :class="$q.dark.isActive ? 'dark_gradient' : 'normal_gradient'"
        ></div>
        <q-btn
                :icon="$q.dark.isActive ? 'nights_stay' : 'wb_sunny'"
                class="absolute-top-right"
                color="white"
                flat
                round
                @click="$q.dark.toggle()"
        />
        <q-card
                class="login-form"
                v-bind:style="
            $q.platform.is.mobile ? { width: '80%' } : { width: '30%' }"
        >
          <div class="col-12">
            <q-form class="q-gutter-md" @submit="updatePasswordRequest()">
              <div class="q-ma-xl">
                <span class="text-primary text-h6">Въведете нова парола:</span>
                <q-input v-model="newPassword" :rules="[val=>val !== null && val !== '' || 'Задължително поле']" class="q-pb-lg" filled label="Нова парола" lazy-rules
                         type="password"/>
                <q-input v-model="confirmNewPassword" :rules="[val=> val===newPassword || 'Паролите не съвпадат',val=>val !== null && val !== '' || 'Задължително поле']" filled label="Потвърди нова парола"
                         type="password"/>
                <q-btn class="q-mt-md q-mb-md float-right"
                       color="primary" label="Промени парола"
                       type="Submit"/>
              </div>
            </q-form>
          </div>
        </q-card>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script lang="ts" setup>

import {$ref} from "vue/macros";
import {updatePassword} from "../../services/RequestService";
import {useRouter} from "vue-router";

const props = defineProps<{
  token: string
}>()
const newPassword = $ref('')
const confirmNewPassword = $ref('')
const router = useRouter()
const updatePasswordRequest = async () => {
  await updatePassword(newPassword,
          props.token).then(async e =>
          await router.push("/login")
  )
}
</script>

<style>
#particles-js {
  position: absolute;
  width: 100%;
  height: 100%;
  background-repeat: no-repeat;
  background-size: cover;
  background-position: 50% 50%;
}

.normal_gradient {
  background: linear-gradient(145deg, rgb(74, 94, 137) 15%, #b61924 70%);
}

.dark_gradient {
  background: linear-gradient(145deg, rgb(11, 26, 61) 15%, #4c1014 70%);
}

.login-form {
  position: absolute;
}
</style>