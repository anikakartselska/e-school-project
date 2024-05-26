<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Редактирай информацията за училище
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
            <q-input v-model="updatedSchool.schoolName" label="Име на училището" stack-label>
                <template v-slot:prepend>
                    <q-icon name="edit"/>
                </template>
            </q-input>
            <q-input v-model="updatedSchool.city" label="Град" stack-label>
                <template v-slot:prepend>
                    <q-icon name="edit"/>
                </template>
            </q-input>
            <q-input v-model="updatedSchool.address" label="Адрес" stack-label>
                <template v-slot:prepend>
                    <q-icon name="edit"/>
                </template>
            </q-input>
            <div class="text-primary q-pl-lg">{{ `Стаи:` }}
                <q-btn color="primary" dense flat icon="add_circle" size="sm" @click="addRoom()">
                </q-btn>
            </div>
            <q-scroll-area style="height: 30vh" visible>
                <q-list>
                    <q-item-label v-for="(room,index) in updatedSchool.rooms">
                        <q-btn color="primary" dense flat icon="edit" size="sm" @click="updateRoom(room)">
                        </q-btn>
                        <q-btn color="negative" dense flat icon="delete" size="sm" @click="deleteRoom(index)">
                        </q-btn>
                        {{ roomToSubjectsText(room) }}
                    </q-item-label>
                </q-list>
            </q-scroll-area>
            <q-card-actions align="right">
                <q-btn color="primary" label="Редактирай" @click="submit"/>
                <q-btn class="q-ml-sm" color="primary" flat label="Отказ" @click="onDialogCancel"/>
            </q-card-actions>
        </q-form>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>
import {useDialogPluginComponent, useQuasar} from "quasar";
import {$ref} from "vue/macros";
import {RoomToSubjects, roomToSubjectsText, School} from "../../../model/School";
import RoomsEditCreateDialog from "./rooms-edit-create-dialog.vue";
import {confirmActionPromiseDialog} from "../../../utils";


const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
    school: School,
    subjects: string[]
}>()

const updatedSchool: School = $ref({...props.school})

const addRoom = () => {
    quasar.dialog(
            {
                component: RoomsEditCreateDialog,
                componentProps: {
                    roomToSubjects: <RoomToSubjects><unknown>{subjects: []},
                    subjects: props.subjects
                },
            }).onOk(async (payload) => {
                const room = payload.item as RoomToSubjects
                updatedSchool.rooms?.push(room)
            }
    )
}
const updateRoom = (room: RoomToSubjects) => {
    quasar.dialog(
            {
                component: RoomsEditCreateDialog,
                componentProps: {
                    roomToSubjects: room,
                    subjects: props.subjects
                },
            }).onOk(async (payload) => {
                const updatedRoom = payload.item as RoomToSubjects
                updatedSchool.rooms = updatedSchool.rooms ? updatedSchool.rooms.map(it => {
                            if (it.room == updatedRoom.room) {
                                return updatedRoom
                            } else {
                                return it
                            }
                        }
                ) : []
            }
    )
}
const deleteRoom = (index: number) => {
    updatedSchool.rooms?.splice(index, 1)
}
const submit = async () => {
    await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
    onDialogOK({
        item: updatedSchool
    })
}
</script>

<style scoped>

</style>