package com.proindiemusic.backend;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Document;
import com.cloudant.client.api.query.JsonIndex;
import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import com.cloudant.client.api.query.Sort;
import com.cloudant.client.api.views.Key;
import com.cloudant.client.api.views.ViewResponse;
import com.google.gson.JsonObject;
import com.proindiemusic.backend.config.CloudantConfigurator;
import com.proindiemusic.backend.domain.Media;
import com.proindiemusic.backend.domain.Prueba;
import com.proindiemusic.backend.domain.User;
import com.proindiemusic.backend.pojo.templates.DaoTemplate;
import org.apache.commons.beanutils.PropertyUtils;
import org.eclipse.jetty.util.IO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.cloudant.client.api.query.Expression.eq;
import static com.cloudant.client.api.query.Expression.gt;
import static com.cloudant.client.api.query.Operation.and;


@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties(CloudantConfigurator.class)
public class BackendApplicationTests {

    @Autowired
    private CloudantConfigurator config;

    private DaoTemplate<Prueba> entityDaoTemplate;

    private Database db;
    private CloudantClient account;

    @Before
    public void setUp() throws Exception {


        account = ClientBuilder
                .url(config.getUrl())
                .username(config.getUsername())
                .password(config.getPassword()).build();


        // create the movies-demo db for our index tests
        com.cloudant.client.api.Replication r = account.replication();
        r.source("https://clientlibs-test.cloudant.com/movies-demo");
        r.createTarget(true);
        r.target("https://1c541747-e0ee-4810-a698-11098c32de37-bluemix:5d3f7d832030e0b8f9ebfd6981c55515a6706ab5e0206d6a577077ea70844b01@1c541747-e0ee-4810-a698-11098c32de37-bluemix.cloudantnosqldb.appdomain.cloud"+"/"+"movies-demo");
        r.trigger();

        db = account.database("proindiemusic", true);

        if(db.listIndexes().jsonIndexes().size() > 0) {
            //Create indexes
            db.createIndex(JsonIndex.builder().name("ArtistUuid")
                    .designDocument("ArtistUuid")
                    .asc("artistUuid")
                    .definition());
        }
    }

    @Test
    public void testView() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, SQLIntegrityConstraintViolationException, IllegalAccessException {

        List<HashMap> result = db.getViewRequestBuilder("groupType", "type-group").newRequest(Key
                .Type.STRING, Object.class).keys("prueba").reduce(false).includeDocs(true).build().getResponse().getDocsAs(HashMap.class);

        db.listIndexes().allIndexes();

        System.out.println(result);

    }

    @Test
    public void uploadCredentials() throws IOException{
        QueryResult<Media> movies = db.query(new QueryBuilder(eq("artistUuid", "f5463dca-a7cf-4a25-88e5-8ad74fe8005c")).
                        useIndex("ArtistUuid", "artistUuid").
                        limit(1).
                        build(),
                Media.class);

        System.out.println(movies);
    }

    @Test
    public void contextLoads() throws IOException, InterruptedException, InvocationTargetException, NoSuchMethodException, SQLIntegrityConstraintViolationException, IllegalAccessException {

        entityDaoTemplate = new DaoTemplate<Prueba>(db) {
            @Override
            public Class<Prueba> domainClass() {
                return Prueba.class;
            }
        };

        Prueba entidad = new Prueba();

        entidad.setDateCreated(new Date());
        entidad.setDateModified(new Date());
        entidad.setStatus(true);

    }

}
