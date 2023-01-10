package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.deffun.gen.Database;
import io.deffun.gen.Framework;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CreateProjectDataFetcher implements DataFetcher<ProjectData> {
    private final ProjectService projectService;

    public CreateProjectDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public ProjectData get(DataFetchingEnvironment environment) throws Exception {
        Logger LOGGER = LoggerFactory.getLogger(CreateProjectDataFetcher.class);
        LOGGER.info(">>> hello");

        String schema = environment.getArgument("schema");
        String name = environment.getArgument("name");
        String domain = environment.getArgument("domain");
        Framework framework = environment.getArgumentOrDefault("framework", Framework.MICRONAUT);
        Database database = environment.getArgumentOrDefault("database", Database.MARIADB);
        String username = environment.getArgument("user");
        CreateProjectData data = new CreateProjectData(schema, name, domain, framework, database, username);
        return projectService.save(data);
    }
}
