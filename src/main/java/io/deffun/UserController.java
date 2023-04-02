package io.deffun;

import io.deffun.usermgmt.UserData;
import io.deffun.usermgmt.UserService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.StreamingFileUpload;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import java.io.File;
import java.io.IOException;

@Controller("/api/users")
@Secured(SecurityRule.IS_ANONYMOUS)
public class UserController {
    @Inject
    private UserService userService;

    @Get("/profile")
    public UserData profile(Authentication authentication) {
        String email = (String) authentication.getAttributes().get("email");
        return userService.getByEmail(email);
    }


    @ExecuteOn(TaskExecutors.IO)
    @Post(value = "/upload_file", consumes = MediaType.MULTIPART_FORM_DATA, produces = MediaType.TEXT_PLAIN)
    public Publisher<HttpResponse<String>> upload(StreamingFileUpload file) {
        File tempFile;
        try {
            tempFile = File.createTempFile(file.getFilename(), "temp");
        } catch (IOException e) {
            return Mono.error(e);
        }
        Publisher<Boolean> uploadPublisher = file.transferTo(tempFile);

        return Mono.from(uploadPublisher)
                .map(success -> {
                    if (success) {
                        return HttpResponse.ok("Uploaded");
                    } else {
                        return HttpResponse.<String>status(HttpStatus.CONFLICT)
                                .body("Upload Failed");
                    }
                });
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
