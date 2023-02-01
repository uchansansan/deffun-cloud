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
        <div>v0.0.1-beta-test-for-friends</div>

        <div v-if="currentUser">
          <q-separator vertical />
          <q-btn flat no-caps href="/logout" label="Sign out" />
        </div>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <q-list>
        <div v-if="currentUser">
          <q-expansion-item expand-separator popup label="Projects">
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
    caption: 'work in progress',
    icon: 'school',
    link: 'mailto:artem@deffun.io',
  },
  {
    title: 'Github',
    caption: 'github.com/deffunproject',
    icon: 'code',
    link: 'https://github.com/deffunproject',
  },
  {
    title: 'Twitter',
    caption: '@deffunproject',
    icon: 'rss_feed',
    link: 'https://twitter.com/deffunproject',
  },
  {
    title: 'Telegram Channel',
    caption: 't.me/deffunproject',
    icon: 'chat',
    link: 'https://t.me/deffunproject',
  },
];

const leftDrawerOpen = ref(false);

function toggleLeftDrawer() {
  leftDrawerOpen.value = !leftDrawerOpen.value;
}
</script>
