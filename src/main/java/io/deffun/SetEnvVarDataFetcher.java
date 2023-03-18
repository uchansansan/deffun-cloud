package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import jakarta.inject.Singleton;

@Singleton
public class SetEnvVarDataFetcher implements DataFetcher<ProjectData> {
    private final ProjectService projectService;

    public SetEnvVarDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public ProjectData get(DataFetchingEnvironment environment) throws Exception {
        Long projectId = Long.valueOf(environment.getArgument("projectId"));
        SetEnvVarData setEnvVarData = new SetEnvVarData();
        setEnvVarData.setKey(environment.getArgument("key"));
        setEnvVarData.setValue(environment.getArgument("value"));
        return projectService.setEnvVar(projectId, setEnvVarData);
    }
}
