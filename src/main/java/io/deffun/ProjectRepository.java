package io.deffun;

import io.deffun.gen.Database;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import javax.transaction.Transactional;

@Repository
public interface ProjectRepository extends CrudRepository<ProjectEntity, Long> {
    Iterable<ProjectEntity> findAllByUserId(Long userId);

    @Transactional
    void update(@Id Long id, boolean deploying, String apiName, Database database);

    @Transactional
    void update(@Id Long id, boolean deploying, String apiEndpointUrl);
}
