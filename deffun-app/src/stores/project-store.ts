import { defineStore } from 'pinia';
import { api } from 'boot/axios';
import {
  ProjectData,
  CreateProjectData,
  CreateApiData,
  SetEnvData,
  AddOAuthData,
} from './ProjectData';
import { useMutation, useQuery } from '@urql/vue';
import { graphql } from '../gql';
import { createClient, provideClient } from '@urql/vue';

export const useProjectStore = defineStore('project', {
  state: () => ({
    projects: [] as ProjectData[],
    selectedProject: null as ProjectData | null,
  }),
  getters: {
    getProjects: (state) => state.projects,
    getSelectedProject: (state) => state.selectedProject,
  },
  actions: {
    async fetchProjects() {
      this.projects = await api
        .get<ProjectData[]>('/projects')
        .then((response) => {
          return response.data;
        })
        .catch((err) => {
          console.log(err);
          return [];
        });
      if (this.projects.length > 0) {
        this.selectedProject = this.projects[this.projects.length - 1];
      }
    },
    async refetchSelectedProject() {
      if (!this.selectedProject) {
        return;
      }
      this.selectedProject = await api
        .get<ProjectData>('/projects/' + this.selectedProject.id)
        .then((response) => {
          return response.data;
        });
    },
    async createProject(projectName: string) {
      const data: CreateProjectData = {
        name: projectName,
      };
      this.selectedProject = await api
        .post<ProjectData>('/projects', data)
        .then((response) => {
          return response.data;
        })
        .catch((err) => {
          console.log(err);
          return null;
        });
      //      this.projects.push(this.selectedProject);
      await this.fetchProjects();
    },
    async createApi(schema: string) {
      if (!this.selectedProject) {
        return;
      }
      const data: CreateApiData = {
        schema: schema,
      };
      this.selectedProject = await api
        .post<ProjectData>(
          '/projects/' + this.selectedProject.id + '/create_api',
          data
        )
        .then((response) => {
          return response.data;
        })
        .catch((err) => {
          console.log(err);
          return null;
        });
    },
    async saveSchema(schema: string) {
      if (!this.selectedProject) {
        return;
      }
      const data: CreateApiData = {
        schema: schema,
      };
      this.selectedProject = await api
        .post<ProjectData>(
          '/projects/' + this.selectedProject.id + '/save_schema',
          data
        )
        .then((response) => {
          return response.data;
        })
        .catch((err) => {
          console.log(err);
          return null;
        });
    },
    async genDeployApi(schema: string, database: string) {
      // todo database as enum
      if (!this.selectedProject) {
        return;
      }
      const data: CreateApiData = {
        schema: schema,
        database: database,
      };
      this.selectedProject = await api
        .post<ProjectData>(
          '/projects/' + this.selectedProject.id + '/gen_deploy_api',
          data
        )
        .then((response) => {
          return response.data;
        })
        .catch((err) => {
          console.log(err);
          return null;
        });
    },
    async deployApi() {
      this.selectedProject = await api
        .post<ProjectData>(
          '/projects/' + this.selectedProject.id + '/deploy_api'
        )
        .then((response) => {
          return response.data;
        })
        .catch((err) => {
          console.log(err);
          return null;
        });
    },
    async setEnvVar(key: string, value: string) {
      const data: SetEnvData = {
        key: key,
        value: value,
      };
      await api
        .post<ProjectData>(
          '/projects/' + this.selectedProject.id + '/setenv',
          data
        )
        .then((response) => {
          return response.data;
        })
        .catch((err) => {
          console.log(err);
          return null;
        });
    },
    async addOAuth(provider: string, clientId: string, clientSecret: string) {
      const data: AddOAuthData = {
        provider: provider,
        clientId: clientId,
        clientSecret: clientSecret,
      };
      await api
        .post<ProjectData>(
          '/projects/' + this.selectedProject.id + '/add_oauth',
          data
        )
        .then((response) => {
          return response.data;
        })
        .catch((err) => {
          console.log(err);
          return null;
        });
    },
    // async setEnvVar(key: string, value: string) {
    //   const client = createClient({
    //     url: 'http://localhost:8080/graphql',
    //   });

    //   provideClient(client);
    //   const setEnvVar = useMutation(`
    //       mutation ($projectId: ID!, $key: String!, $value: String!) {
    //         setEnvVar(projectId: $projectId, key: $key, value: $value) {
    //           id
    //         }
    //       }
    //     `);
    //   const variables = {
    //     projectId: this.selectedProject.id,
    //     key: key,
    //     value: value,
    //   };
    //   setEnvVar.executeMutation(variables).then((result) => {
    //     console.log(result);
    //   });
    //   // `films` is typed!
    //   //      const films = computed(() => data.value?.allFilms?.edges?.map(e => e?.node!))
    // },
  },
});
