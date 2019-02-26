package com.proindiemusic.backend.config;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CloudantConfigurator.class)
public class InitializeDB {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private CloudantConfigurator config;

  @Bean
  public CloudantClient client() {
    ClientBuilder builder = ClientBuilder
            .url(config.getUrl())
            .username(config.getUsername())
            .password(config.getPassword());
    return builder.build();
  }

  @Bean
  public Database database(CloudantClient client) {
    logger.debug("Connecting to Cloudant DB");
    Database db = client.database(config.getDb(), true);
    logger.debug(db.info().toString());
    return db;
  }

}
