import { defineStore } from 'pinia';
import { api } from 'boot/axios';
import { ProjectData, CreateProjectData, CreateApiData } from './ProjectData';

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
    async genDeployApi() {
      if (!this.selectedProject) {
        return;
      }
      const data: CreateApiData = {};
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
  },
});
