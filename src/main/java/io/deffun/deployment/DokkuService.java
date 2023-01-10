package io.deffun.deployment;

import io.deffun.gen.Database;
import io.deffun.janedokku.DatabasePlugin;
import io.deffun.janedokku.Dokku;
import jakarta.inject.Singleton;

@Singleton
public class DokkuService {
    private final Dokku dokku;

    public DokkuService(Dokku dokku) {
        this.dokku = dokku;
    }

    public String list() {
//        dokku.apps().list()
        return "";
    }

    public void addSshKey(String user, String sshKey) {
        dokku.sshKeys().add(user, sshKey);
    }

    public void createRemoteApp(String projectName, Database database) {
        dokku.apps().create(projectName);
        DatabasePlugin databasePlugin = switch (database) {
            case MARIADB -> dokku.mariaDbPlugin();
            case MYSQL -> dokku.mySqlPlugin();
            case POSTGRES -> dokku.postgresPlugin();
        };
        String dbName = projectName + "-db";
        databasePlugin.create(dbName);
        databasePlugin.link(dbName, projectName);
    }
}
