package io.deffun;

import io.deffun.usermgmt.UserData;

public class ProjectData {
    private Long id;

    private String name;

    private String apiName;

    private String apiEndpointUrl;

    private String schema;

    private boolean deploying;

    private boolean test;

    private UserData user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiEndpointUrl() {
        return apiEndpointUrl;
    }

    public void setApiEndpointUrl(String apiEndpointUrl) {
        this.apiEndpointUrl = apiEndpointUrl;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public boolean isDeploying() {
        return deploying;
    }

    public void setDeploying(boolean deploying) {
        this.deploying = deploying;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }
}
