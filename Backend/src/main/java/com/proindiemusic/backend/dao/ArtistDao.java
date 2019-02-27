package com.proindiemusic.backend.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proindiemusic.backend.domain.Artist;
import com.proindiemusic.backend.pojo.templates.DaoTemplate;

import com.cloudant.client.api.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ArtistDao extends DaoTemplate<Artist> {

    private Database db;

    @Autowired
    public ArtistDao(Database db) {
        super(db);
    }

    @Override
    public Class<Artist> domainClass() {
        return Artist.class;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

}