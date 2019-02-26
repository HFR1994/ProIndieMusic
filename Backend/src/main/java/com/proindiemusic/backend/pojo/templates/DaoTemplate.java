package com.proindiemusic.backend.pojo.templates;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.api.views.Key;
import com.proindiemusic.backend.domain.Entity;
import com.proindiemusic.backend.pojo.CommonTools;
import com.proindiemusic.backend.pojo.types.Primitive;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings({"Duplicates", "unchecked", "ConstantConditions"})
public abstract class DaoTemplate<T>{

    private static SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    private Database db;

    @Autowired
    public DaoTemplate(Database db) {
        this.db = db;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public abstract Class<T> domainClass();

    public Optional<T> getByUuid(String uuid){

        HashMap<String, Object> miclase = db.find(HashMap.class, uuid);
        return objectMapper(miclase);
    }

    public Optional<T> objectMapper(HashMap<String, Object> data){
        T clase = null;
        try {
            clase = domainClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        if (data == null | data.isEmpty()) {
            return Optional.empty();
        }

        for(Map.Entry<String, Object> cursor : data.entrySet()) {
            try {

                if(cursor.getKey().equals("_id")){
                    PropertyUtils.setSimpleProperty(clase, "uuid", cursor.getValue());
                }else {
                    Field current = CommonTools.getField(clase.getClass(), cursor.getKey());

                    if (current != null) {

                        if (current.getAnnotations().length == 0) {
                            PropertyUtils.setSimpleProperty(clase, cursor.getKey(), Primitive.getType(current.getType(), cursor.getValue()));
                        } else {
                            for (Annotation annotation : current.getAnnotations()) {
                                if (annotation.annotationType().getSimpleName().equals("Date")) {
                                    PropertyUtils.setSimpleProperty(clase, cursor.getKey(), Primitive.getType(Timestamp.class, cursor.getValue()));
                                    break;
                                } else if (annotation.annotationType().getSimpleName().equals("Date")) {
                                    PropertyUtils.setSimpleProperty(clase, cursor.getKey(), Primitive.getType(Date.class, cursor.getValue()));
                                    break;
                                } else {
                                    PropertyUtils.setSimpleProperty(clase, cursor.getKey(), current.getType());
                                }
                            }
                        }

                    } else {
                        ((Entity) clase).setProperty(cursor.getKey(), cursor.getValue());
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException | ParseException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return Optional.of(clase);
    }

    public Optional<List<T>> getAll() throws IOException {

        String value = Primitive.getDBName(domainClass().getSimpleName());

        List<HashMap> results = db.getViewRequestBuilder("groupType", "type-group").newRequest(Key
                .Type.STRING, Object.class).keys(value).reduce(false).includeDocs(true).build().getResponse().getDocsAs(HashMap.class);

        List<T> datos = new ArrayList<>();

        for(HashMap result : results){
            Optional<T> val = objectMapper(result);
            if(val.isPresent()){
                datos.add(val.get());
            }else{
                return Optional.empty();
            }
        }

        return Optional.of(datos);
    }


    public Optional<T> insert(T klazz) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        String value = Primitive.getDBName(klazz.getClass().getSimpleName());

        PropertyUtils.setSimpleProperty(klazz, "uuid",null);
        String user = PropertyUtils.getProperty(klazz, "_user").toString();

        String newUuid = UUID.randomUUID().toString();
        HashMap<String, Object> data = new HashMap<>();
        Field[] fields = klazz.getClass().getDeclaredFields();
        data.put("_id", newUuid);
        data.put("_user", user);
        data.put("type", value);

        for (Field field : fields) {
            field.setAccessible(true);

            String name = field.getName().substring(0, 1).toLowerCase() + field.getName().substring(1);

            if (name.equalsIgnoreCase("additionalProperties")) {
                data.putAll(((Entity) klazz).getProperties());
            } else {
                data.put(name, PropertyUtils.getProperty(klazz, name));
            }

        }
        data.put("status", true);
        data.put("dateCreated", dateTime.format(new Date()));
        data.put("dateModified", dateTime.format(new Date()));

        try {
            Response response = db.save(data);
            logger.debug("Insertando "+ value);
            if (response.getStatusCode() != 201){
                return Optional.empty();
            }else{
                return getByUuid(newUuid);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Boolean> delete(T klazz){

        String value = Primitive.getDBName(klazz.getClass().getSimpleName());
        ((Entity) klazz).setStatus(false);

        try {
            Optional<T> status = update(klazz);
            logger.debug("Borrando " + value);
            return Optional.of(status.isPresent());
        } catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<T> update(T klazz) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException{

        String value = Primitive.getDBName(klazz.getClass().getSimpleName());
        String uuid = PropertyUtils.getProperty(klazz, "uuid").toString();
        String rev = PropertyUtils.getProperty(klazz, "_rev").toString();
        String user = PropertyUtils.getProperty(klazz, "_user").toString();

        HashMap<String, Object> data = new HashMap<>();
        Field[] fields = klazz.getClass().getDeclaredFields();
        data.put("_id", uuid);
        data.put("_rev", rev);
        data.put("_user", user);
        data.put("type", value);

        for (Field field : fields) {
            field.setAccessible(true);

            String name = field.getName().substring(0, 1).toLowerCase() + field.getName().substring(1);

            if (name.equalsIgnoreCase("additionalProperties")) {
                data.putAll(((Entity) klazz).getProperties());
            } else {
                data.put(name, PropertyUtils.getProperty(klazz, name));
            }

        }
        data.put("dateModified", dateTime.format(new Date()));

        try {
            Response response = db.update(data);
            logger.debug("Estoy modificando "+ value);
            if (response.getStatusCode() != 201){
                return Optional.empty();
            }else{
                return getByUuid(uuid);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

}
