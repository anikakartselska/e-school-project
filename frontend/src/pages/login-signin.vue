<template>
  <q-layout>
    <q-page-container>
      <q-page class="flex flex-center">
        <div
                id="particles-js"
                :class="$q.dark.isActive ? 'dark_gradient' : 'normal_gradient'"
        ></div>
        <q-btn
                color="white"
                class="absolute-top-right"
                flat
                round
                @click="$q.dark.toggle()"
                :icon="$q.dark.isActive ? 'nights_stay' : 'wb_sunny'"
        />
        <q-card
                class="login-form"
                v-bind:style="
            $q.platform.is.mobile ? { width: '80%' } : { width: '30%' }"
        >
          <q-tabs
                  narrow-indicator
                  dense
                  align="justify"
                  class="text-primary"
          >
            <q-route-tab :ripple="false" name="LOGIN" icon="login" label="Вход" to="login"/>
            <q-route-tab :ripple="false" name="SIGN_IN" icon="app_registration" label="Регистрация" to="sign-in"/>
          </q-tabs>
          <div class="col-12">
            <router-view v-slot="{ Component }"
            >
              <template v-if="Component">
                <suspense>
                  <component :is="Component"></component>
                  <template #fallback>
                    <div class="centered-div">
                      <q-spinner
                              :thickness="2"
                              color="primary"
                              size="5.5em"
                      />
                    </div>
                  </template>
                </suspense>
              </template>
            </router-view>
          </div>
        </q-card>
      </q-page>
    </q-page-container>
  </q-layout>
</template>

<script lang="ts" setup></script>

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