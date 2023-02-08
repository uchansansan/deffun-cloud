package io.deffun.billing;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "charges")
public class Charges {
    @Column(name = "project_id")
    private Long projectId;
    @Column(name = "last_charge")
    private LocalDateTime lastCharge;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public LocalDateTime getLastCharge() {
        return lastCharge;
    }

    public void setLastCharge(LocalDateTime lastCharge) {
        this.lastCharge = lastCharge;
    }
}
