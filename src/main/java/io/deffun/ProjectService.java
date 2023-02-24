package io.deffun;

import io.deffun.billing.BillingService;
import io.deffun.deployment.GitService;
import io.deffun.doh.DatabasePlugin;
import io.deffun.doh.Dokku;
import io.deffun.doh.Util;
import io.deffun.gen.Database;
import io.deffun.gen.Deffun;
import io.deffun.usermgmt.UserEntity;
import io.deffun.usermgmt.UserRepository;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.TaskScheduler;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.utils.SecurityService;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import me.atrox.haikunator.Haikunator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Sinks;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

@Singleton
public class ProjectService {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);
    private static final String DEFAULT_SCHEMA = """
            type MyType {
              id: ID!
              name: String
            }
            type Query {
              list: [MyType]
              getById(id: ID!): MyType
            }
            type Mutation {
              create(name: String): MyType
            }
            """;

    private Sinks.Many<ProjectData> sink = Sinks.many().multicast().onBackpressureBuffer();

//    private Flux<ProjectData> onCreateStream = Flux.create((Consumer<FluxSink<ProjectData>>) sink -> {
//        while (!sink.isCancelled()) {
//            try {
//                sink.next(projectAsyncRepository.findAll());
//            } catch (InterruptedException e) {
//                sink.error(e);
//            }
//        }
//        sink.complete();
//    }, FluxSink.OverflowStrategy.BUFFER).share();

    private final SecurityService securityService;
    private final ProjectMapper projectMapper;
    private final ProjectRepository projectRepository;
    private final ProjectAsyncRepository projectAsyncRepository;
    private final UserRepository userRepository;
    private final Haikunator haikunator;
    private final Dokku dokku;
    private final GitService gitService;
    // to run deployment in separate thread
    private final ExecutorService deploymentExecutor;

    // balance mgmt
    private final TaskScheduler taskScheduler;
    private final BillingService billingService;
    private final Long chargeDelay;

    public ProjectService(SecurityService securityService, ProjectMapper projectMapper,
                          ProjectRepository projectRepository,
                          ProjectAsyncRepository projectAsyncRepository, UserRepository userRepository,
                          Haikunator haikunator, Dokku dokku,
                          GitService gitService,
                          @Named("deployment") ExecutorService deploymentExecutor,
                          @Named(TaskExecutors.SCHEDULED) TaskScheduler taskScheduler,
                          BillingService billingService,
                          @Value("${deffun.billing.chargeDelay}") Long chargeDelay) {
        this.securityService = securityService;
        this.projectMapper = projectMapper;
        this.projectRepository = projectRepository;
        this.projectAsyncRepository = projectAsyncRepository;
        this.userRepository = userRepository;
        this.haikunator = haikunator;
        this.dokku = dokku;
        this.gitService = gitService;
        this.deploymentExecutor = deploymentExecutor;
        this.taskScheduler = taskScheduler;
        this.billingService = billingService;
        this.chargeDelay = chargeDelay;
    }

    public List<ProjectData> projects() {
        Subscriber<ProjectEntity> subscriber = new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription subscription) {

            }

            @Override
            public void onNext(ProjectEntity entity) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        };
        projectAsyncRepository.findAll()
                .subscribe(subscriber);
        Iterable<ProjectEntity> entities = projectRepository.findAll();
        List<ProjectData> objects = new ArrayList<>();
        for (var entity : entities) {
            objects.add(projectMapper.projectEntityToProjectData(entity));
        }
        return objects;
    }

    public List<ProjectData> userProjects() {
        UserEntity userEntity = currentUser();
        List<ProjectEntity> entities = projectRepository.findAllByUserId(userEntity.getId());
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

    @Transactional
    public void deleteProject(Long id) {
        // todo in one request please, kinda `findByIdAndUsersEmail`
        UserEntity userEntity = currentUser();
        ProjectEntity projectEntity = projectRepository.findById(id)
                .orElseThrow();
        if (projectEntity.isTest()) {
            deleteDokkuApp(projectEntity.getApiName(), projectEntity.getDatabase());
            projectEntity.setApiEndpointUrl(null);
            projectRepository.update(projectEntity);
            return;
        }
        Validate.isTrue(userEntity.getId().equals(projectEntity.getUser().getId())); // todo del this line
        deleteDokkuApp(projectEntity.getApiName(), projectEntity.getDatabase());
        projectRepository.deleteById(id);
    }

    @Transactional
    public void undeployApi(Long id) {
        // todo in one request please, kinda `findByIdAndUsersEmail`
        UserEntity userEntity = currentUser();
        ProjectEntity projectEntity = projectRepository.findById(id)
                .orElseThrow();
        Validate.isTrue(userEntity.getId().equals(projectEntity.getUser().getId()));
        deleteDokkuApp(projectEntity.getApiName(), projectEntity.getDatabase());
        projectEntity.setApiEndpointUrl(null);
        projectEntity.setLastCharge(null);
        projectEntity.setDeploying(false);
        projectRepository.update(projectEntity);
    }

    // TODO: v1_2 & v1_3 migrations not needed

    @Transactional
    public ProjectData createProject(CreateProjectData createProjectData) {
        UserEntity userEntity = currentUser();
        if (userEntity.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NotEnoughBalanceException("Please, top up your balance.");
        }
        ProjectEntity projectEntity = projectMapper.createProjectDataToProjectEntity(createProjectData, userEntity);
        ProjectEntity saved = projectRepository.save(projectEntity);
        return projectMapper.projectEntityToProjectData(saved);
    }

    @Transactional
    public ProjectData testCreateProject(CreateProjectData createProjectData) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        ProjectEntity entity = projectMapper.createProjectDataToProjectEntity(createProjectData, userEntity);
        ProjectEntity saved = projectRepository.save(entity);
        ProjectData projectData = projectMapper.projectEntityToProjectData(saved);
        sink.tryEmitNext(projectData);
        return projectData;
    }

    public Flux<ProjectData> testGetProjects() {
        return sink.asFlux();
    }

    @EventListener
    public void onStartup(StartupEvent startupEvent) {
        LOG.info("I'm working in startup");
        Iterable<ProjectEntity> all = projectRepository.findAll();
        for (ProjectEntity project : all) {
            if (!project.isTest() && project.getApiEndpointUrl() != null) {
                // we should recalculate balance if node was offline for some time
                Duration between = Duration.between(project.getLastCharge(), LocalDateTime.now());
                long hours = between.toHours();
                Duration duration = Duration.ofHours(1L).minusMinutes(between.toMinutesPart());
                LOG.info("Redeploy after {} hours and will be charged after {} mins -- project '{}' (ID {})", hours, duration.toMinutes(), project.getName(), project.getId());
                if (hours > 0) {
                    billingService.chargeForHours(project.getId(), hours);
                }
                taskScheduler.scheduleWithFixedDelay(duration, getDelay(), () -> billingService.updateBalance(project.getId()));
            }
        }
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
//        Path projects = Paths.get(System.getProperty("user.home"), "projects");
//        Deffun.Parameters parameters = Deffun.parameters()
//                .appName(appName)
//                .basePackage("app.deffun")
//                .schemaContent(data.getSchema())
//                .database(data.getDatabase())
//                .output(projects)
//                .get();
//        Path path = Deffun.generateProject(parameters);
//        LOG.info("app name {} and path {}", appName, path);
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
//        int newVer = projectEntity.getVersion() + 1;
        projectRepository.update(data.getProjectId(), true, appName, database);

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
                    projectRepository.update(data.getProjectId(), null);
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
                projectRepository.update(data.getProjectId(), false, "https://%s.deffun.app".formatted(appName), LocalDateTime.now());

                // task can be canceled, but we should save the reference of the returned `ScheduledFuture<?>` for this
                taskScheduler.scheduleAtFixedRate(getDelay(), getDelay(), () -> billingService.updateBalance(projectEntity.getId()));
            } catch (Exception e) {
                LOG.info("some problem occurred", e);
                projectRepository.update(data.getProjectId(), false, null, (LocalDateTime) null);
            }
        });

        ProjectEntity fetched = projectRepository.findById(projectEntity.getId()).orElseThrow();
        return projectMapper.projectEntityToProjectData(fetched);
    }

    @Transactional
    public void createTestProjectIfAbsent(Long userId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow();
        List<ProjectEntity> list = projectRepository.findAllByUserId(userId);
        LOG.info("create if absent {}", list.size());
        if (list.stream().noneMatch(ProjectEntity::isTest)) {
            LOG.info("none tests found");
            ProjectEntity entity = new ProjectEntity();
            entity.setTest(true);
            entity.setName("Test");
            entity.setSchema(DEFAULT_SCHEMA);
            entity.setUser(userEntity);
            projectRepository.save(entity);
        }
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

    private Duration getDelay() {
        return Duration.ofMinutes(chargeDelay);
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
