package io.deffun;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaMissingError;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.ResourceResolver;
import jakarta.inject.Singleton;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Factory
public class GraphQLFactory {
    @Singleton
    public GraphQL graphQL(ResourceResolver resourceResolver,
                           ProjectsDataFetcher projectsDataFetcher,
                           CreateApiDataFetcher createApiDataFetcher,
                           DeployApiDataFetcher deployApiDataFetcher,
                           DeployApiAsyncDataFetcher deployApiAsyncDataFetcher,
                           CreateProjectDataFetcher createProjectDataFetcher,
                           SetEnvVarDataFetcher setEnvVarDataFetcher,
                           AddOAuthProviderDataFetcher addOAuthProviderDataFetcher
    ) {
        SchemaParser schemaParser = new SchemaParser();
        SchemaGenerator schemaGenerator = new SchemaGenerator();

        InputStream schemaDefinition = resourceResolver
                .getResourceAsStream("classpath:schema.graphqls")
                .orElseThrow(SchemaMissingError::new);

        TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
        typeRegistry.merge(schemaParser.parse(new BufferedReader(new InputStreamReader(schemaDefinition))));

        RuntimeWiring runtimeWiring = RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring -> typeWiring.dataFetcher("projects", projectsDataFetcher))
                .type("Mutation", typeWiring -> typeWiring.dataFetcher("createProject", createProjectDataFetcher))
                .type("Mutation", typeWiring -> typeWiring.dataFetcher("createApi", createApiDataFetcher))
                .type("Mutation", typeWiring -> typeWiring.dataFetcher("deployApi", deployApiDataFetcher))
                .type("Mutation", typeWiring -> typeWiring.dataFetcher("setEnvVar", setEnvVarDataFetcher))
                .type("Mutation", typeWiring -> typeWiring.dataFetcher("addOAuthProvider", addOAuthProviderDataFetcher))
                .type("Subscription", typeWiring -> typeWiring.dataFetcher("asyncDeployApi", deployApiAsyncDataFetcher))
                .build();

        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

        return GraphQL.newGraphQL(graphQLSchema).build();
    }
}
