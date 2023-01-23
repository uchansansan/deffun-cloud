package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.deffun.gen.Database;
import io.deffun.gen.Framework;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

// same as "CreateProjectDataFetcher", but for subs
@Singleton
public class PubProjectDataFetcher implements DataFetcher<Publisher<ProjectData>> {
    private final ProjectService projectService;

    public PubProjectDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public Publisher<ProjectData> get(DataFetchingEnvironment environment) throws Exception {
        String schema = environment.getArgument("schema");
        String name = environment.getArgument("name");
        String domain = environment.getArgument("basePackage");
        Framework framework = environment.getArgumentOrDefault("framework", Framework.MICRONAUT);
        Database database = environment.getArgumentOrDefault("database", Database.MARIADB);
        String username = environment.getArgument("user");
        CreateProjectData data = new CreateProjectData(schema, domain, framework, database, username);
        return projectService.pubSave(data);
    }
}
