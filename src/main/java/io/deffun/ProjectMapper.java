package io.deffun;

import io.deffun.usermgmt.UserEntity;
import jakarta.inject.Singleton;

@Singleton
public class ProjectMapper {
    public ProjectData projectEntityToProjectData(ProjectEntity entity) {
        ProjectData dataClass = new ProjectData();
        dataClass.setId(entity.getId());
        dataClass.setName(entity.getName());
        dataClass.setSchema(entity.getSchema());
        dataClass.setApiName(entity.getApiName());
        dataClass.setApiEndpointUrl(entity.getApiEndpointUrl());
        return dataClass;
    }

    public ProjectEntity projectDataToProjectEntity(ProjectData dataClass) {
        ProjectEntity entity = new ProjectEntity();
        entity.setName(dataClass.getName());
        entity.setApiName(dataClass.getApiName());
        entity.setApiEndpointUrl(dataClass.getApiEndpointUrl());
        return entity;
    }

    public ProjectEntity createProjectDataToProjectEntity(CreateProjectData createProjectData, UserEntity userEntity) {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName(createProjectData.getName());
        projectEntity.setUser(userEntity);
        return projectEntity;
    }
}
