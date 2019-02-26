package tech.aabo.celulascontentas.oauth.domain;

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

    private String email;
    private String password;
    private Boolean verifiedEmail;
    private String name;
    private String pictureURL;
    private String locale;
    private String familyName;
    private String givenName;
    private String gAccessToken;
    private String gRefreshToken;
    private BigInteger googleId;
    private Boolean expired;
    private Boolean locked;
    private List<GrantedAuthority> roles = new ArrayList<>();
    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss zz")
    private Date gExpirationDate;
    private String accessToken;
    private String refreshToken;
    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss zz")
    private Date expirationDate;
    private String userUuid;
    private String authorizationUuid;
    private String clientUuid;
    private boolean scopeUpdated;


    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isScopeUpdated() {
        return scopeUpdated;
    }

    public void setScopeUpdated(boolean scopeUpdated) {
        this.scopeUpdated = scopeUpdated;
    }

    public String getGAcessToken() {
        return gAccessToken;
    }

    public void setGAccessToken(String gAccessToken) {
        this.gAccessToken = gAccessToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getVerifiedEmail() {
        return verifiedEmail;
    }

    public void setVerifiedEmail(Boolean verifiedEmail) {
        this.verifiedEmail = verifiedEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public void setPictureURL(String pictureURL) {
        this.pictureURL = pictureURL;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getGRefreshToken() {
        return gRefreshToken;
    }

    public void setGRefreshToken(String gRefreshToken) {
        this.gRefreshToken = gRefreshToken;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

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

    public List<GrantedAuthority> getRoles() {
        return roles;
    }

    public void resetRoles(){
        roles = new ArrayList<>();
    }

    public Date getGExpirationDate() {
        return gExpirationDate;
    }

    public void setGExpirationDate(Date gExpirationDate) {
        this.gExpirationDate = gExpirationDate;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public BigInteger getGoogleId() {
        return googleId;
    }

    public void setGoogleId(BigInteger googleId) {
        this.googleId = googleId;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getClientUuid() {
        return clientUuid;
    }

    public void setClientUuid(String clientUuid) {
        this.clientUuid = clientUuid;
    }

    public String getAuthorizationUuid() {
        return authorizationUuid;
    }

    public void setAuthorizationUuid(String authorizationUuid) {
        this.authorizationUuid = authorizationUuid;
    }

    /**
     * Métodos requeridos por user details
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    public String getPassword() {
        return accessToken;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return getStatus();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !expired;
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


    @Override
    public String toString() {
        return "{" +
                "uuid: '"+getUuid()+"'"+
                ", email='" + email + '\'' +
                ", verifiedEmail=" + verifiedEmail +
                ", name='" + name + '\'' +
                ", pictureURL='" + pictureURL + '\'' +
                ", locale='" + locale + '\'' +
                ", familyName='" + familyName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", status=" + getStatus() +
                '}';
    }
}

