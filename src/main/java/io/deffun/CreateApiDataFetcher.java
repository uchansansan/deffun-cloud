package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.deffun.gen.Database;
import io.deffun.gen.Framework;
import jakarta.inject.Singleton;

@Singleton
public class CreateApiDataFetcher implements DataFetcher<ProjectData> {
    private final ProjectService projectService;

    public CreateApiDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public ProjectData get(DataFetchingEnvironment environment) throws Exception {
        CreateApiData data = new CreateApiData();
        data.setProjectId(environment.getArgument("projectId"));
        data.setName(environment.getArgument("name"));
        data.setSchema(environment.getArgument("schema"));
        data.setBasePackage(environment.getArgument("basePackage"));
        data.setFramework(environment.getArgumentOrDefault("framework", Framework.MICRONAUT));
        data.setDatabase(environment.getArgumentOrDefault("database", Database.MARIADB));
        return projectService.createApi(data);
    }
}
