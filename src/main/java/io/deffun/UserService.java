package io.deffun;

import jakarta.inject.Singleton;

@Singleton
public class UserService {
    private final UserMapper userMapper;

    private final UserRepository userRepository;

    public UserService(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }
}
