package com.proindiemusic.backend.service;

import com.proindiemusic.backend.domain.Media;
import com.proindiemusic.backend.pojo.templates.ServiceTemplate;
import com.proindiemusic.backend.dao.MediaDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MediaService extends ServiceTemplate<Media> {

    @Autowired
    MediaDao mediaDao;

    @Override
    public Class<Media> domainClass() {
        return Media.class;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
}