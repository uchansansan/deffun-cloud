package io.deffun.deployment;

import io.deffun.doh.DatabasePlugin;
import io.deffun.doh.Dokku;
import io.deffun.doh.SshDokkuExecutor;
import io.deffun.doh.Util;
import io.deffun.gen.Database;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Singleton
public class DokkuService {
    private static final Logger LOG = LoggerFactory.getLogger(DokkuService.class);

    @Inject
    private SshDokkuExecutor sshDokkuExecutor;
    @Value("${deffun.dokku.deployment.useHerokuish}")
    private boolean useHerokuish; // probably should be moved to project properties

    public void setupDokkuApp(String appName, Database database, String userEmail) {
        sshDokkuExecutor.session(dokku -> {
            dokku.apps().create(appName);
            if (useHerokuish) {
                dokku.buildpacks().add(appName, "https://github.com/heroku/heroku-buildpack-java.git");
            } else {
                dokku.builder().setSelected(appName, "pack");
            }
            DatabasePlugin databasePlugin = getDatabasePlugin(dokku, database);
            String dbName = appName + "_db";
            databasePlugin.create(dbName);
            databasePlugin.link(dbName, appName);
            LOG.info(">>> linked app {} and db {} using engine {}", appName, dbName, database);
            if (database == Database.MONGODB) {
                String databaseUrl = dokku.config().get(appName, "MONGO_URL").replaceAll("\n", "");
                LOG.info(">>> mongo database URL {}", databaseUrl);
                // for mongo and Micronaut
                // MONGODB_URI=mongodb://username:password@production-server:27017/databaseName
                dokku.config().set(appName, "MONGODB_URI", databaseUrl);
            } else {
                String databaseUrl = dokku.config().get(appName, "DATABASE_URL").replaceAll("\n", "");
                LOG.info(">>> database URL {}", databaseUrl);
                URI jdbcUrl = Util.convertDbUrlToJdbcUrl(URI.create(databaseUrl));
                LOG.info(">>> JDBC URL {}", jdbcUrl);
                dokku.config().set(appName, "DATASOURCES_DEFAULT_URL", jdbcUrl.toString());
            }
            dokku.letsEncryptPlugin().setEmail(appName, userEmail);
            dokku.letsEncryptPlugin().enable(appName);
        });
    }

    public void deleteDokkuApp(String appName, Database database) {
        sshDokkuExecutor.session(dokku -> {
            dokku.apps().destroyForce(appName);
            DatabasePlugin databasePlugin = getDatabasePlugin(dokku, database);
            LOG.info("delete using {}", databasePlugin);
            databasePlugin.destroyForce(appName + "_db");
        });
    }

    private static DatabasePlugin getDatabasePlugin(Dokku dokku, Database database) {
        return switch (database) {
            case MYSQL -> dokku.mySqlPlugin();
            case MARIADB -> dokku.mariaDbPlugin();
            case POSTGRES -> dokku.postgresPlugin();
            case MONGODB -> dokku.mongoPlugin();
        };
    }
}
