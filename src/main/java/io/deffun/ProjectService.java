package io.deffun;

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

    public ProjectService(ProjectMapper projectMapper, ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectMapper = projectMapper;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
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

        UserEntity user = userRepository.findByUsername(data.username()).orElseThrow();
        ProjectEntity project = projectMapper.createProjectDataToProjectEntity(data);
        project.setUser(user);
        ProjectEntity saved = projectRepository.save(project);

        return projectMapper.projectEntityToProjectData(saved);
    }
}
