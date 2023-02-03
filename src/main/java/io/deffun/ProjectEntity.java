package io.deffun;

import io.deffun.gen.Database;
import io.deffun.usermgmt.UserEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity(
        name = "projects"
)
public class ProjectEntity {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @OneToOne
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name="FK_USER")
    )
    private UserEntity user;

    @Column(
            name = "name",
            nullable = false
    )
    private String name;

    @Column(
            name = "api_name"
    )
    private String apiName;

    @Column(
            name = "db_name"
    )
    @Enumerated(EnumType.STRING)
    private Database database;

    @Column(
            name = "api_endpoint_url"
    )
    private String apiEndpointUrl;

    @Column(name = "schema_content")
    private String schema;

    @Column(name = "deploying")
    private boolean deploying;

    @Column(name = "ver", nullable = false)
    private int version;

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

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
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

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}
