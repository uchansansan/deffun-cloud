<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn
          flat
          dense
          round
          icon="menu"
          aria-label="Menu"
          @click="toggleLeftDrawer"
        />

        <q-toolbar-title> deffun </q-toolbar-title>
        <div style="margin: 0 10px;">v0.0.2-pre-alpha-test</div>

        <q-separator v-if="currentUser" vertical />
        <div v-if="currentUser" style="margin: 0 10px;">{{ currentUser.balance }} &#8381;</div>
        <q-separator v-if="currentUser" vertical />
        <q-btn v-if="currentUser" flat no-caps href="/logout" label="Sign out" />
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <q-list>
        <div v-if="currentUser">
          <q-expansion-item expand-separator label="Projects">
            <ProjectLink
              v-for="project in projects"
              :key="project.name"
              :active="project.id === selectedProject.id"
              v-bind="project"
              @click="selectedProject = project"
            />
            <div v-if="selectedProject">
              <q-btn
                v-if="!expanded"
                flat
                no-caps
                icon="add"
                label="Add new project"
                @click="expanded = true"
              />
              <CreateProjectCard
                v-if="expanded"
                @projectCreated="expanded = false"
              >
                <q-btn
                  flat
                  label="Cancel"
                  @click="expanded = false"
                  v-close-popup
                />
              </CreateProjectCard>
            </div>
            <div v-else>
              <!-- todo align -->
              <q-item-label>Wow, so empty</q-item-label>
            </div>
          </q-expansion-item>
        </div>

        <q-item-label header> Essential Links </q-item-label>

        <EssentialLink
          v-for="link in essentialLinks"
          :key="link.title"
          v-bind="link"
        />
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import EssentialLink, {
  EssentialLinkProps,
} from 'components/EssentialLink.vue';
import ProjectLink from 'components/ProjectLink.vue';
import CreateProjectCard from 'components/CreateProjectCard.vue';
import { useUserStore } from 'stores/user-store';
import { useProjectStore } from 'stores/project-store';
import { storeToRefs } from 'pinia';

const userStore = useUserStore();
const { currentUser } = storeToRefs(userStore);
const projectStore = useProjectStore();
const { projects, selectedProject } = storeToRefs(projectStore);

// todo: select (selectedProject) item, kinda: :selected="project.id === selectedProject.id"

const expanded = ref(false);

const essentialLinks: EssentialLinkProps[] = [
  {
    title: 'Docs',
    caption: 'docs.deffun.io',
    icon: 'school',
    link: 'https://docs.deffun.io/',
  },
  {
    title: 'Github',
    caption: 'github.com/deffunproject',
    icon: 'code',
    link: 'https://github.com/deffunproject',
  }
];

const leftDrawerOpen = ref(false);

function toggleLeftDrawer() {
  leftDrawerOpen.value = !leftDrawerOpen.value;
}
</script>
