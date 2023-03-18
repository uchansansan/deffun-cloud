package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.deffun.gen.OAuthProvider;
import jakarta.inject.Singleton;

import java.util.Map;

@Singleton
public class AddOAuthProviderDataFetcher implements DataFetcher<ProjectData> {
    private final ProjectService projectService;

    public AddOAuthProviderDataFetcher(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public ProjectData get(DataFetchingEnvironment environment) throws Exception {
        Long projectId = Long.valueOf(environment.getArgument("projectId"));
        Map<String, Object> providerInput = environment.getArgument("input");
        OAuthProviderInput oAuthProviderInput = new OAuthProviderInput();
        oAuthProviderInput.setProvider((OAuthProvider) providerInput.get("provider"));
        oAuthProviderInput.setClientId((String) providerInput.get("clientId"));
        oAuthProviderInput.setClientSecret((String) providerInput.get("clientSecret"));
        return projectService.addOAuthProvider(projectId, oAuthProviderInput);
    }
}
