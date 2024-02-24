<template>
    <h1>Component: {{ string }}</h1>
    <div>
        year: {{ year }} <br>
        year: {{ month }}
    </div>
</template>

<script lang="ts" setup>
import {$ref} from "vue/macros";


const props = defineProps<{
    year: string
    month: string
}>()


</script>

<style scoped>

</style>