<template>
  <q-page class="row items-center justify-evenly">
    <div v-if="currentUser">
      <div v-if="selectedProject">
        <q-expansion-item
          expand-separator
          popup
          default-opened
          icon="code"
          :label="selectedProject.name + ' Schema'"
          caption="GraphQL Editor"
          style="width: 850px"
        >
          <MonacoEditor
            theme="vs"
            :options="opts"
            language="graphql"
            :width="800"
            :height="400"
            v-model:value="schema"
          />
        </q-expansion-item>
        <div class="row justify-between">
            <q-btn
              color="primary"
              label="Save schema"
              @click="saveSchema(schema)"
              no-caps
            />
          <div v-if="selectedProject.schema">
            <q-btn
              color="primary"
              label="Deploy API"
              @click="genDeployApi"
              no-caps
            />
          </div>
        </div>
        <div v-if="deploying">
          Deployment in progress
          <q-spinner
            color="primary"
            size="3em"
          />
        </div>
        <div v-if="selectedProject.apiEndpointUrl">
          <q-card>
            <q-card-section>
              <div class="text">
                You can test your GraphQL API by visiting
                <a :href="selectedProject.apiEndpointUrl + '/graphiql'"
                  target="_blank">{{ selectedProject.apiEndpointUrl }}/graphiql</a
                >
              </div>
            </q-card-section>
          </q-card>
        </div>
      </div>
      <div v-else>
        <CreateProjectCard />
      </div>
    </div>
    <div v-else>
      <q-card>
        <q-card-section>
          <div class="text-h6">Sign in</div>
          <div class="text-subtitle2">
            Use one of the following options to sign in
          </div>
        </q-card-section>
        <q-card-actions>
          <q-btn
            no-caps
            href="/oauth/login/google"
            color="primary"
            :icon="mdiGoogle"
            label="Google"
          />
        </q-card-actions>
      </q-card>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useUserStore } from 'stores/user-store';
import { useProjectStore } from 'stores/project-store';
import { storeToRefs } from 'pinia';
import { mdiGoogle } from '@quasar/extras/mdi-v6';
import MonacoEditor from 'monaco-editor-vue3';
import CreateProjectCard from 'components/CreateProjectCard.vue';
import quasar from 'quasar';

const userStore = useUserStore();
const { currentUser } = storeToRefs(userStore);
const { fetchUser } = userStore;

const projectStore = useProjectStore();
const { selectedProject } = storeToRefs(projectStore);
const { fetchProjects, saveSchema, genDeployApi } = projectStore;

const defaultSchema = `type MyType {
  id: ID!
  someRequiredField: String!
  someOptionalField: String
}
type Query {
  getById(id: ID!): MyType!
}
`;
// TODO load data from selectedProject if exist
const schema = ref(defaultSchema);
const opts = ref({
  value: defaultSchema,
  language: 'graphql',
  formatOnPaste: true,
});

const deploying = ref(false);

onMounted(async () => {
  await fetchUser();
  if (currentUser.value) {
    await fetchProjects();
    if (selectedProject.value) {
      if (selectedProject.value.schema) {
        schema.value = selectedProject.value.schema;
        opts.value = {
          value: selectedProject.value.schema,
          language: 'graphql',
          formatOnPaste: true,
        };
      }
      deploying.value = selectedProject.value.deploying;
    }
  }
});
</script>
