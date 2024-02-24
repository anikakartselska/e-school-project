<template>
    <h1> HOME </h1>
    <div>
        <div>{{ number }}</div>
        <q-btn icon="add" @click="myFunction()"></q-btn>
    </div>

</template>

<script lang="ts" setup>

import {$ref} from "vue/macros";

let number = $ref(1)

const myFunction = () => number++;
</script>

<style scoped>

</style>