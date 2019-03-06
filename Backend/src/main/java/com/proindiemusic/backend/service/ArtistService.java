package com.proindiemusic.backend.service;

import com.proindiemusic.backend.domain.Artist;
import com.proindiemusic.backend.pojo.annotations.Password;
import com.proindiemusic.backend.pojo.templates.ServiceTemplate;
import com.proindiemusic.backend.dao.ArtistDao;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class ArtistService extends ServiceTemplate<Artist> {

    @Autowired
    ArtistDao artistDao;

    @Override
    public Class<Artist> domainClass() {
        return Artist.class;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public Optional<Artist> getByUser(String user) {
        return Objects.requireNonNull(artistDao.getByUser(user));
    }

}