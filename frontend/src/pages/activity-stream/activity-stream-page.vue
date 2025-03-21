<template>
  <div class="row q-col-gutter-lg">
    <div class="col-1"></div>
    <div class="col-10">
      <q-page class="page-content" padding>
        <div style="margin-top: 30px;">
          <div class="text-h4 q-mb-md">Известия</div>
          <q-separator/>
          <q-scroll-area style="height: 60vh">
            <q-infinite-scroll ref="infiniteScroll" :offset="250" @load="onLoad">
              <q-timeline class="q-pl-xl" color="secondary">
                <div v-for="(action, index) in actionsModels">
                  <q-timeline-entry v-if="checkIfMaxDate(action.executedTime, dates)" heading>
                    {{ formatDateToBulgarian(action.executedTime) }}
                  </q-timeline-entry>
                  <q-timeline-entry
                          v-if="actionsModels.length>0"
                          :key="index"
                          :subtitle="dateTimeToBulgarianLocaleString(action.executedTime)"
                          :title="action.action"
                          icon="info">
                    <q-separator/>
                  </q-timeline-entry>
                  <h6 v-else style="text-align: center">Няма известия
                    <q-icon name="sentiment_dissatisfied"/>
                  </h6>
                </div>
              </q-timeline>
              <template v-slot:loading>
                <div class="row justify-center q-my-md">
                  <q-spinner-dots color="primary" size="40px"/>
                </div>
              </template>
            </q-infinite-scroll>
          </q-scroll-area>
        </div>
      </q-page>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";
import {useRouter} from "vue-router";
import {QInfiniteScroll} from "quasar";
import {Actions, PaginatedFetchingInformationDTO} from "../../model/Actions";
import {getCurrentUser} from "../../services/LocalStorageService";
import {getActionsWithFiltersAndPagination} from "../../services/RequestService";
import {checkIfMaxDate, dateTimeToBulgarianLocaleString, getDistinctDates} from "../../utils";


const props = defineProps<{
  periodId: number
  schoolId: number
}>()

const router = useRouter()

let infiniteScroll = $ref<InstanceType<typeof QInfiniteScroll>>()
const currentUser = getCurrentUser()

const LOADED_ROWS_COUNT = 20


const getAllActionsWithPaginationAndFilters = async (loadingIndex): Promise<Actions[]> => {
  const actionsFetchingInformationDTO: PaginatedFetchingInformationDTO = {
    startRange: (loadingIndex * LOADED_ROWS_COUNT) - LOADED_ROWS_COUNT,
    endRange: loadingIndex * LOADED_ROWS_COUNT,
    forUserId: currentUser.id,
    periodId: props.periodId,
    schoolId: props.schoolId,
  }
  return await getActionsWithFiltersAndPagination(actionsFetchingInformationDTO)

}
let actionsModels = $ref<Actions[]>([])
let uniqueDates = $ref<Date[]>([])
let dates = $ref<Date[]>([])
const currentDate = new Date()
const onLoad = async (index: number, done: () => void) => {
  const fetched = await getAllActionsWithPaginationAndFilters(index)
  if (fetched.length < LOADED_ROWS_COUNT) {
    infiniteScroll.stop()
  }
  actionsModels = actionsModels.concat(fetched)
  dates = dates.concat(fetched.map(it => it.executedTime!!))
  uniqueDates = getDistinctDates(dates)
  done()
}

function formatDateToBulgarian(date: Date | string): string {
  const bgFormatter = new Intl.DateTimeFormat("bg-BG", {day: "2-digit", month: "long"});

  const validDate = new Date(date);
  if (isNaN(validDate.getTime())) return "Invalid Date"; // Handle invalid dates

  console.log(validDate.getDate())
  console.log(currentDate.getDate())
  return !(validDate.getDate() == currentDate.getDate() && validDate.getMonth() == currentDate.getMonth() && validDate.getFullYear() == currentDate.getFullYear()) ?
          bgFormatter.format(validDate) : 'Днес';
}
</script>
<style scoped>
.page-content {
  box-shadow: 0 30px 25px rgba(0, 0, 0, 0.4);
}

.menu {
  background-color: rgba(46, 185, 246, 0.27);
  color: black;
}

.scroll-area-for-find-by {
  height: 25vh;
}

.find-by-section {
  max-width: 36vh;
  margin-left: 7vh
}

q-toolbar {
  width: 36vh;
}

</style>
