package io.deffun;

import io.deffun.deployment.GitService;
import io.deffun.doh.DatabasePlugin;
import io.deffun.doh.Dokku;
import io.deffun.doh.Util;
import io.deffun.gen.Database;
import io.deffun.gen.Deffun;
import io.deffun.usermgmt.UserEntity;
import io.deffun.usermgmt.UserRepository;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.utils.SecurityService;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import me.atrox.haikunator.Haikunator;
import org.apache.commons.io.FileUtils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Singleton
public class ProjectService {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    private final SecurityService securityService;

    private final ProjectMapper projectMapper;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final Haikunator haikunator;

    private final Dokku dokku;

    private final GitService gitService;

    private final ExecutorService deploymentExecutor;

    public ProjectService(SecurityService securityService, ProjectMapper projectMapper,
                          ProjectRepository projectRepository,
                          UserRepository userRepository,
                          Haikunator haikunator, Dokku dokku,
                          GitService gitService,
                          @Named("deployment") ExecutorService deploymentExecutor) {
        this.securityService = securityService;
        this.projectMapper = projectMapper;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.haikunator = haikunator;
        this.dokku = dokku;
        this.gitService = gitService;
        this.deploymentExecutor = deploymentExecutor;
    }

    public List<ProjectData> projects() {
        Iterable<ProjectEntity> entities = projectRepository.findAll();
        List<ProjectData> objects = new ArrayList<>();
        for (var entity : entities) {
            objects.add(projectMapper.projectEntityToProjectData(entity));
        }
        return objects;
    }

    public List<ProjectData> userProjects() {
        UserEntity userEntity = currentUser();
        Iterable<ProjectEntity> entities = projectRepository.findAllByUserId(userEntity.getId());
        List<ProjectData> objects = new ArrayList<>();
        for (ProjectEntity entity : entities) {
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
    public ProjectData createProject(CreateProjectData createProjectData) {
        UserEntity userEntity = currentUser();
        ProjectEntity projectEntity = projectMapper.createProjectDataToProjectEntity(createProjectData, userEntity);
        ProjectEntity saved = projectRepository.save(projectEntity);
        return projectMapper.projectEntityToProjectData(saved);
    }

    @Transactional
    public ProjectData createApi(CreateApiData data) {
        LOG.info("create api data {}", data);
        ///// begin code customization
        String appName;
        if (data.getName() != null) {
            appName = data.getName();
        } else {
            appName = haikunator.haikunate();
        }
        ProjectEntity projectEntity = projectRepository.findById(data.getProjectId()).orElseThrow();
        projectEntity.setApiName(appName);
        projectEntity.setSchema(data.getSchema());
        Path projects = Paths.get(System.getProperty("user.home"), "projects");
        Deffun.Parameters parameters = Deffun.parameters()
                .appName(appName)
                .basePackage("app.deffun")
                .schemaContent(data.getSchema())
                .database(data.getDatabase())
                .output(projects)
                .get();
        Path path = Deffun.generateProject(parameters);
        LOG.info("app name {} and path {}", appName, path);
        ///// end code customization

        ProjectEntity saved = projectRepository.save(projectEntity);
        return projectMapper.projectEntityToProjectData(saved);
    }

    @Transactional
    public ProjectData update(CreateApiData data) {
        ProjectEntity projectEntity = projectRepository.findById(data.getProjectId()).orElseThrow();
        projectEntity.setSchema(data.getSchema());
        ProjectEntity saved = projectRepository.update(projectEntity);
        LOG.info(">>> schema {}", saved.getSchema());
        ProjectData projectData = projectMapper.projectEntityToProjectData(saved);
        LOG.info(">>> and schema {}", projectData.getSchema());
        return projectData;
    }

    @Transactional
    public Publisher<ProjectData> deployApiAsync(DeployApiData data) {
        ///// begin code customization
        ProjectEntity projectEntity = projectRepository.findById(data.getProjectId()).orElseThrow();
        String appName = projectEntity.getApiName();
        Database database = projectEntity.getDatabase();
        setupDokkuApp(appName, database);
        Path projectsPath = Paths.get(System.getProperty("user.home"), "projects");
        Path path = projectsPath.resolve(appName);
        gitService.initRepository(path, appName);
        return Flux.create(emitter -> {
            gitService.pushRepository(path);
            projectEntity.setApiEndpointUrl("%s.deffun.app".formatted(appName));
            ///// end code customization

            ProjectEntity saved = projectRepository.save(projectEntity);
            ProjectData projectData = projectMapper.projectEntityToProjectData(saved);
            emitter.complete();
        });
    }

    @Deprecated // ???
    @Transactional
    public ProjectData deployApi(DeployApiData data) {
        ///// begin code customization
        ProjectEntity projectEntity = projectRepository.findById(data.getProjectId()).orElseThrow();
        String appName = projectEntity.getApiName();
        Database database = projectEntity.getDatabase();
        setupDokkuApp(appName, database);
        Path projectsPath = Paths.get(System.getProperty("user.home"), "projects");
        Path path = projectsPath.resolve(appName);
        gitService.initRepository(path, appName);
        gitService.pushRepository(path);
        projectEntity.setApiEndpointUrl("%s.deffun.app".formatted(appName));
        ///// end code customization

        ProjectEntity saved = projectRepository.save(projectEntity);
        return projectMapper.projectEntityToProjectData(saved);
    }

    public ProjectData generateAndDeploy(CreateApiData data) {
        ProjectEntity projectEntity = projectRepository.findById(data.getProjectId()).orElseThrow();
        if (projectEntity.isDeploying()) {
            return projectMapper.projectEntityToProjectData(projectEntity);
        }

        String appName = projectEntity.getApiName() != null ? projectEntity.getApiName() : haikunator.haikunate();
        Database database = projectEntity.getDatabase() != null ? projectEntity.getDatabase() : Database.MARIADB;
        int newVer = projectEntity.getVersion() + 1;
        projectRepository.update(data.getProjectId(), true, appName, database, newVer);

        deploymentExecutor.submit(() -> {
            try {
                Path projects = Paths.get(System.getProperty("user.home"), "projects");
                if (projectEntity.getApiEndpointUrl() != null) {
                    LOG.info(">>> delete app");
                    deleteDokkuApp(appName, database);
                    LOG.info(">>> app deleted");
                    try {
                        FileUtils.deleteDirectory(projects.resolve(appName).toFile());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                Deffun.Parameters parameters = Deffun.parameters()
                        .appName(appName)
                        .basePackage("app.deffun")
                        .schemaContent(projectEntity.getSchema())
                        .database(database)
                        .output(projects)
                        .get();
                Path path = Deffun.generateProject(parameters);
                LOG.info("app name {} and path {}", appName, path);

                setupDokkuApp(appName, database);
                gitService.initRepository(path, appName);
                gitService.pushRepository(path);
                projectRepository.update(data.getProjectId(), false, "https://%s.deffun.app".formatted(appName));
            } catch (Exception e) {
                LOG.info("some problem occurred", e);
                projectRepository.update(data.getProjectId(), false, null);
            }
        });

        ProjectEntity fetched = projectRepository.findById(projectEntity.getId()).orElseThrow();
        return projectMapper.projectEntityToProjectData(fetched);
    }

    private void setupDokkuApp(String appName, Database database) {
        dokku.apps().create(appName);
        try {
            dokku.buildpacks().add(appName, new URL("https://github.com/heroku/heroku-buildpack-java.git"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        DatabasePlugin databasePlugin = getDatabasePlugin(database);
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
        dokku.letsEncryptPlugin().setEmail(appName, currentUserEmail());
        dokku.letsEncryptPlugin().enable(appName);
    }

    private void deleteDokkuApp(String appName, Database database) {
        dokku.apps().destroyForce(appName);
        DatabasePlugin databasePlugin = getDatabasePlugin(database);
        LOG.info("delete using {}", databasePlugin);
        databasePlugin.destroyForce(appName + "_db");
    }

    private DatabasePlugin getDatabasePlugin(Database database) {
        return switch (database) {
            case MYSQL -> dokku.mySqlPlugin();
            case MARIADB -> dokku.mariaDbPlugin();
            case POSTGRES -> dokku.postgresPlugin();
        };
    }

    //region SECURITY
    private UserEntity currentUser() {
        String email = currentUserEmail();
        return userRepository.findByEmail(email).orElseThrow(); // or else 401 Unauthorized
    }

    private String currentUserEmail() {
        Authentication authentication = securityService.getAuthentication().orElseThrow();
        return (String) authentication.getAttributes().get("email");
    }
    //endregion
}
