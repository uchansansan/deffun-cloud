package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.deffun.gen.Database;
import io.deffun.gen.Framework;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

@Singleton
public class SubCreateProjectDataFetcher implements DataFetcher<Publisher<ProjectData>> {
    private final ProjectService projectService;

    public SubCreateProjectDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public Publisher<ProjectData> get(DataFetchingEnvironment environment) throws Exception {
        String schema = environment.getArgument("schema");
        String name = environment.getArgument("name");
        String domain = environment.getArgument("domain");
        Framework framework = environment.getArgumentOrDefault("framework", Framework.MICRONAUT);
        Database database = environment.getArgumentOrDefault("database", Database.MARIADB);
        String username = environment.getArgument("user");
        CreateProjectData data = new CreateProjectData(schema, name, domain, framework, database, username);
        return projectService.pubSave(data);
    }
}
