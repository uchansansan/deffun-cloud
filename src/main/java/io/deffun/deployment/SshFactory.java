package io.deffun.deployment;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.OpenSSHConfig;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Slf4jLogger;
import io.deffun.janedokku.CommandExecutor;
import io.deffun.janedokku.Dokku;
import io.deffun.janedokku.JSchCommandExecutor;
import io.deffun.janedokku.SshUrl;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Factory
public class SshFactory {
    @Singleton
    public JSch jSch(
            @Value("${deffun.dokku.keyFile}") String keyFile,
            @Value("${deffun.dokku.passphrase}") String passphrase
    ) throws JSchException, IOException {
        JSch.setLogger(new Slf4jLogger());
        JSch jSch = new JSch();

        String configFile = "/root/.ssh/config";
        if (Files.exists(Paths.get(configFile))) {
            OpenSSHConfig openSSHConfig = OpenSSHConfig.parseFile(configFile);
            jSch.setConfigRepository(openSSHConfig);
        }

        jSch.addIdentity(keyFile, passphrase);

        String knownHostsFile = "/root/.ssh/known_hosts";
        if (Files.exists(Paths.get(knownHostsFile))) {
            jSch.setKnownHosts(knownHostsFile);
        }

        return jSch;
    }

    @Singleton
    public SshSessionFactory sshSessionFactory(JSch jSch) {
        // https://git.eclipse.org/r/plugins/gitiles/jgit/jgit/+/af0126e1d01100fad673b6d0a56a99633383a198/org.eclipse.jgit.ssh.apache/README.md
        return new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                //JSch jSch = super.createDefaultJSch(fs);
                return jSch;
            }
        };
    }

    @Singleton
    public Dokku dokku(JSch jSch, @Value("${deffun.dokku.host}") String host) {
        CommandExecutor commandExecutor = new JSchCommandExecutor(jSch, new SshUrl("dokku", host));
        return new Dokku(commandExecutor);
    }
}
