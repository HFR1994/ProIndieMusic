package tech.aabo.celulascontentas.oauth.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigInteger;
import java.util.Date;

public abstract class Entity<T>{

    private Integer id;

    private String uuid;

    @JsonIgnore
    private Boolean status;

    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss zz")
    private Date dateCreated;

    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss zz")
    private Date dateModified;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public abstract Class<T> findDomainClass();

}
