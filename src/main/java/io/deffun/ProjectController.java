package io.deffun;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller("/api/projects")
@Secured(SecurityRule.IS_ANONYMOUS)
public class ProjectController {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);

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

    @Error(exception = NotEnoughBalanceException.class)
    public HttpResponse<String> onCreateFailed(/*HttpRequest request,*/ NotEnoughBalanceException ex) {
        return HttpResponse.status(HttpStatus.PAYMENT_REQUIRED, ex.getMessage());
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
        LOG.info("generate and deploy serialized data {}", createApiData);
        createApiData.setProjectId(id);
        return projectService.generateAndDeploy(createApiData);
    }

    @Post("{id}/setenv")
    public ProjectData setEnvVar(SetEnvVarData setEnvVarData, @PathVariable("id") Long id) {
        return projectService.setEnvVar(id, setEnvVarData);
    }

    @Post("{id}/add_oauth")
    public ProjectData addOAuth(OAuthProviderInput oAuthProviderInput, @PathVariable("id") Long id) {
        LOG.info("add oauth for provider {}", oAuthProviderInput.getProvider());
        return projectService.addOAuthProvider(id, oAuthProviderInput);
    }

    @Post("{id}/deploy_api")
    public ProjectData deploy(@PathVariable("id") Long id) {
//        (io.micronaut.http.HttpHeaders httpHeaders)
        //https://www.javacodemonk.com/generating-cryptographically-strong-key-secret-in-java-82749205
        //https://blog.mergify.com/api-keys-best-practice/
        //https://stackoverflow.com/questions/14412132/whats-the-best-approach-for-generating-a-new-api-key
//        String apiKey = httpHeaders.get("X-DEFFUN-API-KEY");
        DeployApiData data = new DeployApiData(); // move to method params if other then id is needed
        data.setProjectId(id);
        return projectService.deployApi(data);
    }

    @Delete("{id}")
    public void delete(@PathVariable("id") Long id) {
        projectService.deleteProject(id);
    }
}
