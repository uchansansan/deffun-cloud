package io.deffun.usermgmt;

import jakarta.inject.Singleton;

@Singleton
public class UserMapper {
    public UserData userEntityToUserData(UserEntity entity) {
        UserData dataClass = new UserData();
        dataClass.setId(entity.getId());
        dataClass.setUsername(entity.getUsername());
        dataClass.setEmail(entity.getEmail());
        return dataClass;
    }

    public UserEntity userDataToUserEntity(UserData dataClass) {
        UserEntity entity = new UserEntity();
        entity.setUsername(dataClass.getUsername());
        entity.setEmail(dataClass.getEmail());
        return entity;
    }
}
