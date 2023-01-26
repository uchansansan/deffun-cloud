package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

// same as "DeployApiDataFetcher", but for subs
@Singleton
public class DeployApiAsyncDataFetcher implements DataFetcher<ProjectData> {
    private final ProjectService projectService;

    public DeployApiAsyncDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public ProjectData get(DataFetchingEnvironment environment) throws Exception {
        DeployApiData data = new DeployApiData();
        data.setProjectId(environment.getArgument("projectId"));
        return projectService.deployApi(data);
    }
}
