package io.deffun;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
//    UserEntity findByUsername(String username);

    Optional<UserEntity> findByUsername(String username);

    Collection<UserEntity> findByIdIn(Collection<Long> ids);

    default UserEntity findOrCreate(String username) {
        return findByUsername(username).orElseGet(() -> {
            UserEntity entity = new UserEntity();
            entity.setUsername(username);
            return save(entity);
        });
    }
}
