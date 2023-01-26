package io.deffun.usermgmt;

import io.deffun.doh.Dokku;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.RandomStringUtils;

@Singleton
public class UserService {
    @Inject
    private UserMapper userMapper;
    @Inject
    private UserRepository userRepository;
//    private TokenRepository tokenRepository;
//    @Inject
//    private Dokku dokku;

    public UserData save(UserData userData) {
        UserEntity userEntity = userMapper.userDataToUserEntity(userData);
        UserEntity saved = userRepository.save(userEntity);
        return userMapper.userEntityToUserData(saved);
    }

    public UserData getById(Long id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow();
        return userMapper.userEntityToUserData(userEntity);
    }

    public UserData getByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow();
        return userMapper.userEntityToUserData(userEntity);
    }

    public void saveIfAbsent(UserData userData) {
        userRepository.findByEmail(userData.getEmail())
                .orElseGet(() -> userRepository.save(userMapper.userDataToUserEntity(userData)));
    }

    public void uploadSshKey(String email, String sshPublicKey) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("not found"));
        userEntity.setSshPublicKey(sshPublicKey);
        userRepository.update(userEntity);
//        dokku.sshKeys().add(String.valueOf(userEntity.getId()), sshPublicKey);
    }

    public String getSshKey(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("not found"));
        return userEntity.getSshPublicKey();
    }

    public String generateApiKey(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("not found"));
        String apiKey = generateApiKey();
//        userEntity.setApiKey(apiKey);
        userRepository.update(userEntity);
        return apiKey;
    }

    public String getApiKey(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("not found"));
//        return userEntity.getApiKey();
        return "";
    }

    private static String generateApiKey() {
        String random = RandomStringUtils.random(20, true, true);
        return "df_" + random;
    }
}
