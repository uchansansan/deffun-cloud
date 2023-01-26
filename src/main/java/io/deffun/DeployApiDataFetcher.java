package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

@Singleton
public class DeployApiDataFetcher implements DataFetcher<Publisher<ProjectData>> {
    private final ProjectService projectService;

    public DeployApiDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public Publisher<ProjectData> get(DataFetchingEnvironment environment) throws Exception {
        DeployApiData data = new DeployApiData();
        data.setProjectId(environment.getArgument("projectId"));
        return projectService.deployApiAsync(data);
    }
}
