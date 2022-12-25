package io.deffun;

import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ProjectService {
    private final ProjectMapper projectMapper;

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectMapper projectMapper, ProjectRepository projectRepository) {
        this.projectMapper = projectMapper;
        this.projectRepository = projectRepository;
    }

    public List<ProjectData> projects() {
        Iterable<ProjectEntity> entities = projectRepository.findAll();
        List<ProjectData> objects = new ArrayList<>();
        for (var entity : entities) {
            objects.add(projectMapper.projectEntityToProjectData(entity));
        }
        return objects;
    }
}
