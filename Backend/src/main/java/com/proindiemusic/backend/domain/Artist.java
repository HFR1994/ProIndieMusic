package com.proindiemusic.backend.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.proindiemusic.backend.pojo.annotations.Administrator;
import com.proindiemusic.backend.pojo.annotations.Email;
import com.sun.corba.se.spi.ior.ObjectKey;


@SuppressWarnings("ALL")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Artist extends Entity {

    private String bandName;
    @Email
    private String email;
    private String shandraw;
    private String photo;
    private Object trayectory;
    private Object costPerPresentation;
    private Object internationalConcerts;
    private Object concertsPerYear;
    private Object attendancePerConcert;
    private Object socialMediaFollowUp;
    private String liveVideo;
    private String studioVideo;
    private String pressKit;

    @Administrator
    private Object level;
    @Administrator
    private Object sublevel;
    @Administrator
    private Object completeness;
    @Administrator
    private String review;
    @Administrator
    private String step1;
    @Administrator
    private String step2;
    @Administrator
    private String step3;


    public String getBandName() {
        return bandName;
    }

    public void setBandName(String bandName) {
        this.bandName = bandName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getShandraw() {
        return shandraw;
    }

    public void setShandraw(String shandraw) {
        this.shandraw = shandraw;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Object getTrayectory() {
        return trayectory;
    }

    public void setTrayectory(Object trayectory) {
        this.trayectory = trayectory;
    }

    public Object getCostPerPresentation() {
        return costPerPresentation;
    }

    public void setCostPerPresentation(Object costPerPresentation) {
        this.costPerPresentation = costPerPresentation;
    }

    public Object getInternationalConcerts() {
        return internationalConcerts;
    }

    public void setInternationalConcerts(Object internationalConcerts) {
        this.internationalConcerts = internationalConcerts;
    }

    public Object getConcertsPerYear() {
        return concertsPerYear;
    }

    public void setConcertsPerYear(Object concertsPerYear) {
        this.concertsPerYear = concertsPerYear;
    }

    public Object getAttendancePerConcert() {
        return attendancePerConcert;
    }

    public void setAttendancePerConcert(Object attendancePerConcert) {
        this.attendancePerConcert = attendancePerConcert;
    }

    public Object getSocialMediaFollowUp() {
        return socialMediaFollowUp;
    }

    public void setSocialMediaFollowUp(Object socialMediaFollowUp) {
        this.socialMediaFollowUp = socialMediaFollowUp;
    }

    public String getLiveVideo() {
        return liveVideo;
    }

    public void setLiveVideo(String liveVideo) {
        this.liveVideo = liveVideo;
    }

    public String getStudioVideo() {
        return studioVideo;
    }

    public void setStudioVideo(String studioVideo) {
        this.studioVideo = studioVideo;
    }

    public String getPressKit() {
        return pressKit;
    }

    public void setPressKit(String pressKit) {
        this.pressKit = pressKit;
    }

    public Object getLevel() {
        return level;
    }

    public void setLevel(Object level) {
        this.level = level;
    }

    public Object getSublevel() {
        return sublevel;
    }

    public void setSublevel(Object sublevel) {
        this.sublevel = sublevel;
    }

    public Object getCompleteness() {
        return completeness;
    }

    public void setCompleteness(Object completeness) {
        this.completeness = completeness;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getStep1() {
        return step1;
    }

    public void setStep1(String step1) {
        this.step1 = step1;
    }

    public String getStep2() {
        return step2;
    }

    public void setStep2(String step2) {
        this.step2 = step2;
    }

    public String getStep3() {
        return step3;
    }

    public void setStep3(String step3) {
        this.step3 = step3;
    }

    @Override
    public Class findDomainClass() { return Artist.class; }

    @Override
    public String toString() {
        return "{" +
                "bandName='" + bandName + '\'' +
                ", email='" + email + '\'' +
                ", shandraw='" + shandraw + '\'' +
                ", photo='" + photo + '\'' +
                ", trayectory=" + trayectory +
                ", costPerPresentation=" + costPerPresentation +
                ", internationalConcerts=" + internationalConcerts +
                ", concertsPerYear=" + concertsPerYear +
                ", attendancePerConcert=" + attendancePerConcert +
                ", socialMediaFollowUp=" + socialMediaFollowUp +
                ", liveVideo='" + liveVideo + '\'' +
                ", studioVideo='" + studioVideo + '\'' +
                ", pressKit='" + pressKit + '\'' +
                ", level='" + level + '\'' +
                ", sublevel=" + sublevel +
                ", completeness=" + completeness +
                ", review='" + review + '\'' +
                ", step1='" + step1 + '\'' +
                ", step2='" + step2 + '\'' +
                ", step3='" + step3 + '\'' +
                '}';
    }
}
