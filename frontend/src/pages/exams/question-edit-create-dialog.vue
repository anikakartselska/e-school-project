<template>
  <q-dialog ref="dialogRef" persistent @hide="onDialogHide">
    <q-card class="q-dialog-plugin" style="width: 500px;">
      <q-card-section class="dialog-header">
          <span class="text-h6">{{ title }}
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
          <q-select v-model="questionType"
                    :option-label="(option) => translationOfQuestionType[option]"
                    :options="Object.keys(QuestionType)"
                    class="text-black"
                    label="Тип на въпроса"
                    label-color="primary"/>
          <div v-if="questionType === QuestionType.OPEN_QUESTION">
            <q-input v-model="updatedQuestion.questionTitle" autogrow label="Въпрос" stack-label>
              <template v-slot:prepend>
                <q-icon name="edit"/>
              </template>
            </q-input>
            <q-input v-model="updatedQuestion.questionDescription" autogrow label="Описание" stack-label>
              <template v-slot:prepend>
                <q-icon name="edit"/>
              </template>
            </q-input>
            <q-input v-model="updatedQuestion.points" autogrow label="Точки" stack-label type="number">
              <template v-slot:prepend>
                <q-icon name="edit"/>
              </template>
            </q-input>
          </div>
          <div v-if="questionType === QuestionType.CHOICE_QUESTION">
            <q-input v-model="updatedQuestion.questionTitle" autogrow label="Въпрос" stack-label>
              <template v-slot:prepend>
                <q-icon name="edit"/>
              </template>
            </q-input>
            <q-input v-model="updatedQuestion.questionDescription" autogrow label="Описание" stack-label>
              <template v-slot:prepend>
                <q-icon name="edit"/>
              </template>
            </q-input>
            <q-input v-model="updatedQuestion.points" autogrow label="Точки" stack-label type="number">
              <template v-slot:prepend>
                <q-icon name="edit"/>
              </template>
            </q-input>
            <q-btn class="bg-secondary q-mt-sm" dense icon="add" rounded text-color="white" @click="addChoice()">
              <q-tooltip>Добави отговор</q-tooltip>
            </q-btn>
            <div v-if="updatedQuestion.possibleAnswersToIfCorrect.length > 0" class="q-pt-sm q-pb-sm">Отбележете верните
              отговори:
            </div>
            <div v-for="(choice,index) in updatedQuestion.possibleAnswersToIfCorrect">
              <div class="row">
                <q-checkbox v-model="updatedQuestion.possibleAnswersToIfCorrect[index].correct"></q-checkbox>
                <q-input v-model="updatedQuestion.possibleAnswersToIfCorrect[index].text" autogrow dense></q-input>
                <q-btn class="text-negative" dense flat icon="delete" round
                       @click="removeElement(index);"></q-btn>
              </div>
            </div>
          </div>
          <div>
            <q-avatar v-if="updatedQuestion.picture" class="q-ma-sm" font-size="400px" size="400px"
                      square text-color="white">
              <q-img
                      :src="imageUrl(file,updatedQuestion.questionUUID)"
                      fit="contain"
                      ratio="1"
                      spinner-color="white"
              ></q-img>
            </q-avatar>
          </div>
          <div class="row">
            <q-file v-model="file" :display-value="updatedQuestion.picture ? 'Смени снимка' : 'Добави снимка'"
                    accept="image/*"
                    class="col"
                    dense
                    outlined
                    @update:model-value="value => handleUpdate(value)"
            >
              <template v-slot:prepend>
                <q-icon name="attach_file"/>
              </template>
            </q-file>
            <q-btn v-if="updatedQuestion.picture" class="col-1 bg-negative text-white" icon="remove"
                   @click="value => handleDelete(value)">
              <q-tooltip>Премахни снимката</q-tooltip>
            </q-btn>
          </div>
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

import {$ref} from "vue/macros";
import {useDialogPluginComponent, useQuasar} from "quasar";
import {
    ChoiceQuestion,
    defineQuestionType,
    OpenQuestion,
    PossibleAnswersToIfCorrect,
    Question,
    QuestionType,
    translationOfQuestionType
} from "../../model/Questions";
import {cloneDeep} from "lodash-es";
import {confirmActionPromiseDialog, generateUUID} from "../../utils";
import {watch} from "vue";


const {dialogRef, onDialogHide, onDialogOK, onDialogCancel} = useDialogPluginComponent()
const quasar = useQuasar()
defineEmits([...useDialogPluginComponent.emits])

const props = defineProps<{
  question: Question | null,
  title: string,
}>()

let updatedQuestion = $ref<Question>(props.question ? cloneDeep(props.question) : <OpenQuestion><unknown>{
  questionTitle: null,
  questionDescription: null
})
let file = $ref(updatedQuestion?.picture ? base64ToImageFile(updatedQuestion?.picture, updatedQuestion.questionUUID) : null)
const questionType = $ref<QuestionType | null>(updatedQuestion ? defineQuestionType(updatedQuestion) : null)

function imageFileToBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string); // Base64 Data URL
    reader.onerror = reject;
    reader.readAsDataURL(file); // Read file as Base64
  });
}

const cachedImageUrls = new Map<string, string>();
const imageUrl = (file: File, uuid: string) => {
  if (!file) return '';
  if (!cachedImageUrls.has(uuid)) {
    cachedImageUrls.set(uuid, window.URL.createObjectURL(file));
  }
  return cachedImageUrls.get(uuid);
};

const handleUpdate = async (value: File) => {
  const picture = await imageFileToBase64(value)
  updatedQuestion = {...updatedQuestion, picture: picture}
}

const handleDelete = async (value: File) => {
  file = null
  updatedQuestion = {...updatedQuestion, picture: null}
}

function base64ToImageFile(base64String: string, fileName: string): File {
  const arr = base64String.split(",");
  const mimeType = arr[0].match(/:(.*?);/)?.[1] || "image/png";
  const byteCharacters = atob(arr[1]); // Decode Base64
  const byteNumbers = new Uint8Array(byteCharacters.length);

  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }

  const blob = new Blob([byteNumbers], {type: mimeType});
  return new File([blob], fileName, {type: mimeType});
}

watch(() => questionType, () => {
  switch (questionType) {
    case QuestionType.CHOICE_QUESTION: {
      updatedQuestion = <ChoiceQuestion><unknown>{
        questionTitle: null,
        questionDescription: null,
        possibleAnswersToIfCorrect: []
      }
      break;
    }
    case QuestionType.OPEN_QUESTION: {
      updatedQuestion = <OpenQuestion><unknown>{
        questionTitle: null,
        questionDescription: null
      }
      break;
    }
    default: {
      updatedQuestion = <OpenQuestion><unknown>{
                questionTitle: null,
                questionDescription: null
              }
              break
            }
          }
        }
)

const submit = async () => {
  await confirmActionPromiseDialog("Сигурни ли сте, че искате да продължите?")
  onDialogOK({
    item: {...updatedQuestion, questionUUID: generateUUID()}
  })
}

const addChoice = () => {
  (<ChoiceQuestion>updatedQuestion).possibleAnswersToIfCorrect.push(<PossibleAnswersToIfCorrect>{
    text: '',
    correct: false,
    uuid: generateUUID()
  })
}

const removeElement = (index) => {

  updatedQuestion.possibleAnswersToIfCorrect.splice(index, 1)

}</script>

<style scoped>

</style>