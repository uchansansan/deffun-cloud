package io.deffun;

import io.deffun.gen.Database;
import io.deffun.gen.Framework;

public final class CreateProjectData {
    private final String schema;
    private final /*@Nullable*/ String basePackage;
    private final /*@Nullable*/ Framework framework;
    private final /*@Nullable*/ Database database;
    private final String username;

    public CreateProjectData(String schema, String basePackage, Framework framework, Database database, String username) {
        this.schema = schema;
        this.basePackage = basePackage;
        this.framework = framework;
        this.database = database;
        this.username = username;
    }

    public String schema() {
        return schema;
    }

    public String basePackage() {
        return basePackage;
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
