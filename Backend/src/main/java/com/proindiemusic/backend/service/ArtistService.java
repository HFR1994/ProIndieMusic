package com.proindiemusic.backend.service;

import com.proindiemusic.backend.domain.Artist;
import com.proindiemusic.backend.pojo.templates.ServiceTemplate;
import com.proindiemusic.backend.dao.ArtistDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ArtistService extends ServiceTemplate<Artist> {

    @Autowired
    ArtistDao artistDao;

    @Override
    public Class<Artist> domainClass() {
        return Artist.class;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}