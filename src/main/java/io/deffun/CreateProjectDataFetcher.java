package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

@Singleton
public class CreateProjectDataFetcher implements DataFetcher<ProjectData> {
    private final ProjectService projectService;

    public CreateProjectDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public ProjectData get(DataFetchingEnvironment environment) throws Exception {
        CreateProjectData createProjectData = new CreateProjectData();
        createProjectData.setName(environment.getArgument("name"));
        return projectService.createProject(createProjectData);
    }
}
