package com.proindiemusic.backend.domain;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.proindiemusic.backend.pojo.annotations.Administrator;
import com.proindiemusic.backend.pojo.annotations.Email;


@SuppressWarnings("ALL")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Artist extends Entity {

    private String bandName;
    @Email
    private String email;
    private String shandraw;
    private String photo;
    private Float trayectory;
    private Float costPerPresentation;
    private Integer internationalConcerts;
    private Integer concertsPerYear;
    private Float attendancePerConcert;
    private Float socialMediaFollowUp;
    private String liveVideo;
    private String studioVideo;
    private String pressKit;

    @Administrator
    private String level;
    @Administrator
    private Integer sublevel;
    @Administrator
    private Float completeness;
    @Administrator
    private String review;
    @Administrator
    private String step1;
    @Administrator
    private String step2;
    @Administrator
    private String step3;


    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
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

    public Float getTrayectory() {
        return trayectory;
    }

    public void setTrayectory(Float trayectory) {
        this.trayectory = trayectory;
    }

    public Float getCostPerPresentation() {
        return costPerPresentation;
    }

    public void setCostPerPresentation(Float costPerPresentation) {
        this.costPerPresentation = costPerPresentation;
    }

    public Integer getInternationalConcerts() {
        return internationalConcerts;
    }

    public void setInternationalConcerts(Integer internationalConcerts) {
        this.internationalConcerts = internationalConcerts;
    }

    public Integer getConcertsPerYear() {
        return concertsPerYear;
    }

    public void setConcertsPerYear(Integer concertsPerYear) {
        this.concertsPerYear = concertsPerYear;
    }

    public Float getAttendancePerConcert() {
        return attendancePerConcert;
    }

    public void setAttendancePerConcert(Float attendancePerConcert) {
        this.attendancePerConcert = attendancePerConcert;
    }

    public Float getSocialMediaFollowUp() {
        return socialMediaFollowUp;
    }

    public void setSocialMediaFollowUp(Float socialMediaFollowUp) {
        this.socialMediaFollowUp = socialMediaFollowUp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Integer getSublevel() {
        return sublevel;
    }

    public void setSublevel(Integer sublevel) {
        this.sublevel = sublevel;
    }

    public Float getCompleteness() {
        return completeness;
    }

    public void setCompleteness(Float completeness) {
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
