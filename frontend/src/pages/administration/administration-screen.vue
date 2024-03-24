<template>
  <div class="bg-sms">
    <div class="col-2"></div>
    <div class="col-8">
      <q-page class="page-content" padding>
        <div style="margin-top: 30px;">
          <div class="row">
            <div class="col-8">
              <q-tabs dense>
                <q-route-tab label="Класове" to="classes"/>
                <q-route-tab label="Учители" to="teachers"/>
                <q-route-tab label="Всички ученици" to="students"/>
                <q-route-tab label="Всички родители" to="parents"/>
                <q-route-tab label="Всички админи" to="admins"/>
              </q-tabs>
            </div>
            <div class="col-12">
              <router-view v-slot="{ Component }">
                <template v-if="Component">
                  <suspense>
                    <component :is="Component">
                    </component>
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
          </div>
        </div>
      </q-page>
    </div>
  </div>
</template>

<script lang="ts" setup>

import {watch} from "vue";
import {useRouter} from "vue-router";
import {periodId, schoolId} from "../../model/constants";

const props = defineProps<{
  periodId: number,
  schoolId: number
}>()


const route = useRouter()


watch(() => [schoolId.value, periodId.value], () => {
  const currentRouterFullPathSplit = route.currentRoute.value.fullPath.split("/");
  route.push({path: `/administration/${periodId.value}/${schoolId.value}/${currentRouterFullPathSplit[currentRouterFullPathSplit.length - 1]}`})
})

watch(props, async () => {

        }
)

</script>

<style scoped>

</style>