package io.deffun;

import io.deffun.deployment.DokkuService;
import io.deffun.usermgmt.UserEntity;
import io.deffun.usermgmt.UserRepository;
import jakarta.inject.Singleton;

import javax.transaction.Transactional;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ProjectService {
    private final ProjectMapper projectMapper;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final DokkuService dokkuService;

    public ProjectService(ProjectMapper projectMapper,
                          ProjectRepository projectRepository,
                          UserRepository userRepository,
                          DokkuService dokkuService) {
        this.projectMapper = projectMapper;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.dokkuService = dokkuService;
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
        Path path = Paths.get("");
//        path = new DeffunCli()
//                .generateProject(); // generate project

        dokkuService.createRemoteApp(data.name());
        dokkuService.initLocalAndPush(path, data.name());

        UserEntity user = userRepository.findByUsername(data.username()).orElseThrow();
        ProjectEntity project = projectMapper.createProjectDataToProjectEntity(data);
        project.setUser(user);
        ProjectEntity saved = projectRepository.save(project);

        return projectMapper.projectEntityToProjectData(saved);
    }
}
