export interface ProjectData {
  id: bigint;
  name: string;
  apiName?: string;
  apiEndpointUrl?: string;
  schema?: string;
  deploying: boolean;
  test: boolean;
}

export interface CreateProjectData {
  name: string;
}

export interface CreateApiData {
  schema: string;
  database: string;
}
