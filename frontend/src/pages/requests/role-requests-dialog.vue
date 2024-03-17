<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6 col-8">Отхвърлянето на тази заявка за регистрация автоматично ще отхвърли и заявките за роли на потребителя
        <q-btn v-close-popup
               class="absolute-top-right text-black"
               dense
               flat
               icon="close"
               round/>
            </span>
      </q-card-section>
      <q-card>
        <q-scroll-area style="height: 650px">
          <div v-for="request in userRequests">
            <q-separator/>
            <q-card-section>
              <div class="row">
                <div class="text-h5 q-pl-md">Статус:</div>
                <div :class="`text-h5 ${getRequestStatusColorClass(request.requestStatus)}`">
                  {{ translationOfRequestStatus[request.requestStatus] }}
                </div>
              </div>
            </q-card-section>
            <q-separator/>
            <q-card-section>
              <div class="q-pl-md q-pb-none">
                                <span class="text-h5 text-negative">
                      Информация за ролята:
                      </span><br>
                Роля:<span class="text-primary">
              {{
                  constructSchoolUserRoleMessage(request.requestValue.oneRoleUser.role)
                }}
                          </span>
                <q-separator spaced/>
                <span class="text-h5">
                      Информация за потребителя:
                      </span><br>
                Име:<span class="text-primary">
              {{
                  request.requestValue.oneRoleUser.firstName
                }} {{ request.requestValue.oneRoleUser.middleName }} {{
                  request.requestValue.oneRoleUser.lastName
                }}</span><br>
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
          </div>
        </q-scroll-area>
        <q-card-actions align="right">
          <q-btn color="primary" flat label="ок" @click="resolveRequest"/>
          <q-btn class="q-ml-sm" color="primary" flat label="отказ" @click="onDialogCancel"/>
        </q-card-actions>
      </q-card>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {getCurrentUserAsUserView} from "../../services/LocalStorageService";
import {formatToBulgarian, getRequestStatusColorClass, translationOfRequestStatus} from "../../utils";
import {useDialogPluginComponent} from "quasar";
import {constructSchoolUserRoleMessage} from "../../model/SchoolUserRole";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const props = defineProps<{
  requests: Request[]
}>()

const userRequests = [...props.requests]

const resolveRequest = (request, requestStatus) => {
  onDialogOK({
    item: {
      ...request, requestStatus: requestStatus, resolvedByUser: getCurrentUserAsUserView(),
      resolvedDate: new Date()
    }
  })
}
</script>

<style scoped>

</style>