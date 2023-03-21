package io.deffun;

import io.deffun.usermgmt.UserData;
import io.deffun.usermgmt.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Controller("/api/users")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class UserController {
    @Inject
    private UserService userService;

    @Get("/profile")
    public UserData profile(Authentication authentication) {
        String email = (String) authentication.getAttributes().get("email");
        return userService.getByEmail(email);
    }

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Post("/upload_ssh_key")
    public HttpResponse<Void> uploadSshKey( StreamingFileUpload file) throws IOException {
        FileWriter f = new FileWriter("1.pub");
        f.write(file.toString());
        f.close();
//Authentication authentication,  String sshPublicKey,
        //String email = (String) authentication.getAttributes().get("email");
//        if (email == null) {
//            // use AuthenticationExceptionHandler // or create your own
//            throw new AuthenticationException("no email");
//        }
        //userService.uploadSshKey(email, sshPublicKey);
        return HttpResponse.ok();
    }

    @Get("/ssh_key")
    public HttpResponse<String> getSshKey(Authentication authentication) {
        String email = (String) authentication.getAttributes().get("email");
        String sshKey = userService.getSshKey(email);
        return HttpResponse.ok(sshKey);
    }

    @Post("/generate_api_key")
    public String generateApiKey(Authentication authentication) {
        String email = (String) authentication.getAttributes().get("email");
        return userService.generateApiKey(email);
    }

    @Get("/api_key")
    public String getApiKey(Authentication authentication) {
        String email = (String) authentication.getAttributes().get("email");
        return userService.getApiKey(email);
    }
}
