<template>
    <div class="row">
        <div class="col-1"></div>
        <div class="col">
            <q-page class="page-content" padding>
                <div style="margin-top: 30px;">
                    <div class="row">
                        <div class="text-h4 q-mb-md">
                            {{ props.isStatusChange ? 'Заявки за промяна на статус на роля' : 'Заявки за роля' }}
                        </div>
                        <q-space/>
                        <div class="q-pb-lg q-gutter-sm">
                            <slot name="toggleSlot"></slot>
                        </div>
                    </div>
                    <q-separator/>
          <div class="row">
            <div class="col">
              <div class="q-pa-md q-gutter-md">
                <q-scroll-area style="height: 70vh;">
                  <div class="row">
                    <transition-group name="list">
                      <div v-for="request in filteredRequests"
                           :key="request" class="col-6 q-pa-md">
                        <q-card>
                          <q-card-actions>
                            <div class="row">
                              <div :class="`text-h5 q-pl-lg ${getRequestStatusColorClass(request.requestStatus)}`">
                                {{ translationOfRequestStatus[request.requestStatus] }}
                              </div>
                              <div class="absolute-top-right q-pt-sm q-pr-sm">
                                <q-btn
                                        color="positive"
                                        flat
                                        label="Одобри"
                                        @click="resolveRequest(request,RequestStatus.APPROVED)"
                                />
                                <q-tooltip v-if="getCurrentUser().id === request.requestedByUser?.id">
                                  Не може да одобрите ваша заявка
                                </q-tooltip>
                                <q-tooltip v-else-if="request.requestStatus !== RequestStatus.PENDING">
                                  Заявката вече е {{ translationOfRequestStatus[request.requestStatus] }}
                                </q-tooltip>

                                <q-btn :disable="request.requestStatus !== RequestStatus.PENDING"
                                       color="negative"
                                       flat
                                       label="Отхвърли"
                                       @click="resolveRequest(request,RequestStatus.REJECTED)"/>
                                <q-tooltip v-if="request.requestStatus !== RequestStatus.PENDING">
                                  Заявката вече е {{ translationOfRequestStatus[request.requestStatus] }}
                                </q-tooltip>
                              </div>
                            </div>
                          </q-card-actions>
                          <q-separator/>
                          <q-card-section>
                              <div class="q-pl-md q-pb-none">
                                <span class="text-h5 text-negative">
                      Информация за ролята:
                      </span><br>
                                  Роля:<span class="text-primary">{{
                                  constructSchoolUserRoleMessage(request.requestValue.oneRoleUser.role)
                                  }}
                          </span>
                                  <div v-if="isStatusChange">
                        <span class="text-negative">
                      Нов статус:
                      </span>
                                      <span :class="getRequestStatusColorClass(request.requestValue.status)">{{
                                          translationOfRequestStatusForRole[request.requestValue.status]
                                          }}
                                </span>
                                      <br>
                                  </div>
                                  <q-separator spaced/>
                                  <span class="text-h5">
                      Информация за потребителя:
                      </span><br>
                                  Име:
                                  <router-link
                                          :to="`/user/${request.requestValue.oneRoleUser.id}/${periodId}/${schoolId}`"
                                          active-class="text-negative" class="text-primary"
                                          exact-active-class="text-negative">
                                      {{
                                      request.requestValue.oneRoleUser.firstName
                                      }} {{ request.requestValue.oneRoleUser.middleName }} {{
                                      request.requestValue.oneRoleUser.lastName
                                      }}
                                  </router-link>
                                  <br>
                                  Потребителско име:<span class="text-primary">{{
                                  request.requestValue.oneRoleUser.username
                                  }}</span><br>
                                  Телефонен номер:<span class="text-primary">{{
                                  request.requestValue.oneRoleUser.phoneNumber
                                  }}</span><br>
                                  <q-separator/>
                                  <span class="text-h5">
                      Информация за заявката:
                      </span><br>
                                  <span class="text-h6">Създадена от:</span><br>
                              Име:<span class="text-primary">{{ request.requestedByUser.firstName }} {{
                                request.requestedByUser.lastName
                              }}</span><br>
                              Имейл: <span class="text-primary">
                            {{ request.requestedByUser.email }}
                            </span> <br>
                              Потребителско име: <span class="text-primary">
                            {{ request.requestedByUser.username }}</span> <br>
                              Дата на създаване: <span class="text-primary">
                            {{ formatToBulgarian(request.requestDate) }}</span> <br>
                              <span class="text-h6">Разрешена от:</span><br>
                              Име:<span class="text-primary">{{ request?.resolvedByUser?.firstName }} {{
                                request?.resolvedByUser?.lastName
                              }}</span><br>
                              Имейл: <span class="text-primary">
                            {{ request?.resolvedByUser?.email }}
                            </span> <br>
                              Потребителско име: <span class="text-primary">
                            {{ request?.resolvedByUser?.username }}</span> <br>
                              Дата на създаване: <span class="text-primary">
                            {{ formatToBulgarian(request.resolvedDate) }}</span> <br>
                            </div>
                          </q-card-section>
                        </q-card>
                      </div>
                    </transition-group>
                  </div>
                </q-scroll-area>
              </div>
            </div>
          </div>
        </div>
      </q-page>
    </div>
    <div class="col-1"></div>
  </div>
</template>

<script lang="ts" setup>

import {changeRequestStatus} from "../../services/RequestService";
import {$ref} from "vue/macros";
import {getCurrentUser, getCurrentUserAsUserView} from "../../services/LocalStorageService";
import {RequestStatus} from "../../model/RequestStatus";
import {
    formatToBulgarian,
    getRequestStatusColorClass,
    translationOfRequestStatus,
    translationOfRequestStatusForRole
} from "../../utils";
import {watch} from "vue";
import {constructSchoolUserRoleMessage} from "../../model/SchoolUserRole";
import {useQuasar} from "quasar";
import UserRequestDialog from "./user-request-dialog.vue";
import {Request, Role, UserRegistration} from "../../model/Request";
import {periodId, schoolId} from "../../model/constants";

const props = defineProps<{
    userRequests: Request[],
    roleRequests: Request[],
    isStatusChange?: boolean
}>()

const emits = defineEmits<{
    (e: 'update:userRequests', value: Request[]): void
    (e: 'update:roleRequests', value: Request[]): void
}>();

const quasar = useQuasar()
let filteredRequests = $ref([...props.roleRequests].filter(request => (props.isStatusChange && (<Role>request.requestValue).status != null) || (!props.isStatusChange && (<Role>request.requestValue).status == null)))

watch(props, async () => {
    filteredRequests = [...props.roleRequests].filter(request => (props.isStatusChange && (<Role>request.requestValue).status != null) || (!props.isStatusChange && (<Role>request.requestValue).status == null))
})
const resolveRequest = async (request: Request, requestStatus: RequestStatus) => {
    const userRequestForCurrentRole = props.userRequests.find(req => (<UserRegistration>req.requestValue).user.id === (<Role>request.requestValue).oneRoleUser.id)
    if (userRequestForCurrentRole) {
        await quasar.dialog({
            component: UserRequestDialog,
            componentProps: {
                request: userRequestForCurrentRole
            },
        }).onOk(async (payload) => {
            const userRequest = payload.item as Request
            // @ts-ignore
            await changeRequestStatus(userRequest.id, userRequest.requestStatus, getCurrentUserAsUserView().id).then(async e => {
                emits("update:userRequests",
                        [...props.userRequests].map(
                                req => {
                                    if (req.id == userRequest.id) {
                                        return {
                                            ...userRequest,
                                            requestStatus: userRequest.requestStatus,
                                            resolvedByUser: getCurrentUserAsUserView(),
                                            resolvedDate: new Date()
                                        }
                          } else {
                            return req
                          }
                        }
                )
        )
        if (userRequest.requestStatus == RequestStatus.APPROVED) {
          await changeRequestStatus(request.id, requestStatus, getCurrentUserAsUserView().id).then(e => {
                    emits("update:roleRequests",
                            [...props.roleRequests].map(
                                    req => {
                                      if (req.id == request.id) {
                                        return {
                                          ...request,
                                          requestStatus: requestStatus,
                                          resolvedByUser: getCurrentUserAsUserView(),
                                          resolvedDate: new Date()
                                        }
                                      } else {
                                        return req
                                      }
                                    }
                            )
                    )
                  }
          )
        }
      })
    })
  } else {
    await changeRequestStatus(request.id, requestStatus, getCurrentUserAsUserView().id).then(e => {
              emits("update:roleRequests",
                      [...props.roleRequests].map(
                              req => {
                                if (req.id == request.id) {
                                  return {
                                    ...request,
                                    requestStatus: requestStatus,
                                    resolvedByUser: getCurrentUserAsUserView(),
                                    resolvedDate: new Date()
                                  }
                                } else {
                                  return req
                                }
                              }
                      )
              )
            }
    )
  }
}

</script>

<style scoped>
.absolute-top-right {
  position: absolute;
  top: 0;
  right: 0;
}

.margin-top {
  padding: 0 5px 5px;
  margin-top: 7px;
}

.list-move, /* apply transition to moving elements */
.list-enter-active,
.list-leave-active {
  transition: all 0.4s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* ensure leaving items are taken out of layout flow so that moving
   animations can be calculated correctly. */
.list-leave-active {
  position: absolute;
}

.highlight:hover {
  background-color: lightgray;
}

</style>