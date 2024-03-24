<template>
  <router-view></router-view>
</template>

<script lang="ts" setup>
import {setUpAxios} from "./boot/axios";
import {useQuasar} from "quasar";

const quasar = useQuasar()
setUpAxios(quasar)
</script>


<style lang="sass">
@import "style.sass"
</style>
