package com.proindiemusic.backend.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proindiemusic.backend.domain.Media;
import com.proindiemusic.backend.pojo.templates.DaoTemplate;

import com.cloudant.client.api.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MediaDao extends DaoTemplate<Media> {

    private Database db;

    @Autowired
    public MediaDao(Database db) {
        super(db);
    }

    @Override
    public Class<Media> domainClass() {
        return Media.class;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

}