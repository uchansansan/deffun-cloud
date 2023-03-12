package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

import java.util.LinkedHashMap;
import java.util.Map;

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

    @SuppressWarnings("rawtypes")
    private CreateProjectData ddd(Map<String, Object> input) {
        CreateProjectData data = new CreateProjectData();
        Object field = input.get("fieldName");
        if (field instanceof LinkedHashMap m) {
            m.get("");
            Long.valueOf((String) input.get("asdasd"));
        }
        return data;
    }
}
