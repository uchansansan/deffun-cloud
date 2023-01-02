package io.deffun;

import jakarta.inject.Singleton;

@Singleton
public class ProjectMapper {
    public ProjectData projectEntityToProjectData(ProjectEntity entity) {
        ProjectData dataClass = new ProjectData();
        dataClass.setName(entity.getName());
        dataClass.setDomain(entity.getDomain());
        dataClass.setSourceCodeUrl(entity.getSourceCodeUrl());
        dataClass.setEndpointUrl(entity.getEndpointUrl());
        return dataClass;
    }

    public ProjectEntity projectDataToProjectEntity(ProjectData dataClass) {
        ProjectEntity entity = new ProjectEntity();
        entity.setName(dataClass.getName());
        entity.setDomain(dataClass.getDomain());
        entity.setSourceCodeUrl(dataClass.getSourceCodeUrl());
        entity.setEndpointUrl(dataClass.getEndpointUrl());
        return entity;
    }

    public ProjectEntity createProjectDataToProjectEntity(CreateProjectData createProjectData) {
        ProjectEntity entity = new ProjectEntity();
        entity.setName(createProjectData.name());
        entity.setDomain(createProjectData.domain());
        return entity;
    }
}
