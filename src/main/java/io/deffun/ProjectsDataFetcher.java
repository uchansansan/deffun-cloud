package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

@Singleton
public class ProjectsDataFetcher implements DataFetcher<Iterable<ProjectData>> {
    private final ProjectService projectService;

    public ProjectsDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public Iterable<ProjectData> get(DataFetchingEnvironment environment) {
        return projectService.projects();
    }
}
