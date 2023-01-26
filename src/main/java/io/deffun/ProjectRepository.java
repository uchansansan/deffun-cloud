package io.deffun;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

@Repository
public interface ProjectRepository extends CrudRepository<ProjectEntity, Long> {
    Iterable<ProjectEntity> findAllByUserId(Long userId);
}
