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
              <router-link
                      active-class="text-negative q-pt-md q-sm-md"
                      class="text-primary q-pt-md q-pl-sm" exact-active-class="text-negative q-pt-md q-sm-md"
                      to="/login">
                  Назад
              </router-link>
              <q-form class="q-gutter-md">
                  <div class="q-ma-xl">
                      <span class="text-primary text-h6">Въведете имейл, на който да изпратим заявката за промяна на паролата</span>
                      <q-input v-model="email" filled label="Имайл" lazy-rules/>
                      <q-btn :disable="!email" class="q-mt-md q-mb-md float-right" color="primary" label="Изпрати"
                             @click="resetPassword()"/>
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
import {resetPasswordRequest} from "../../services/RequestService";
import {useRouter} from "vue-router";
import {notifyForError} from "../../utils";

const router = useRouter()
const email = $ref('')
let isEmailSent = $ref<null | boolean>(null)
const resetPassword = async () => {
    await resetPasswordRequest(email).then(async e => {
                isEmailSent = e.data
                if (isEmailSent === true) {
                    await router.push({path: "/login"})
                } else {
                    notifyForError("Грешка при изпращането на имейл", "Възникна проблем при изпращането на имейл за промяна на паролата. Опитайте отново!", true)
                }
            }
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