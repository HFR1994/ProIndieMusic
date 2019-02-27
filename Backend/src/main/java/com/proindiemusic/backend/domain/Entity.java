package com.proindiemusic.backend.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.proindiemusic.backend.pojo.annotations.Date;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public abstract class Entity<T>{

    private String uuid;

    private String _rev;

    private Boolean status;

    private String userAuth;

    private String table;

    @Date
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private java.util.Date dateCreated;

    @Date
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private java.util.Date dateModified;


    private HashMap<String, Object> additionalProperties;

    public HashMap<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(HashMap<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }


    public String getUserAuth() {
        return userAuth;
    }

    public void setUserAuth(String userAuth) {
        this.userAuth = userAuth;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String get_rev() {
        return _rev;
    }

    public void set_rev(String _rev) {
        this._rev = _rev;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public java.util.Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(java.util.Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public java.util.Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(java.util.Date dateModified) {
        this.dateModified = dateModified;
    }


    public abstract Class<T> findDomainClass();

    public Entity() {
        this.additionalProperties = new HashMap<>();
    }

    public void setProperty(String key, Object value) {
        this.additionalProperties.put(key, value);
    }

    public Object getProperty(String key, Class clase){
        if(this.additionalProperties.containsKey(key))
            return clase.cast(this.additionalProperties.get(key));
        else
            return null;
    }

    public HashMap<String, Object> getProperties(){
        return this.additionalProperties;
    }

    @Override
    public boolean equals(Object o) {

        Class<T> clase = findDomainClass();

        if (o == this) return true;

        if (!clase.isInstance(o)) {
            return false;
        }

        T obj = clase.cast(o);

        EqualsBuilder equals = new EqualsBuilder();

        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            try {
                equals.append(PropertyUtils.getProperty(this, field.getName()),field.get(obj));
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }


        return equals.isEquals();
    }

    @Override
    public int hashCode() {
        Field[] fields = this.getClass().getDeclaredFields();
        HashCodeBuilder var = new HashCodeBuilder(17, 37);
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                var.append(field.get(this.getClass()));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return var.toHashCode();
    }

}
