<template>
  <q-dialog ref="dialogRef" persistent style="width: 1000px;" @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 1000px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">Прикачени файлове
              <q-btn color="secondary" dense flat icon="add" @click="addFile()">
                  <q-tooltip>
                      Добави файл
                  </q-tooltip>
              </q-btn>
        <q-btn v-close-popup
               class="float-right text-black"
               dense
               flat
               icon="close"
               round/>
            </span>
        <q-table
                :columns="columns"
                :pagination="{rowsPerPage:20}"
                :rows="files"
                hide-pagination
                no-data-label="Няма данни в таблицата"
                no-results-label="Няма резултати от вашето търсене"
                row-key="id"
                rows-per-page-label="Редове на страница"
                virtual-scroll
        >
          <template v-slot:body-cell-edit="props">
            <q-td>
              <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN]) || props.row.createdBy.id === currentUser.id "
                     color="primary" dense flat
                     icon="edit"
                     @click="updateFile(props.row)">
              </q-btn>
              <q-btn v-if="currentUserHasAnyRole([SchoolRole.ADMIN]) || props.row.createdBy.id === currentUser.id"
                     color="negative" dense flat
                     icon="delete"
                     @click="deleteFile(props.row)">
              </q-btn>
              <q-btn color="secondary" dense flat
                     icon="download"
                     @click="downloadFile(props.row)">
                <q-tooltip>
                  Свали файлове
                </q-tooltip>
              </q-btn>
            </q-td>
          </template>
        </q-table>
      </q-card-section>
    </q-card>
  </q-dialog>
</template>

<script lang="ts" setup>

import {useDialogPluginComponent, useQuasar} from "quasar";
import {SmsFile} from "../../../model/SmsFile";
import {deleteFileById, downloadFileById, getFileWithoutDownload, uploadFile} from "../../../services/RequestService";
import FileAddDialog from "./file-add-dialog.vue";
import {currentUserHasAnyRole, getCurrentUser} from "../../../services/LocalStorageService";
import {$computed, $ref} from "vue/macros";
import {confirmActionPromiseDialog, dateTimeToBulgarianLocaleString} from "../../../utils";
import {SchoolRole} from "../../../model/User";

const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  smsFiles: SmsFile[],
  assignmentId: number
}>()
const currentUser = getCurrentUser()
let files = $ref<SmsFile[]>(props.smsFiles)
const addFile = async () =>
        quasar.dialog({
          component: FileAddDialog,
          componentProps: {
            title: "Добави файл",
          },
        }).onOk(async (payload) => {
          await uploadFile(payload.item.file, payload.item.file?.name, currentUser.id, payload.item.note, null, props.assignmentId).then(r =>
                  files.push(r.data)
          )
        })

const deleteFile = async (file: SmsFile) => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  await deleteFileById(file.id!!).then(r => {
    files?.splice(files.indexOf(file), 1)
  })
}

const updateFile = async (file: SmsFile) =>
        quasar.dialog({
          component: FileAddDialog,
          componentProps: {
            title: "Редактирай файл",
            note: file.note,
            fileContent: await getFileWithoutDownload(file.id!!)
          },
        }).onOk(async (payload) => {
          await uploadFile(payload.item.file, payload.item.file?.name, currentUser.id, payload.item.note, null, props.assignmentId, null, file.id).then(r => {
                    files = files?.map((smsFile) => {
                      if (smsFile.id == file.id) {
                        return r.data
                      } else {
                        return smsFile
                      }
                    })
                  }
          )
        })
const downloadFile = async (file: SmsFile) => {
  await downloadFileById(file.id!!)
}
const columns = $computed(() => [
  {
    name: 'edit',
    headerClasses: 'q-table--col-auto-width'
  },
  {
    name: "fileName",
    label: "Име на файла",
    align: "left",
    field: (row: SmsFile) => row.fileName,
    sortable: true
  },
  {
    name: "createdBy",
    label: "Създадено от",
    align: "left",
    field: (row: SmsFile) => `${row.createdBy.firstName} ${row.createdBy.lastName}`,
    sortable: true
  },
  {
    name: "createdOn",
    label: "Дата на създаване",
    align: "left",
    field: (row: SmsFile) => dateTimeToBulgarianLocaleString(row.createdOn),
    sortable: true
  },
  {
    name: "note",
    label: "Описание",
    align: "left",
    field: (row: SmsFile) => row.note,
    sortable: true
  },
])
</script>

<style scoped>

</style>