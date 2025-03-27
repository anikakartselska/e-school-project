<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Настройки на групата
        <q-btn v-close-popup
               class="float-right text-black"
               dense
               flat
               icon="close"
               round/>
            </span>
      </q-card-section>
      <q-card-section>
        <q-form class="q-gutter-md" @submit="submit">
            <q-input v-model="newlyCreatedChat.chatName" class="q-pa-sm"
                     label="Име на чата"/>
            <q-select
                    v-model="selectedUsers"
                    :option-label="user => user.firstName + ' ' + user.lastName + ' ('+ getUserRoles(user)+')'"
                    :options="users"
                    label="Потърсере потребител"
                    multiple
                    use-chips
                    use-input
                    @filter="filterFn"
            >
                <!-- Append slot for search icon -->
                <template v-slot:append>
                    <q-icon name="search"/>
                </template>

                <!-- Slot to customize the chips display -->
                <template v-slot:selected-item="props">
                    <q-chip
                            :key="props.opt.value"
                            :label="props.opt.firstName + ' ' + props.opt.lastName"
                            :removable="props.opt.id !== getCurrentUserAsUserView().id"
                            @remove="removeUserFromSelection(props.opt)"
                    />
                </template>
            </q-select>

          <q-card-actions align="right">
            <q-btn color="primary" label="Готово" @click="submit"/>
            <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
          </q-card-actions>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent, useQuasar} from "quasar";
import {Chat, ChatType} from "../../model/Chat";
import {$ref} from "vue/macros";
import {translationOfRoles} from "../../utils";
import {UserView} from "../../model/User";
import {get10UserViewsBySchoolMatchingSearchText} from "../../services/RequestService";
import {periodId, schoolId} from "../../model/constants";
import {getCurrentUserAsUserView} from "../../services/LocalStorageService";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  chat: Chat | null,
}>()

const newlyCreatedChat = $ref(props.chat ? {...props.chat} : <Chat>{chatType: ChatType.GROUP_CHAT})
let users = $ref<UserView[]>([])
let selectedUsers = $ref<UserView[]>(props.chat?.chatMembers ? [...props.chat.chatMembers] : [getCurrentUserAsUserView()])

const filterFn = (val, update) => {
  if (val === '') {
    update(() => {
      users = []

      // here you have access to "ref" which
      // is the Vue reference of the QSelect
    })
    return
  }

  update(async () => {
    users = await get10UserViewsBySchoolMatchingSearchText(schoolId.value, periodId.value, val)
  })
}
const submit = () => {
  onDialogOK({
    item: {
      chat: {
        ...newlyCreatedChat, chatMembers: selectedUsers
      }
    }
  })
}

const getUserRoles = (userView: UserView) => {
  if (userView.roles?.length == 0) {
    return "НЯМА АКТИВНИ РОЛИ"
  } else {
    return [...new Set(userView.roles?.map(it => translationOfRoles[it]))].join(",")
  }
}

const removeUserFromSelection = (userView: UserView) => {
    selectedUsers = selectedUsers.filter(user => user.id !== userView.id)
}
</script>

<style scoped>

</style>