package io.deffun.deployment;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class DokkuService {
    @Inject
    private JSch jSch;

    public String list() {
        String command = "apps:list";
        return execCommand(command);
    }

    public String addSshKey(String user, String sshKey) {
        String command = "ssh-keys:add %s %s".formatted(user, sshKey);
        return execCommand(command);
    }

    public static void main(String[] args) {
        DokkuService dokkuService = new DokkuService();
        String projectName = "deffun-cloud6";
        dokkuService.createRemoteApp(projectName);
        dokkuService.initLocalAndPush(Paths.get("/Users/artem/projects/deffun/deffun-tests/deffun-cloud2"), projectName);
    }

    public void createRemoteApp(String projectName) {
        String command = "apps:create %s".formatted(projectName);
        execCommand(command);
        execCommand("postgres:create mydatabase");
        execCommand("postgres:link mydatabase %s".formatted(projectName));
    }

    // another possible ssh impl - https://github.com/hierynomus/sshj/blob/master/examples/src/main/java/net/schmizz/sshj/examples/InMemoryKnownHosts.java
    // but am not sure we JGit can use it

    // TBD - use user's username, email, ssh keys and so on
    public void initLocalAndPush(Path path, String projectName) {
        // https://git.eclipse.org/r/plugins/gitiles/jgit/jgit/+/af0126e1d01100fad673b6d0a56a99633383a198/org.eclipse.jgit.ssh.apache/README.md
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
//                JSch jSch = super.createDefaultJSch(fs);
                return jSch;
            }
        };
        try (Git git = Git.init().setDirectory(path.toFile()).call()) {
            //git remote add dokku dokku@deffun.io:ruby-getting-started
            git.remoteAdd()
                    .setName("dokku")
                    .setUri(new URIish("dokku@deffun.io:%s".formatted(projectName)))
                    .call();
            git.add().addFilepattern(".").call();
            git.commit().setMessage("init").call();
            //git push dokku main:master
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            git.push()
                    .setRemote("dokku")
                    .setTransportConfigCallback(transport -> {
                        if (transport instanceof SshTransport sshTransport) {
                            sshTransport.setSshSessionFactory(sshSessionFactory);
                        }
                    })
                    .setOutputStream(responseStream)
                    .call();
            System.out.println(responseStream);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String execCommand(String command) {
        Session session = null;
        ChannelExec channel = null;
        try {
            session = jSch.getSession("dokku", "deffun.io");
            session.connect();

            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.connect();

            return responseStream.toString();
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
    }
}
