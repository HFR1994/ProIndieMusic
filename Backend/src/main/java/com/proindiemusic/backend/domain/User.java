package com.proindiemusic.backend.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigInteger;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends Entity implements UserDetails {

    private String locale;
    private String gAccessToken;
    private String accessToken;
    private String email;
    private List<GrantedAuthority> roles = new ArrayList<>();

    public void setRoles(String role){

        if(role.contains("[")){
            if(!role.equalsIgnoreCase("[]")) {
                String[] ar = role.substring(1, role.length() - 1).split(",");
                for (String anAr : ar) {
                    roles.add(new SimpleGrantedAuthority(anAr.trim()));
                }
            }
        }else {
            roles.add(new SimpleGrantedAuthority(role.trim()));
        }
    }

    public boolean hasRole(String value){
        for(GrantedAuthority role : roles){
            if(role.getAuthority().equals(value))
                return true;
        }
        return false;
    }

    public List<GrantedAuthority> getRoles() {
        return roles;
    }

    public void resetRoles(){
        roles = new ArrayList<>();
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getgAccessToken() {
        return gAccessToken;
    }

    public void setgAccessToken(String gAccessToken) {
        this.gAccessToken = gAccessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "locale='" + locale + '\'' +
                ", gAccessToken='" + gAccessToken + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + Arrays.toString(roles.toArray()) +
                '}';
    }

    /**
     * Métodos requeridos por user details
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    public String getPassword() {
        return gAccessToken;
    }

    @Override
    public String getUsername() {
        return getUuid();
    }

    @Override
    public boolean isAccountNonExpired() {
        return getStatus();
    }

    @Override
    public boolean isAccountNonLocked() {
        return getStatus();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return getStatus();
    }

    @Override
    public boolean isEnabled() {
        return getStatus();
    }

    /**
     * FIN de Métodos requeridos por user details
     */

    @Override
    public Class findDomainClass() {
        return User.class;
    }



}

