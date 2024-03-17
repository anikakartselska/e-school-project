<template>
  <div class="col-2"></div>
  <div class="col-8">
    <q-page class="page-content" padding>
      <div style="margin-top: 30px;">
        <div class="row">
          <div class="col-8">
            <q-tabs dense>
              <q-route-tab label="Заявки за регистрация" to="user-requests"/>
              <q-route-tab label="Заявки за роля" to="role-requests"/>
            </q-tabs>
          </div>
          <div class="col-2">
            <q-select
                    v-model="filter"
                    hide-dropdown-icon
                    input-debounce="0"
                    label="Търси"
                    multiple
                    new-value-mode="add-unique"
                    style="width: 250px"
                    use-chips
                    use-input
            >
              <template v-slot:append>
                <q-icon name="search"></q-icon>
              </template>
            </q-select>
          </div>
          <div class="col-12">
            <router-view v-slot="{ Component }"
                         v-model:roleRequests="filteredRoleRequests"
                         v-model:userRequests="filteredUserRequests"
            >
              <template v-if="Component">
                <suspense>
                  <component :is="Component">
                    <template v-slot:toggleSlot>
                      <q-toggle v-model="requestStatusFilters"
                                :label="translationOfRequestStatus[RequestStatus.PENDING]"
                                :val="RequestStatus.PENDING"
                                color="blue"/>
                      <q-toggle v-model="requestStatusFilters"
                                :label="translationOfRequestStatus[RequestStatus.REJECTED]"
                                :val="RequestStatus.REJECTED"
                                color="red"/>
                      <q-toggle v-model="requestStatusFilters"
                                :label="translationOfRequestStatus[RequestStatus.APPROVED]"
                                :val="RequestStatus.APPROVED"
                                color="green"/>
                    </template>
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
</template>

<script lang="ts" setup>

import {watch} from "vue";
import {useRouter} from "vue-router";
import {periodId, schoolId} from "../../model/constants";
import {$ref} from "vue/macros";
import {getRoleRequestsBySchoolAndPeriod, getUserRequestsBySchoolAndPeriod} from "../../services/RequestService";
import {translationOfRequestStatus} from "../../utils";
import {RequestStatus} from "../../model/RequestStatus";
import {constructSchoolUserRoleMessage} from "../../model/SchoolUserRole";

const props = defineProps<{
  periodId: number,
  schoolId: number
}>()
const filter = $ref(<string[]>[])

const route = useRouter()
const requestStatusFilters = $ref([RequestStatus.PENDING,
  RequestStatus.REJECTED,
  RequestStatus.APPROVED])

let roleRequests = $ref(await getRoleRequestsBySchoolAndPeriod(props.periodId, props.schoolId))
let userRequests = $ref(await getUserRequestsBySchoolAndPeriod(props.periodId, props.schoolId))
let filteredRoleRequests = $ref([...roleRequests])
let filteredUserRequests = $ref([...userRequests])

watch(() => [requestStatusFilters, roleRequests, userRequests, filter], () => {
  filteredRoleRequests = roleRequests.filter(value => requestStatusFilters.includes(value.requestStatus) && (filter.length == 0 || filter.find(fil => stringifyRoleRequest(value).includes(fil.toLowerCase()))))
  filteredUserRequests = userRequests.filter(value => requestStatusFilters.includes(value.requestStatus) && (filter.length == 0 || filter.find(fil => stringifyUserRequest(value).includes(fil.toLowerCase()))))
})

const stringifyRoleRequest = (request) => JSON.stringify(request).concat(constructSchoolUserRoleMessage(request.requestValue.oneRoleUser.role).toLowerCase())
const stringifyUserRequest = (request) => JSON.stringify(request).toLowerCase()

watch(() => [schoolId.value, periodId.value], () => {
  const currentRouterFullPathSplit = route.currentRoute.value.fullPath.split("/");
  route.push({path: `/requests/${periodId.value}/${schoolId.value}/${currentRouterFullPathSplit[currentRouterFullPathSplit.length - 1]}`})
})

watch(props, async () => {
          periodId.value = props.periodId.toString()
          schoolId.value = props.schoolId.toString()
          roleRequests = await getRoleRequestsBySchoolAndPeriod(props.periodId, props.schoolId)
          userRequests = await getUserRequestsBySchoolAndPeriod(props.periodId, props.schoolId)
        }
)

</script>

<style scoped>

</style>