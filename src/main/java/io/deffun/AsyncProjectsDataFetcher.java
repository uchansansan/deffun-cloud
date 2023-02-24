package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;

@Singleton
public class AsyncProjectsDataFetcher implements DataFetcher<Flux<ProjectData>> {
    private final ProjectService projectService;

    public AsyncProjectsDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public Flux<ProjectData> get(DataFetchingEnvironment environment) throws Exception {
        return projectService.testGetProjects();
    }
}
