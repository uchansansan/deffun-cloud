package io.deffun;

import io.micronaut.context.annotation.Factory;
import io.micronaut.runtime.http.scope.RequestScope;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderRegistry;

@Factory
public class DataLoaderRegistryFactory {
    @RequestScope
    public DataLoaderRegistry dataLoaderRegistry(UserDataLoader userDataLoader) {
        DataLoaderRegistry dataLoaderRegistry = new DataLoaderRegistry();
        dataLoaderRegistry.register(
                "user",
                DataLoader.newMappedDataLoader(userDataLoader)
        );
        return dataLoaderRegistry;
    }
}
