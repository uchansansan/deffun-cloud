<template>
  <q-card>
    <q-card-section>
      <q-input
        filled
        v-model="projectName"
        label="Project name"
        hint='For example, "My super cool project"'
      />
    </q-card-section>

    <q-card-actions align="right" class="text-primary">
      <!-- for cancel btn / note; probably prop solution is better -->
      <slot />
      <q-btn
        flat
        label="Add project"
        @click="toggleCeateProject"
        v-close-popup
      />
    </q-card-actions>
  </q-card>
</template>

<script setup lang="ts">
import { ref, defineEmits } from 'vue';
import { useProjectStore } from 'stores/project-store';

const projectName = ref('');
const projectStore = useProjectStore();
const { createProject } = projectStore;

const emit = defineEmits(['projectCreated']);

const toggleCeateProject = async () => {
  await createProject(projectName.value);
  projectName.value = '';
  emit('projectCreated');
};
</script>
