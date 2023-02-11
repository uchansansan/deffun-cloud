<template>
  <q-page class="row full-width justify-evenly">
    <div v-if="currentUser" class="full-width">
      <div v-if="selectedProject">
        <q-expansion-item
          expand-separator
          default-opened
          icon="code"
          :label="selectedProject.name + ' Schema' + (selectedProject.test ? ' (note: test projects have resource usage limits) ' : '')"
          caption="GraphQL Editor"
          class="full-width"
        >
          <MonacoEditor
            theme="vs-dark"
            :options="opts"
            language="graphql"
            :height="400"
            @change="editorChange"
            v-model:value="selectedProject.schema"
          />
        </q-expansion-item>
        <div class="row justify-between">
          <q-btn
            color="primary"
            label="Save schema"
            @click="saveSchema(newSchema)"
            no-caps
          />
          <div v-if="selectedProject.schema">
            <q-btn
              color="primary"
              label="Deploy API"
              @click="genDeployApi(newSchema)"
              :icon-right="mdiPlayOutline"
              :disable="selectedProject.deploying"
              :loading="selectedProject.deploying"
              no-caps
            >
              <template v-slot:loading>
                Deploying
                <q-spinner-ios
                  color="secondary"
                />
              </template>
            </q-btn>
          </div>
        </div>
        <div v-if="selectedProject.apiEndpointUrl">
          <q-card>
            <q-card-section>
              <div class="text">
                Test your GraphQL API with Graph<em>i</em>QL
                <a class="text-white" :href="selectedProject.apiEndpointUrl + '/graphiql'" target="_blank">
                  {{ selectedProject.apiEndpointUrl }}/graphiql
                </a>
              </div>
              <div class="text">
                GraphQL API itself is available here
                <a class="text-white" :href="selectedProject.apiEndpointUrl + '/graphql'" target="_blank">
                  {{ selectedProject.apiEndpointUrl }}/graphql
                </a>
              </div>
            </q-card-section>
          </q-card>
        </div>
      </div>
      <div v-else class="items-center">
        <CreateProjectCard />
      </div>
    </div>
    <div v-else class="items-center">
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
import { mdiGoogle, mdiPlayOutline } from '@quasar/extras/mdi-v6';
import MonacoEditor from 'monaco-editor-vue3';
import CreateProjectCard from 'components/CreateProjectCard.vue';
import {editor} from 'monaco-editor';
import IStandaloneCodeEditor = editor.IStandaloneCodeEditor;

const userStore = useUserStore();
const { currentUser } = storeToRefs(userStore);
const { fetchUser } = userStore;

const projectStore = useProjectStore();
const { selectedProject } = storeToRefs(projectStore);
const { fetchProjects, refetchSelectedProject, saveSchema, genDeployApi } = projectStore;

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
const newSchema = ref(defaultSchema);
const opts = ref({
  language: 'graphql',
  formatOnPaste: true,
});

//function editorDid(editor: IStandaloneCodeEditor) {
//  editor.updateOptions();
//}

function editorChange(value, event) {
  console.log("... " + value);
  newSchema.value = value;
}

onMounted(async () => {
  await fetchUser();
  if (currentUser.value) {
    await fetchProjects();
    if (selectedProject.value) {
      if (selectedProject.value.schema) {
        newSchema.value = selectedProject.value.schema;
//        opts.value = {
//          value: selectedProject.value.schema,
//          language: 'graphql',
//          formatOnPaste: true,
//        };
      }
//      deploying.value = selectedProject.value.deploying;
      const pollInterval = setInterval(async () => {
        await refetchSelectedProject();
      }, 30000);
      //      setTimeout(() => { clearInterval(pollInterval) }, 36000000);
    }
  }
});
</script>
