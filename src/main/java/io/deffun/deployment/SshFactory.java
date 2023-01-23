package io.deffun.deployment;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.OpenSSHConfig;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Slf4jLogger;
import io.deffun.doh.CommandExecutor;
import io.deffun.doh.Dokku;
import io.deffun.doh.JSchCommandExecutor;
import io.deffun.doh.SshUrl;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import me.atrox.haikunator.Haikunator;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.JschConfigSessionFactory;
import org.eclipse.jgit.transport.ssh.jsch.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        // todo /root -> System.getProperty and also how to add known host programmatically or via script?
        String configFile = System.getProperty("user.home") + "/.ssh/config";
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info(configFile);
        if (Files.exists(Paths.get(configFile))) {
            logger.info(configFile + " exists");
            OpenSSHConfig openSSHConfig = OpenSSHConfig.parseFile(configFile);
            jSch.setConfigRepository(openSSHConfig);
        }

        jSch.addIdentity(keyFile, passphrase);

        String knownHostsFile = System.getProperty("user.home") + "/.ssh/known_hosts";
        logger.info(knownHostsFile);
        if (Files.exists(Paths.get(knownHostsFile))) {
            logger.info(knownHostsFile + " exists");
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

    @Singleton
    public Haikunator haikunator() {
        return new Haikunator().setDelimiter("");
    }
}
