package io.deffun;

import io.deffun.usermgmt.UserData;

public class ProjectData {
    private Long id;

    private String name;

    private String domain;

    private String sourceCodeUrl;

    private String endpointUrl;

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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSourceCodeUrl() {
        return sourceCodeUrl;
    }

    public void setSourceCodeUrl(String sourceCodeUrl) {
        this.sourceCodeUrl = sourceCodeUrl;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }
}
