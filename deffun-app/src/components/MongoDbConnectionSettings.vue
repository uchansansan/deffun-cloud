<template>
  <q-card >
    <q-card-section>
      <q-input
        filled
        v-model="projectName"
        label="Project name"
        hint='For example, "My super cool project"'
      />
    </q-card-section>
  </q-card>
</template>

<script setup lang="ts">
import { ref, defineEmits } from 'vue';
import { useProjectStore } from 'stores/project-store';

const mongoDbConnectionSettings = ref('');
const projectStore = useProjectStore();
const { createProject } = projectStore;

const emit = defineEmits(['projectCreated']);

const toggleCeateProject = async () => {
  await createProject(projectName.value);
  projectName.value = '';
  emit('projectCreated');
};
</script>
