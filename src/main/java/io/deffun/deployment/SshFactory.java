package io.deffun.deployment;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.OpenSSHConfig;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.Slf4jLogger;
import io.deffun.doh.JSchDokkuExecutor;
import io.deffun.doh.SshDokkuExecutor;
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
    private static final Logger LOG = LoggerFactory.getLogger(SshFactory.class);

    @Singleton
    public JSch jSch(
            @Value("${deffun.dokku.keyFile}") String keyFile,
            @Value("${deffun.dokku.keyPassphrase}") String passphrase
    ) throws JSchException, IOException {
        JSch.setLogger(new Slf4jLogger());
        JSch jSch = new JSch();

        String configFile = System.getProperty("user.home") + "/.ssh/config";
        LOG.info(configFile);
        if (Files.exists(Paths.get(configFile))) {
            LOG.info(configFile + " exists");
            OpenSSHConfig openSSHConfig = OpenSSHConfig.parseFile(configFile);
            jSch.setConfigRepository(openSSHConfig);
        }

        jSch.addIdentity(keyFile, passphrase);

        String knownHostsFile = System.getProperty("user.home") + "/.ssh/known_hosts";
        LOG.info(knownHostsFile);
        if (Files.exists(Paths.get(knownHostsFile))) {
            LOG.info(knownHostsFile + " exists");
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
    public SshDokkuExecutor dokkuExecutor(JSch jSch, @Value("${deffun.dokku.host}") String host) {
        return new JSchDokkuExecutor(jSch, "dokku", host);
    }

    @Singleton
    public Haikunator haikunator() {
        return new Haikunator().setDelimiter("");
    }

//    @Named("deploymentExecutor")
//    @Singleton
//    public ExecutorService deploymentExecutor() {
//        return Executors.newFixedThreadPool(10);
//    }
}
