package com.proindiemusic.backend.pojo.templates;

import com.proindiemusic.backend.domain.Entity;
import com.proindiemusic.backend.domain.Media;
import com.proindiemusic.backend.domain.User;
import com.proindiemusic.backend.pojo.CommonTools;
import com.proindiemusic.backend.pojo.Result;
import com.proindiemusic.backend.pojo.annotations.Password;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@SuppressWarnings({"Duplicates", "unchecked"})
public abstract class ServiceTemplate<T> {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private List<DaoTemplate<T>> daos;

    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", Pattern.CASE_INSENSITIVE);

    private final Pattern VALID_PASSWORD_VALIDATION =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^!&+=])(?=\\S+$).{8,}$");

    private final Pattern VALID_DATE_VALIDATION =
            Pattern.compile("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))");

    private final Pattern VALID_DATETIME_VALIDATION =
            Pattern.compile("^\\d{4}-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d(\\.\\d+)?(([+-]\\d\\d:\\d\\d)|Z)?$", Pattern.CASE_INSENSITIVE);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public abstract Class<T> domainClass();

    public DaoTemplate<T> getDao(){
        for(DaoTemplate<T> dao: daos){
            if(dao.domainClass().equals(domainClass())){
                return dao;
            }
        }
        return null;
    }

    public Result getAll() throws IOException {

        Result result = new Result();
        Optional<List<T>> val = Objects.requireNonNull(getDao()).getAll();

        if(val.isPresent()){
            List<T> results = val.get();
            for(T klazz : results) {
                Set<Field> fields = findFields(klazz.getClass(), Password.class);
                for (Field field : fields) {
                    field.setAccessible(true);
                    try {
                        PropertyUtils.setSimpleProperty(klazz, field.getName(), null);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                        result.setCode(Result.INTERNAL_SERVER_ERROR);
                        result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
                        return result;
                    }
                }
            }

            List<Map<String, Object>> list = new ArrayList<>();

            for(T klazz : results){
                list.add(CommonTools.mapPOJO((Entity) klazz));
            }

            result.setData(list);
            result.setCode(Result.CREATED);
            result.setMessage("¡Genial! Se cargaron exitosamente los datos");
        } else {
            result.setCode(Result.INTERNAL_SERVER_ERROR);
            result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
        }

        return result;

    }

    public Result getAll(String artist) throws IOException {

        Result result = new Result();
        Optional<List<T>> val = Objects.requireNonNull(getDao()).getAll(artist);

        if(val.isPresent()){
            List<T> results = val.get();
            for(T klazz : results) {
                Set<Field> fields = findFields(klazz.getClass(), Password.class);
                for (Field field : fields) {
                    field.setAccessible(true);
                    try {
                        PropertyUtils.setSimpleProperty(klazz, field.getName(), null);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                        result.setCode(Result.INTERNAL_SERVER_ERROR);
                        result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
                        return result;
                    }
                }
            }

            List<Map<String, Object>> list = new ArrayList<>();

            for(T klazz : results){
                list.add(CommonTools.mapPOJO((Entity) klazz));
            }

            result.setData(list);
            result.setCode(Result.CREATED);
            result.setMessage("¡Genial! Se cargaron exitosamente los datos");
        } else {
            result.setCode(Result.INTERNAL_SERVER_ERROR);
            result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
        }

        return result;

    }

    public Optional<T> getByUuid(String uuid) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException{

        Optional<T> val = Objects.requireNonNull(getDao()).getByUuid(uuid);

        if(val.isPresent()) {
            T clase = val.get();

            Set<Field> fields = findFields(clase.getClass(), Password.class);
            for (Field field : fields) {
                field.setAccessible(true);
                PropertyUtils.setSimpleProperty(clase, field.getName(), null);
            }
        }

        return val;

    }

    public Optional<T> getByUuid(String artist, String uuid) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException{

        Optional<T> val = Objects.requireNonNull(getDao()).getByUuid(artist, uuid);

        if(val.isPresent()) {
            T clase = val.get();

            Set<Field> fields = findFields(clase.getClass(), Password.class);
            for (Field field : fields) {
                field.setAccessible(true);
                PropertyUtils.setSimpleProperty(clase, field.getName(), null);
            }
        }

        return val;

    }

    private HashMap<String, HashMap<String, Object>> checkData(T klazz, boolean b, User user) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {

        HashMap<String, HashMap<String, Object>> datos = new HashMap<>();
        Field[] fields = klazz.getClass().getDeclaredFields();
        Optional<T> var = Optional.empty();

        if(klazz.getClass() != null) {

            if(!b) {
                String uuid = PropertyUtils.getProperty(klazz, "uuid").toString();
                var = getByUuid(uuid);
            }

            for (Field field : fields) {
                field.setAccessible(true);

                HashMap<String, Object> val = new HashMap<>();
                String name = field.getName().substring(0, 1).toLowerCase() + field.getName().substring(1);

                Object value;
                try {
                    value  = PropertyUtils.getProperty(klazz, field.getName());
                }catch (InvocationTargetException e){
                    PropertyUtils.setSimpleProperty(klazz, field.getName(), null);
                    value = null;
                }

                HashSet<String> error = new HashSet<>();

                if(value != null || field.getAnnotations().length != 0) {

                    boolean check = true;

                    if(var.isPresent()) {
                        String current = String.valueOf(PropertyUtils.getProperty(var.get(), field.getName()));
                        if (current.equals(String.valueOf(value))) {
                            check = false;
                        }
                    }

                    if(check) {
                        for (Annotation annotation : field.getAnnotations()) {
                            Matcher matcher;
                            switch (annotation.annotationType().getSimpleName()) {
                                case "Email":
                                    matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(value.toString());
                                    if (!matcher.find()) {
                                        error.add("No es valido");
                                    }
                                    break;
                                case "Password":
                                    matcher = VALID_PASSWORD_VALIDATION.matcher(value.toString());
                                    if (!matcher.find()) {
                                        error.add("Debe:\n\tTener 8 Letras\n\tUna letra mayúscula\n\tUna letra minúscula\n\tUn caracter especial\n\tNo debe tener espacios o saltos de página");
                                    }
                                    break;
                                case "Date":
                                    matcher = VALID_DATE_VALIDATION.matcher(value.toString());
                                    if (matcher.find()) {
                                        error.add("Debe ir en formato yyyy-mm-dd");
                                    }
                                    break;
                                case "DateTime":
                                    matcher = VALID_DATETIME_VALIDATION.matcher(value.toString());
                                    if (matcher.find()) {
                                        error.add("Debe ir en formato ISO 8601");
                                    }
                                    break;
                                case "Administrator":
                                    if (!user.hasRole("ADMIN") || value != null) {
                                        error.add("El usuario "+user.getEmail()+" no tiene permisos de administrador");
                                    }
                                    break;
                                case "Required":
                                    if (value == null || value.toString().trim().length() == 0) {
                                        error.add("No debe estar vacio");
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    val.put("changed", check);
                }else{
                    if(var.isPresent()){
                        value = PropertyUtils.getProperty(var.get(), field.getName());
                        val.put("changed", false);
                    }else if(b) {
                        val.put("changed", true);
                    }else{
                        throw new NullPointerException("No existe ese uuid");
                    }
                }

                if(field.getAnnotation(Password.class) == null){
                    val.put("value", value);
                }else{
                    val.put("value", "");
                }

                val.put("error", error);
                datos.put(name, val);

            }

            HashMap<String, Object> properties = (HashMap<String, Object>) PropertyUtils.getProperty(klazz, "additionalProperties");

            if(var.isPresent()){
                HashMap<String, Object> current = (HashMap<String, Object>) PropertyUtils.getProperty(var.get(), "additionalProperties");

                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    HashMap<String, Object> val = new HashMap<>();
                    val.put("changed", current.get(entry.getKey()) == entry.getValue());
                    val.put("value", entry.getValue());
                    val.put("error", new HashSet<>());
                    datos.put(entry.getKey(), val);
                }

            }else{
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    HashMap<String, Object> val = new HashMap<>();
                    val.put("changed", true);
                    val.put("value", entry.getValue());
                    val.put("error", new HashSet<>());
                    datos.put(entry.getKey(), val);
                }
            }

        }
        return datos;
    }

    private void addNew(HashMap<String, HashMap<String, Object>> val, T klazz) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        String uuid = String.valueOf(PropertyUtils.getProperty(klazz, "uuid"));
        Boolean status = (Boolean) PropertyUtils.getProperty(klazz, "status");

        HashMap<String, Object> mapa = new HashMap<>();
        mapa.put("value", uuid);
        mapa.put("error", new HashSet<String>());
        mapa.put("changed", true);
        val.put("uuid",mapa);

        mapa = new HashMap<>();
        mapa.put("value", status);
        mapa.put("error", new HashSet<String>());
        mapa.put("changed", true);
        val.put("status",mapa);
    }


    public Result insert(HashMap<String, Object> data, User user){

        Result result = new Result();

        Optional<T> mapper = getDao().objectMapper(data);

        if(mapper.isPresent()) {

            T klazz = mapper.get();

            try {
                PropertyUtils.setSimpleProperty(klazz, "userAuth", user.getUuid());
                if (klazz.getClass() != null) {
                    HashMap<String, HashMap<String, Object>> datos;
                    try {
                        datos = checkData(klazz, true, user);
                    } catch (NullPointerException | NoSuchFieldException e) {
                        e.printStackTrace();
                        result.setCode(Result.BAD_REQUEST);
                        result.setMessage("¡Ups! No especificaste el uuid del registro");
                        return result;
                    }

                    int cuenta = 0;

                    for (HashMap<String, Object> entry : datos.values()) {
                        //noinspection unchecked
                        HashSet<String> value = (HashSet<String>) entry.get("error");
                        cuenta += value.size();
                    }

                    if (cuenta == 0) {
                        Optional<T> val;
                        val = Objects.requireNonNull(getDao()).insert(klazz);
                        if (val.isPresent()) {
                            addNew(datos, val.get());
                            result.setCode(Result.CREATED);
                            result.setMessage("¡Genial! Se agregaron exitosamente los datos");
                        } else {
                            result.setCode(Result.INTERNAL_SERVER_ERROR);
                            result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
                        }
                    } else {
                        result.setCode(Result.BAD_REQUEST);
                        result.setMessage("¡Ups! Hay uno o mas errores en tu formulario");
                    }

                    result.setData(datos);
                } else {
                    result.setCode(Result.BAD_REQUEST);
                    result.setMessage("¡Ups! El cuerpo del post no puede ir vacio");
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                result.setCode(Result.INTERNAL_SERVER_ERROR);
                result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
            }
        }else{
            result.setCode(Result.INTERNAL_SERVER_ERROR);
            result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
        }

        return result;
    }

    public Result delete(String uuid, User user){

        Result result = new Result();

        Optional<T> mapper = Objects.requireNonNull(getDao()).getByUuid(uuid);

        if(mapper.isPresent()) {

            T klazz = mapper.get();

            try {
                PropertyUtils.setSimpleProperty(klazz, "userAuth", user.getUuid());
                if(klazz.getClass() != null) {
                    Optional<Boolean> object;
                    try {
                        object = Objects.requireNonNull(getDao()).delete(klazz);
                    } catch (NullPointerException e) {
                        result.setCode(Result.BAD_REQUEST);
                        result.setMessage("¡Ups! No especificaste el uuid del registro");
                        return result;
                    }

                    if(object.isPresent()){
                        if(object.get()){
                            result.setCode(Result.OK);
                            result.setMessage("¡Genial! Se dio de baja exitosamente el registro");
                        }else{
                            result.setCode(Result.NOT_MODIFIED);
                            result.setMessage("¡Ups! No existe ese uuid de registro");
                        }
                    } else {
                        result.setCode(Result.INTERNAL_SERVER_ERROR);
                        result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
                    }

                }else{
                    result.setCode(Result.BAD_REQUEST);
                    result.setMessage("¡Ups! El cuerpo del post no puede ir vacio");
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                result.setCode(Result.INTERNAL_SERVER_ERROR);
                result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
            }
        }else{
            result.setCode(Result.INTERNAL_SERVER_ERROR);
            result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
        }

        return result;
    }

    public Result update(HashMap<String, Object> data, User user){

        Result result = new Result();

        Optional<T> mapper = getDao().objectMapper(data);

        if(mapper.isPresent()) {

            T klazz = mapper.get();


            try {
                PropertyUtils.setSimpleProperty(klazz, "userAuth", user.getUuid());

                if(klazz.getClass() != null) {

                    String uuid;

                    try {
                        uuid = PropertyUtils.getProperty(klazz, "uuid").toString();

                        Optional<T> current = Objects.requireNonNull(getDao()).getByUuid(uuid);

                        if (current.isPresent()) {

                            HashMap<String, HashMap<String, Object>> datos = checkData(klazz, false, user);

                            int cuenta = 0;

                            for (HashMap<String, Object> entry : datos.values()) {
                                //noinspection unchecked
                                HashSet<String> value = (HashSet<String>) entry.get("error");
                                cuenta += value.size();
                            }

                            if (klazz.getClass().equals(current.get())) {
                                result.setCode(Result.NOT_MODIFIED);
                                result.setMessage("¡Alerta! No hubo cambios en el contenido");
                            } else if (cuenta == 0) {
                                Optional<T> val = Objects.requireNonNull(getDao()).update(klazz);
                                if (val.isPresent()) {
                                    addNew(datos,val.get());
                                    result.setCode(Result.OK);
                                    result.setMessage("¡Genial! Se actualizaron exitosamente los datos");
                                } else {
                                    result.setCode(Result.INTERNAL_SERVER_ERROR);
                                    result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
                                }
                            } else {
                                result.setCode(Result.BAD_REQUEST);
                                result.setMessage("¡Ups! Hay uno o mas errores en tu formulario");
                            }

                            result.setData(datos);
                        }else{
                            result.setCode(Result.BAD_REQUEST);
                            result.setMessage("¡Ups! No existe ese uuid de registro");
                        }
                    } catch (NullPointerException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                        result.setCode(Result.BAD_REQUEST);
                        result.setMessage("¡Ups! No especificaste el uuid del registro");
                    } catch (NoSuchFieldException e) {
                        result.setCode(Result.BAD_REQUEST);
                        result.setMessage("¡Ups! Uno de los campos no pertenece al registro");
                    }
                }else{
                    result.setCode(Result.BAD_REQUEST);
                    result.setMessage("¡Ups! El cuerpo del post no puede ir vacio");
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
                result.setCode(Result.INTERNAL_SERVER_ERROR);
                result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
            }
        }else{
            result.setCode(Result.INTERNAL_SERVER_ERROR);
            result.setMessage("¡Ups! Hubo un error por favor contacta a tu administrador");
        }

        return result;
    }


    private static Set<Field> findFields(Class<?> classs, Class<? extends Annotation> ann) {
        Set<Field> set = new HashSet<>();
        Class<?> c = classs;
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (field.isAnnotationPresent(ann)) {
                    set.add(field);
                }
            }
            c = c.getSuperclass();
        }
        return set;
    }
}
