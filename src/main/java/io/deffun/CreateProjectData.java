package io.deffun;

import io.deffun.gen.Database;
import io.deffun.gen.Framework;

public final class CreateProjectData {
    private final String schema;
    private final String name;
    private final /*@Nullable*/ String domain;
    private final /*@Nullable*/ Framework framework;
    private final /*@Nullable*/ Database database;
    private final String username;

    public CreateProjectData(String schema, String name, String domain, Framework framework, Database database, String username) {
        this.schema = schema;
        this.name = name;
        this.domain = domain;
        this.framework = framework;
        this.database = database;
        this.username = username;
    }

    public String schema() {
        return schema;
    }

    public String name() {
        return name;
    }

    public String domain() {
        return domain;
    }

    public Framework framework() {
        return framework;
    }

    public Database database() {
        return database;
    }

    public String username() {
        return username;
    }
}
