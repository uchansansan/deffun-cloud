package io.deffun.deployment;

import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.URIish;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

@Singleton
public class GitService {
    private static final String DEFAULT_REMOTE_NAME = "dokku";

    @Inject
    private SshSessionFactory sshSessionFactory;
    @Value("${deffun.dokku.host}")
    private String host;

    // TBD - use user's username, email, ssh keys and so on
    public void initRepository(Path path, String projectName) {
        URIish uri;
        try {
            uri = new URIish("dokku@%s:%s".formatted(host, projectName));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try (Git git = Git.init().setDirectory(path.toFile()).call()) {
            //git remote add dokku dokku@deffun.app:myapp
            git.remoteAdd()
                    .setName(DEFAULT_REMOTE_NAME)
                    .setUri(uri)
                    .call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Initial commit").call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public void pushRepository(Path path) {
        try (Git git = Git.open(path.toFile())) {
            //git push dokku main:master
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            git.push()
                    .setRemote(DEFAULT_REMOTE_NAME)
                    .setTransportConfigCallback(transport -> {
                        if (transport instanceof SshTransport sshTransport) {
                            sshTransport.setSshSessionFactory(sshSessionFactory);
                        }
                    })
                    .setOutputStream(responseStream)
                    .call();
        } catch (IOException e) { // cannot open repo
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
