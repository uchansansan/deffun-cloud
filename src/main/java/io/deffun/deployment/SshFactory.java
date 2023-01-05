package io.deffun.deployment;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.OpenSSHConfig;
import com.jcraft.jsch.Slf4jLogger;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Factory
public class SshFactory {
    @Singleton
    public JSch jSch() throws JSchException, IOException {
        JSch.setLogger(new Slf4jLogger());
        JSch jSch = new JSch();

        String configFile = "/Users/artem/.ssh/config";
        if (Files.exists(Paths.get(configFile))) {
            OpenSSHConfig openSSHConfig = OpenSSHConfig.parseFile(configFile);
            jSch.setConfigRepository(openSSHConfig);
        }

        jSch.addIdentity("/Users/artem/.ssh/personal_selectel");

        String knownHostsFile = "/Users/artem/.ssh/known_hosts";
        if (Files.exists(Paths.get(knownHostsFile))) {
            jSch.setKnownHosts(knownHostsFile);
        }

        return jSch;
    }
}
