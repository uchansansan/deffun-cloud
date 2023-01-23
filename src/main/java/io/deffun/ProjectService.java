package io.deffun;

import io.deffun.deployment.GitService;
import io.deffun.doh.DatabasePlugin;
import io.deffun.doh.Dokku;
import io.deffun.doh.Util;
import io.deffun.gen.Database;
import io.deffun.gen.Deffun;
import io.deffun.usermgmt.UserEntity;
import io.deffun.usermgmt.UserRepository;
import jakarta.inject.Singleton;
import me.atrox.haikunator.Haikunator;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
public class ProjectService {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectMapper projectMapper;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final Haikunator haikunator;

    private final Dokku dokku;

    private final GitService gitService;

    public ProjectService(ProjectMapper projectMapper,
                          ProjectRepository projectRepository,
                          UserRepository userRepository,
                          Haikunator haikunator, Dokku dokku,
                          GitService gitService) {
        this.projectMapper = projectMapper;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.haikunator = haikunator;
        this.dokku = dokku;
        this.gitService = gitService;
    }

    public List<ProjectData> projects() {
        Iterable<ProjectEntity> entities = projectRepository.findAll();
        List<ProjectData> objects = new ArrayList<>();
        for (var entity : entities) {
            objects.add(projectMapper.projectEntityToProjectData(entity));
        }
        return objects;
    }

    public ProjectData project(Long id) {
        ProjectEntity entity = projectRepository.findById(id).orElseThrow();
        return projectMapper.projectEntityToProjectData(entity);
    }

    public void delete(Long id) {
        projectRepository.deleteById(id);
    }

    @Transactional
    public ProjectData save(CreateProjectData data) {
        ///// begin code customization
        String appName = haikunator.haikunate();

        LOG.info("app name {}", appName);

        ///// begin code customization
        Path projects = Paths.get(System.getProperty("user.home"), "projects");
        Deffun.Parameters parameters = Deffun.parameters()
                .appName(appName)
                .basePackage("app.deffun")
                .schemaContent(data.schema())
                .output(projects)
                .get();
        Path path = Deffun.generateProject(parameters);
        dokku.apps().create(appName);
        try {
            dokku.buildpacks().add(appName, new URL("https://github.com/heroku/heroku-buildpack-java.git"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Database database = data.database();
        DatabasePlugin databasePlugin = switch (database) {
            case MYSQL -> dokku.mySqlPlugin();
            case MARIADB -> dokku.mariaDbPlugin();
            case POSTGRES -> dokku.postgresPlugin();
        };
        String dbName = appName + "_db";
        databasePlugin.create(dbName);
        databasePlugin.link(dbName, appName);
        LOG.info(">>> linked app {} and db {}", appName, dbName);
        String databaseUrl = dokku.config().get(appName, "DATABASE_URL");
        LOG.info(">>> database URL {}", databaseUrl);
        URI jdbcUrl = Util.convertDbUrlToJdbcUrl(URI.create(databaseUrl));
        LOG.info(">>> JDBC URL {}", jdbcUrl);
        dokku.config().set(appName,
                Map.of(
                        "DATASOURCES_DEFAULT_URL", jdbcUrl.toString()
                ));
        gitService.initRepository(path, appName);
        gitService.pushRepository(path);

        ///// end code customization

//        return projectMapper.projectEntityToProjectData(saved);
        ProjectData projectData = new ProjectData();
        projectData.setName(appName);
        return projectData;
    }

    //    private val publishSubject = PublishSubject.create<Planet>()

    //    @Transactional
    public Publisher<ProjectData> pubSave(CreateProjectData data) {
        String appName = haikunator.haikunate();

        LOG.info("app name {}", appName);

        ///// begin code customization
        Path projects = Paths.get(System.getProperty("user.home"), "projects");
        Deffun.Parameters parameters = Deffun.parameters()
                .appName(appName)
                .basePackage("app.deffun")
                .schemaContent(data.schema())
                .output(projects)
                .get();
        Path path = Deffun.generateProject(parameters);
        dokku.apps().create(appName);
        try {
            dokku.buildpacks().add(appName, new URL("https://github.com/heroku/heroku-buildpack-java.git"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Database database = data.database();
        DatabasePlugin databasePlugin = switch (database) {
            case MYSQL -> dokku.mySqlPlugin();
            case MARIADB -> dokku.mariaDbPlugin();
            case POSTGRES -> dokku.postgresPlugin();
        };
        String dbName = appName + "_db";
        databasePlugin.create(dbName);
        databasePlugin.link(dbName, appName);
        LOG.info(">>> linked app {} and db {}", appName, dbName);
        String databaseUrl = dokku.config().get(appName, "DATABASE_URL");
        LOG.info(">>> database URL {}", databaseUrl);
        URI jdbcUrl = Util.convertDbUrlToJdbcUrl(URI.create(databaseUrl));
        LOG.info(">>> JDBC URL {}", jdbcUrl);
        dokku.config().set(appName,
                Map.of(
                        "DATASOURCES_DEFAULT_URL", jdbcUrl.toString()
                ));
        gitService.initRepository(path, appName);
        return Flux.create(emitter -> {
            gitService.pushRepository(path);
            ///// end code customization

//            UserEntity user = userRepository.findByUsername(data.username()).orElseThrow();
//            ProjectEntity project = projectMapper.createProjectDataToProjectEntity(data);
//            project.setUser(user);
//            ProjectEntity saved = projectRepository.save(project);
//
//            ProjectData projectData = projectMapper.projectEntityToProjectData(saved);
            emitter.complete();
        });
    }

    private void createRemoteApp(String projectName, Database database) {
        dokku.apps().create(projectName);
        DatabasePlugin databasePlugin = switch (database) {
            case MARIADB -> dokku.mariaDbPlugin();
            case MYSQL -> dokku.mySqlPlugin();
            case POSTGRES -> dokku.postgresPlugin();
        };
        String dbName = projectName + "-db";
        databasePlugin.create(dbName);
        databasePlugin.link(dbName, projectName);
    }
}
