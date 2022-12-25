package io.deffun;

import jakarta.inject.Singleton;

@Singleton
public class UserMapper {
    public UserData userEntityToUserData(UserEntity entity) {
        UserData dataClass = new UserData();
        dataClass.setUsername(entity.getUsername());
        return dataClass;
    }

    public UserEntity userDataToUserEntity(UserData dataClass) {
        UserEntity entity = new UserEntity();
        entity.setUsername(dataClass.getUsername());
        return entity;
    }
}
