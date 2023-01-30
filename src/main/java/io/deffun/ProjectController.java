package io.deffun;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

import java.util.List;

@Controller("/api/projects")
@Secured(SecurityRule.IS_ANONYMOUS)
public class ProjectController {
    @Inject
    private ProjectService projectService;

    @Get("/list")
    public List<ProjectData> projects() {
        return projectService.projects();
    }

    @Get
    public List<ProjectData> userProjects() {
        return projectService.userProjects();
    }

    @Get("/{id}")
    public ProjectData projectById(Long id) {
        return projectService.project(id);
    }

    @Post
    public ProjectData create(CreateProjectData createProjectData) {
        return projectService.createProject(createProjectData);
    }

    @Post("{id}/create_api")
    public ProjectData createApi(CreateApiData createApiData, @PathVariable("id") Long id) {
        createApiData.setProjectId(id);
        return projectService.createApi(createApiData);
    }
    @Post("{id}/save_schema")
    public ProjectData saveSchema(CreateApiData createApiData, @PathVariable("id") Long id) {
        createApiData.setProjectId(id);
        return projectService.update(createApiData);
    }

    @Post("{id}/gen_deploy_api")
    public ProjectData genDeployApi(CreateApiData createApiData, @PathVariable("id") Long id) {
        createApiData.setProjectId(id);
        return projectService.generateAndDeploy(createApiData);
    }

    @Post("{id}/deploy_api")
    public ProjectData deploy(@PathVariable("id") Long id) {
//        (io.micronaut.http.HttpHeaders httpHeaders)
//        String apiKey = httpHeaders.get("X-DEFFUN-API-KEY");
        DeployApiData data = new DeployApiData(); // move to method params if other then id is needed
        data.setProjectId(id);
        return projectService.deployApi(data);
    }
}
