package io.deffun;

import io.deffun.usermgmt.UserEntity;
import io.deffun.usermgmt.UserRepository;
import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.dataloader.MappedBatchLoader;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

// todo use UserData and UserService instead?
@Singleton
public class UserDataLoader implements MappedBatchLoader<Long, UserEntity> {
    private final UserRepository userRepository;
    private final ExecutorService executor;

    public UserDataLoader(
            UserRepository userRepository,
            @Named(TaskExecutors.IO) ExecutorService executor
    ) {
        this.userRepository = userRepository;
        this.executor = executor;
    }

    @Override
    public CompletionStage<Map<Long, UserEntity>> load(Set<Long> set) {
        return CompletableFuture.supplyAsync(() -> userRepository
                        .findByIdIn(set)
                        .stream()
                        .collect(Collectors.toMap(UserEntity::getId, Function.identity())),
                executor
        );
    }
}
