package com.proindiemusic.backend.service;

import com.proindiemusic.backend.domain.International;
import com.proindiemusic.backend.pojo.templates.ServiceTemplate;
import com.proindiemusic.backend.dao.InternationalDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class InternationalService extends ServiceTemplate<International> {

    @Autowired
    InternationalDao internationalDao;

    @Override
    public Class<International> domainClass() {
        return International.class;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}