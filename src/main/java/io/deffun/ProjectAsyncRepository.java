package io.deffun;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import io.micronaut.data.repository.reactive.ReactorCrudRepository;

@Repository
public interface ProjectAsyncRepository
        extends ReactorCrudRepository<ProjectEntity, Long> {
//        extends ReactiveStreamsCrudRepository<ProjectEntity, Long> {
}
