package io.deffun.usermgmt;

import io.deffun.NotEnoughBalanceException;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.utils.SecurityService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.RandomStringUtils;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Singleton
public class UserService {
    @Inject
    private UserMapper userMapper;
    @Inject
    private UserRepository userRepository;
    @Inject
    private SecurityService securityService;
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

    public UserData saveIfAbsent(UserData userData) {
        UserEntity userEntity = userRepository.findByEmail(userData.getEmail())
                .orElseGet(() -> {
                    UserEntity entity = userMapper.userDataToUserEntity(userData);
                    return userRepository.save(entity);
                });
        return userMapper.userEntityToUserData(userEntity);
    }

    @Transactional
    public void topUpBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NotEnoughBalanceException("Illegal balance " + balance);
        }
        UserEntity userEntity = currentUser();
        userEntity.setBalance(userEntity.getBalance().add(balance));
        userRepository.update(userEntity);
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

    //region SECURITY
    private UserEntity currentUser() {
        String email = currentUserEmail();
        return userRepository.findByEmail(email).orElseThrow(); // or else 401 Unauthorized
    }

    private String currentUserEmail() {
        Authentication authentication = securityService.getAuthentication().orElseThrow();
        return (String) authentication.getAttributes().get("email");
    }
    //endregion
}
