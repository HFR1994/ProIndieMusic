package com.proindiemusic.backend.pojo;

import com.proindiemusic.backend.domain.User;
import com.proindiemusic.backend.pojo.annotations.Administrator;
import com.proindiemusic.backend.pojo.annotations.Password;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Result {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Integer code;

    private String message;

    private Boolean various;

    private List<Map<String, Object>> data;

    private byte[] pdf;

    public static final Integer OK = 200;

    public static final Integer CREATED = 201;

    public static final Integer NO_CONTENT = 204;

    public static final Integer NOT_MODIFIED = 304;

    public static final Integer BAD_REQUEST = 400;

    public static final Integer UNAUTHORIZED = 401;

    public static final Integer FORBIDDEN = 403;

    public static final Integer NOT_FOUND = 404;

    public static final Integer GONE = 410;

    public static final Integer INTERNAL_SERVER_ERROR = 500;

    public static final Integer SERVICE_UNAVAILABLE = 503;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getVarious() {
        return various;
    }

    public void setVarious(Boolean various) {
        this.various = various;
    }

    public HashMap getData(boolean various) {

        HashMap<String, Object> value = new HashMap<>();

        value.put("message", getMessage());

        if (data == null || data.isEmpty()){
            if(various){
                value.put("data", new ArrayList<>());
            }else{
                value.put("data", null);
            }
        }else if(data.size() == 1 && getVarious()==null){
            value.put("data", data.get(0));
        }else{
            value.put("data", data);
        }

        return value;
    }

    public <T> HashMap getData(T klazz) {

        HashMap<String, Object> value = new HashMap<>();

        value.put("response", getMessage());

        HashMap<String, Object> datos = new HashMap<>();

        if (klazz != null) {
            for (Field field : klazz.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                String name = field.getName().substring(0, 1).toLowerCase() + field.getName().substring(1);
                Object data;
                try {
                    data = PropertyUtils.getProperty(klazz, field.getName());

                    if(field.getAnnotation(Password.class) == null){
                        datos.put(name, data);
                    }

                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            try {
                String uuid = String.valueOf(PropertyUtils.getProperty(klazz, "uuid"));
                Boolean status = (Boolean) PropertyUtils.getProperty(klazz, "status");
                HashMap<String, Object> additionalProps = (HashMap) PropertyUtils.getProperty(klazz, "additionalProperties");

                datos.putAll(additionalProps);
                datos.put("uuid",uuid);
                datos.put("status", status);

            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }

            value.put("data", datos);
        } else {
            value.put("data", null);
        }

        return value;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public void setData(HashMap data) {
        List<Map<String, Object>> lista = new ArrayList<>();
        lista.add(data);
        this.data = lista;
    }


    public byte[] getPdf() {
        return pdf;
    }

    public void setPdf(byte[] pdf) {
        this.pdf = pdf;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
