package io.deffun;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller("/api/projects")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ProjectController {
    @Get
    public String list() {
        return "[]";
    }

    @Post("{id}/deploy")
    public String deploy(HttpHeaders httpHeaders, @PathVariable Long id) {
        String apiKey = httpHeaders.get("X-DEFFUN-API-KEY");
//        return projectService.deploy(apiKey);
        return "";
    }
}
