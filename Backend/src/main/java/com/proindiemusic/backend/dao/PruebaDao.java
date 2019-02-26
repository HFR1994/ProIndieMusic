package com.proindiemusic.backend.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proindiemusic.backend.domain.Prueba;
import com.proindiemusic.backend.pojo.templates.DaoTemplate;

import com.cloudant.client.api.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PruebaDao extends DaoTemplate<Prueba> {

    private Database db;

    @Autowired
    public PruebaDao(Database db) {
        super(db);
    }

    @Override
    public Class<Prueba> domainClass() {
        return Prueba.class;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

}