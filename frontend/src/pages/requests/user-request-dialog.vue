<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Потребителят с тази роля все още няма одобрена заявка за регистрация
        <q-btn v-close-popup
               class="float-right text-black"
               dense
               flat
               icon="close"
               round/>
            </span>
      </q-card-section>
      <q-card>
        <q-card-actions>
          <div class="row">
            <div :class="`text-h5 q-pl-lg ${getRequestStatusColorClass(request.requestStatus)}`">
              {{ translationOfRequestStatus[request.requestStatus] }}
            </div>
            <div class="absolute-top-right q-pt-sm q-pr-sm">
              <q-btn :disable="getCurrentUser().id === request?.requestedByUser?.id || request.requestStatus !== RequestStatus.PENDING"
                     color="positive"
                     flat
                     label="Одобри"
                     @click="resolveRequest(request,RequestStatus.APPROVED)"
              />
              <q-tooltip v-if="getCurrentUser().id === request?.requestedByUser?.id">
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
                      Информация за потребителя:
                      </span><br>
            Име:<span class="text-primary">{{
              request.requestValue.user.firstName
            }} {{ request.requestValue.user.middleName }} {{
              request.requestValue.user.lastName
            }}</span><br>
            Потребителско име:<span class="text-primary">{{
              request.requestValue.user.username
            }}</span><br>
            ЕГН:<span class="text-primary">{{
              request.requestValue.user.personalNumber
            }}</span><br>
            Телефонен номер:<span class="text-primary">{{
              request.requestValue.user.phoneNumber
            }}</span><br>
            Пол:<span class="text-primary">{{ translationOfGender[request.requestValue.user.gender] }}</span><br>
            Адрес:<span class="text-primary">{{ request.requestValue.user.address }}</span>
            <q-separator spaced/>
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
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {getCurrentUser, getCurrentUserAsUserView} from "../../services/LocalStorageService";
import {
    confirmActionPromiseDialog,
    formatToBulgarian,
    getRequestStatusColorClass,
    translationOfGender,
    translationOfRequestStatus
} from "../../utils";
import {useDialogPluginComponent} from "quasar";
import {RequestStatus} from "../../model/RequestStatus";


const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const props = defineProps<{
  request: Request
}>()

const resolveRequest = async (request, requestStatus) => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
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