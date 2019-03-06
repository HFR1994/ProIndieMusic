package com.proindiemusic.backend.dao;

import com.cloudant.client.api.query.QueryBuilder;
import com.cloudant.client.api.query.QueryResult;
import com.cloudant.client.api.query.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proindiemusic.backend.domain.Artist;
import com.proindiemusic.backend.pojo.templates.DaoTemplate;

import com.cloudant.client.api.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.cloudant.client.api.query.Expression.eq;
import static com.cloudant.client.api.query.Operation.and;

@Repository
public class ArtistDao extends DaoTemplate<Artist> {

    @Autowired
    private Database db;

    public ArtistDao(Database db) {
        super(db);
    }

    @Override
    public Class<Artist> domainClass() {
        return Artist.class;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Optional<Artist> getByUser(String artist){

        QueryResult<HashMap> data = db.query(new QueryBuilder(and(
                eq("userAuth", artist),
                eq("table", "artist"))).
                        useIndex("DateModified", "DateModified").
                        sort(Sort.desc("dateModified")).
                        build(),
                HashMap.class);

        return super.objectMapper(data.getDocs().get(0));
    }


}