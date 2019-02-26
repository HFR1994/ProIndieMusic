package com.proindiemusic.backend.pojo.types;

import jnr.ffi.annotations.In;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

public class Primitive {
    private final static Set<Class<?>> DECIMALS;
    private final static Set<Class<?>> INTEGERS;
    private final static Set<Class<?>> BOOLEANS;
    private final static Set<Class<?>> DATES;

    private static SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    private static SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");

    static {
        Set<Class<?>> s = new HashSet<>();
        s.add(float.class);
        s.add(double.class);
        s.add(Float.class);
        s.add(Double.class);
        DECIMALS = s;
    }

    static {
        Set<Class<?>> s = new HashSet<>();
        s.add(short.class);
        s.add(int.class);
        s.add(long.class);
        s.add(Short.class);
        s.add(Integer.class);
        s.add(Long.class);
        INTEGERS = s;
    }

    static {
        Set<Class<?>> s = new HashSet<>();
        s.add(boolean.class);
        s.add(Boolean.class);
        BOOLEANS = s;
    }

    static {
        Set<Class<?>> s = new HashSet<>();
        s.add(java.util.Date.class);
        s.add(java.sql.Date.class);
        DATES = s;
    }
    
    public static Object getType(Class<?> type, Object value) throws ParseException {

        if(float.class.equals(type) || Float.class.equals(type)){
            return Float.parseFloat(value.toString());
        }else if(double.class.equals(type) || Double.class.equals(type)){
            return Double.parseDouble(value.toString());
        }else if(short.class.equals(type) || Short.class.equals(type)){
            return Short.parseShort(value.toString());
        }else if(int.class.equals(type) || Integer.class.equals(type)){
            return Integer.parseInt(value.toString());
        }else if(long.class.equals(type) || Long.class.equals(type)){
            return Long.parseLong(value.toString());
        }else if(boolean.class.equals(type) || Boolean.class.equals(type)){
            return Boolean.parseBoolean(value.toString());
        }else if(java.util.Date.class.equals(type) || java.sql.Date.class.equals(type)){
            return date.parse(value.toString());
        }else if(java.sql.Timestamp.class.equals(type)){
            return dateTime.parse(value.toString());
        }else{
            return value;
        }
    }

    public static String getDBName(String val){
        return StringUtils.lowerCase(StringUtils.join(
                StringUtils.splitByCharacterTypeCamelCase(val),
                '_'
        ));
    }


}
