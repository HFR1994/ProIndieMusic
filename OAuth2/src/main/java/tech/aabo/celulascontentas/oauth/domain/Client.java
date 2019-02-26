package tech.aabo.celulascontentas.oauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Arrays;
import java.util.HashSet;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Client extends Entity{

    private String fullName;
    private String cEmail;
    private Boolean verifiedCEmail;
    private String clientId;
    private String clientSecret;
    private HashSet<String> redirects;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getcEmail() {
        return cEmail;
    }

    public void setcEmail(String cEmail) {
        this.cEmail = cEmail;
    }

    public Boolean getVerifiedCEmail() {
        return verifiedCEmail;
    }

    public void setVerifiedCEmail(Boolean verifiedCEmail) {
        this.verifiedCEmail = verifiedCEmail;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public Class findDomainClass() {
        return Client.class;
    }

    public HashSet<String> getRedirects() {
        return redirects;
    }

    public void setRedirects(String redirects){

        if(this.redirects==null){
            this.redirects=new HashSet<>();
        }

        if(redirects.contains("[")){
            if(!redirects.equalsIgnoreCase("[]")) {
                String[] ar = redirects.substring(1, redirects.length() - 1).split(",");
                for (String anAr : ar) {
                    this.redirects.add(anAr.trim());
                }
            }
        }else {
            this.redirects.add(redirects.trim());
        }
    }

    public void setRedirects(String[] redirects) {

        this.redirects = new HashSet<>();
        for (String redirect : redirects) {
            this.redirects.add(redirect.trim());
        }

    }

    private String printRedirects(){
        StringBuilder urls=new StringBuilder();
        int size = redirects.size()-1;
        for(String url : redirects){
            if(size == 0){
                urls.append('"').append(url).append('"');
            }else {
                urls.append('"').append(url).append("\",\n");
            }
            size-=1;
        }
        return urls.toString();
    }

    public void resetRedirects(){
        redirects = new HashSet<>();
    }

    @Override
    public String toString() {
        return "{" +
                "fullName='" + fullName + '\'' +
                ", cEmail='" + cEmail + '\'' +
                ", verifiedCEmail=" + verifiedCEmail +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", status='" + getStatus() + '\'' +
                ", redirect_uri=[" + printRedirects()+ ']' +
                '}';
    }
}
