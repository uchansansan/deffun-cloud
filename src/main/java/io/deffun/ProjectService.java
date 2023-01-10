package io.deffun;

import io.deffun.deployment.DokkuService;
import io.deffun.deployment.GitService;
import io.deffun.gen.Deffun;
import io.deffun.usermgmt.UserEntity;
import io.deffun.usermgmt.UserRepository;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import javax.transaction.Transactional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class ProjectService {
    private final ProjectMapper projectMapper;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final DokkuService dokkuService;

    private final GitService gitService;

    public ProjectService(ProjectMapper projectMapper,
                          ProjectRepository projectRepository,
                          UserRepository userRepository,
                          DokkuService dokkuService,
                          GitService gitService) {
        this.projectMapper = projectMapper;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.dokkuService = dokkuService;
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

    @Transactional
    public ProjectData save(CreateProjectData data) {
        ///// begin code customization
        Path projects = Paths.get(System.getProperty("user.home"), "projects");
        Deffun.Parameters parameters = Deffun.parameters()
                .appName(data.name())
                .basePackage("app.deffun")
                .schemaContent(data.schema())
                .output(projects)
                .get();
        Path path = Deffun.generateProject(parameters);
        dokkuService.createRemoteApp(data.name(), data.database());
        gitService.initRepository(path, data.name());
        gitService.pushRepository(path);
        ///// end code customization

        UserEntity user = userRepository.findByUsername(data.username()).orElseThrow();
        ProjectEntity project = projectMapper.createProjectDataToProjectEntity(data);
        project.setUser(user);
        ProjectEntity saved = projectRepository.save(project);

        return projectMapper.projectEntityToProjectData(saved);
    }

    @Transactional
    public Publisher<ProjectData> pubSave(CreateProjectData data) {
        ///// begin code customization
        Path projects = Paths.get(System.getProperty("user.home"), "projects");
        Deffun.Parameters parameters = Deffun.parameters()
                .appName(data.name())
                .basePackage("app.deffun")
                .schemaContent(data.schema())
                .output(projects)
                .get();
        Path path = Deffun.generateProject(parameters);
//        dokkuService.createRemoteApp(data.name(), data.database());
        gitService.initRepository(path, data.name());
        return Flux.create(emitter -> {
//            gitService.pushRepository(path);
            ///// end code customization

//            UserEntity user = userRepository.findByUsername(data.username()).orElseThrow();
//            ProjectEntity project = projectMapper.createProjectDataToProjectEntity(data);
//            project.setUser(user);
//            ProjectEntity saved = projectRepository.save(project);
//
//            ProjectData projectData = projectMapper.projectEntityToProjectData(saved);
            try {
                TimeUnit.SECONDS.sleep(45);
                emitter.complete();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                emitter.error(e);
            }
        });
    }
}
