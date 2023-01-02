package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.deffun.gen.Database;
import io.deffun.gen.Framework;
import jakarta.inject.Singleton;

@Singleton
public class CreateProjectDataFetcher implements DataFetcher<ProjectData> {
    private final ProjectService projectService;

    public CreateProjectDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public ProjectData get(DataFetchingEnvironment environment) throws Exception {
        String schema = environment.getArgument("schema");
        String name = environment.getArgument("name");
        String domain = environment.getArgument("domain");
        Framework framework = environment.getArgument("framework");
        Database database = environment.getArgument("database");
        String username = environment.getArgument("user");
        CreateProjectData data = new CreateProjectData(schema, name, domain, framework, database, username);
        return projectService.save(data);
    }
}
