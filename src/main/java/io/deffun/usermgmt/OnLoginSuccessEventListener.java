package io.deffun.usermgmt;

import io.deffun.ProjectService;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.event.LoginSuccessfulEvent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class OnLoginSuccessEventListener implements ApplicationEventListener<LoginSuccessfulEvent> {
    @Inject
    private UserService userService;
    @Inject
    private ProjectService projectService;

    @Override
    public void onApplicationEvent(LoginSuccessfulEvent event) {
        Authentication authentication = (Authentication) event.getSource();
        Boolean emailVerified = (Boolean) authentication.getAttributes().get("email_verified");
        if (emailVerified == null || !emailVerified) {
            // TODO: send verification code
            return;
        }
        UserData userData = new UserData();
        userData.setUsername((String) authentication.getAttributes().get("name"));
        userData.setEmail((String) authentication.getAttributes().get("email"));
        UserData currentUser = userService.saveIfAbsent(userData);
        projectService.createTestProjectIfAbsent(currentUser.getId());
    }
}
