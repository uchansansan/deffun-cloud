package io.deffun;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity(
        name = "projects"
)
class ProjectEntity {
    @Id
    @GeneratedValue(
            strategy = GenerationType.AUTO
    )
    private Long id;

    @OneToOne
    @JoinColumn(
            name = "user_id"
    )
    private UserEntity user;

    @Column(
            name = "name",
            nullable = false
    )
    private String name;

    @Column(
            name = "domain",
            nullable = false
    )
    private String domain;

    @Column(
            name = "sourceCodeUrl",
            nullable = false
    )
    private String sourceCodeUrl;

    @Column(
            name = "endpointUrl",
            nullable = false
    )
    private String endpointUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
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
}
