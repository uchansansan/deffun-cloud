package io.deffun;

import io.deffun.gen.Database;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRepository extends CrudRepository<ProjectEntity, Long> {
    List<ProjectEntity> findAllByUserId(Long userId);

    // todo in 2 update methods below 'deploying' value can be hardcoded in @query...
    //  and methods must be renamed to something meaningful
    @Transactional
    void update(@Id Long id, boolean deploying, String apiName, Database database);

    @Transactional
    void update(@Id Long id, boolean deploying, String apiEndpointUrl, LocalDateTime lastCharge);

    @Transactional
    void update(@Id Long id, String apiEndpointUrl);
}
