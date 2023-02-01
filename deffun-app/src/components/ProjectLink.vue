<template>
  <q-item clickable>
    <!--    <q-item-section v-if="icon" avatar>-->
    <!--      <q-icon :name="icon" />-->
    <!--    </q-item-section>-->

    <q-item-section>
      <q-item-label>{{ name }}</q-item-label>
    </q-item-section>
  </q-item>
</template>

<script setup lang="ts">
import { defineProps } from 'vue';

defineProps({
  name: String,
});
</script>
