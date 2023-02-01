package io.deffun;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.deffun.usermgmt.UserEntity;
import jakarta.inject.Singleton;
import org.dataloader.DataLoader;

import java.util.concurrent.CompletionStage;

// todo use UserData and UserService instead!!!
@Singleton
public class UserDataFetcher implements DataFetcher<CompletionStage<UserEntity>> {
    @Override
    public CompletionStage<UserEntity> get(DataFetchingEnvironment environment) throws Exception {
        ProjectEntity project = environment.getSource();
        DataLoader<Long, UserEntity> userDataLoader = environment.getDataLoader("user");
        return userDataLoader.load(project.getUser().getId());
    }
}
