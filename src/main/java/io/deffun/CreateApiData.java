package io.deffun;

import io.deffun.gen.Database;
import io.deffun.gen.Framework;

public class CreateApiData {
    private Long projectId;
    private /*@Nullable*/ String name; // optional name
    private String schema;
    private /*@Nullable*/ String basePackage;
    private /*@Nullable*/ Framework framework;
    private /*@Nullable*/ Database database;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public Framework getFramework() {
        return framework;
    }

    public void setFramework(Framework framework) {
        this.framework = framework;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public String toString() {
        return "CreateApiData{" +
               "projectId=" + projectId +
               ", name='" + name + '\'' +
               ", schema='" + schema + '\'' +
               ", basePackage='" + basePackage + '\'' +
               ", framework=" + framework +
               ", database=" + database +
               '}';
    }
}
