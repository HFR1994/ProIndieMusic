package com.proindiemusic.backend.config;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ApplicationContext appCtx;

    @PostConstruct
    public void setup() {

        log.info("Rest classes found:");
        Map<String,Object> beans = appCtx.getBeansWithAnnotation(Path.class);
        for (Object o : beans.values()) {
            log.info(" -> " + o.getClass().getName());
            register(o);
        }
    }

}