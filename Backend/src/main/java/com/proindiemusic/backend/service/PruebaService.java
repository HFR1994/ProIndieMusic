package com.proindiemusic.backend.service;

import com.proindiemusic.backend.domain.Prueba;
import com.proindiemusic.backend.pojo.templates.ServiceTemplate;
import com.proindiemusic.backend.dao.PruebaDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class PruebaService extends ServiceTemplate<Prueba> {

    @Autowired
    PruebaDao pruebaDao;

    @Override
    public Class<Prueba> domainClass() {
        return Prueba.class;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}