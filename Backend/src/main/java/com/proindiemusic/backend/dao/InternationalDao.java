package com.proindiemusic.backend.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proindiemusic.backend.domain.International;
import com.proindiemusic.backend.pojo.templates.DaoTemplate;

import com.cloudant.client.api.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class InternationalDao extends DaoTemplate<International> {

    @Autowired
    private Database db;

    public InternationalDao(Database db) {
        super(db);
    }

    @Override
    public Class<International> domainClass() {
        return International.class;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

}