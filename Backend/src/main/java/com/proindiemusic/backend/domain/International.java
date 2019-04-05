package com.proindiemusic.backend.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.proindiemusic.backend.pojo.annotations.Date;
import com.proindiemusic.backend.pojo.annotations.DateTime;


@SuppressWarnings("ALL")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
public class International extends Entity {

    private String country;
    private String city;

    private Boolean addToCount;

    @DateTime
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date date;

    private Number profit;
    private String artistUuid;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Boolean getAddToCount() {
        return addToCount;
    }

    public void setAddToCount(Boolean addToCount) {
        this.addToCount = addToCount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Number getProfit() {
        return profit;
    }

    public void setProfit(Number profit) {
        this.profit = profit;
    }

    public String getArtistUuid() {
        return artistUuid;
    }

    public void setArtistUuid(String artistUuid) {
        this.artistUuid = artistUuid;
    }

    @Override
    public Class findDomainClass() { return International.class; }
}
