package com.proindiemusic.backend.pojo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proindiemusic.backend.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Duplicates")
@Service
public class CommonTools {

    private static final Logger logger = LoggerFactory.getLogger(CommonTools.class);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss zz");

    public static void setResponse(HttpServletResponse response, String msg, Integer status ){
        try {

            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.setStatus(status);
            PrintWriter out = response.getWriter();

            //create Json Object
            JSONObject values = new JSONObject();

            values.put("timestamp", String.valueOf(Timestamp.from(Instant.now())));
            values.put("code", status);
            values.put("message", msg);

            out.print(values.toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public static Field getField(Class<?> type, String key) {

        try {
            return type.getDeclaredField(key);
        } catch (NoSuchFieldException e) {
            if (type.getSuperclass() != null) {
                return getField(type.getSuperclass(), key);
            }
        }

        return null;
    }

    public static HashMap<String, String> getQueryMap(String query)
    {
        try {
            String[] params = query.split("&");
            HashMap<String, String> map = new HashMap<>();
            for (String param : params) {
                String name = param.split("=")[0];
                String value = param.split("=")[1];
                map.put(name, value);
            }
            return map;
        }catch (NullPointerException e){
            return new HashMap<>();
        }
    }

    public static Boolean eraseCookies(HttpServletResponse response, Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
            return true;
        } else {
            return false;
        }
    }

    public static String getCookieValue(Cookie[] cookies, String name){
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    public static String transformName(HttpServletRequest request, Integer type){

        switch(type) {
            case 0:
                if(request.getServerName().contains("localhost")) {
                    return request.getScheme() + "://" +   // "http" + "://
                            request.getServerName() +       // "myhost"
                            ":" +                           // ":"
                            request.getServerPort() +       // "8080"
                            request.getRequestURI() +       // "/people"
                            "?" +                           // "?"
                            request.getQueryString();       // "lastname=Fox&age=30"
                }else{
                    return  "https://" +   // "http" + "://
                            request.getServerName() +       // "myhost"
                            ":" +                           // ":"
                            request.getServerPort() +       // "8080"
                            request.getRequestURI() +       // "/people"
                            "?" +                           // "?"
                            request.getQueryString();       // "lastname=Fox&age=30"
                }
            case 1:
                if(request.getServerName().contains("localhost")) {
                    return request.getScheme() + "://" +    // "http" + "://
                            request.getServerName() +       // "myhost"
                            ":" +                           // ":"
                            request.getServerPort() +       // "8080"
                            request.getRequestURI();
                }else{
                    return  "https://" +   // "http" + "://
                            request.getServerName() +       // "myhost"
                            ":" +                           // ":"
                            request.getServerPort() +       // "8080"
                            request.getRequestURI();
                }
            case 2:
                if(request.getServerName().contains("localhost")) {
                    return request.getScheme() + "://" +   // "http" + "://
                            request.getServerName() +       // "myhost"
                            ":" +                           // ":"
                            request.getServerPort();
                }else{
                    return  "https://" +   // "http" + "://
                            request.getServerName() +       // "myhost"
                            ":" +                           // ":"
                            request.getServerPort();
                }
            default:
                if(request.getServerName().contains("localhost")) {
                    return request.getScheme() + "://" +   // "http" + "://
                            "127.0.0.1" +       // "myhost"
                            ":" +                           // ":"
                            request.getServerPort() +       // "8080"
                            request.getRequestURI();        // "/people"

                }else{
                    return "https://" +   // "http" + "://
                            request.getServerName() +       // "myhost"
                            ":" +                           // ":"
                            request.getServerPort() +       // "8080"
                            request.getRequestURI();       // "/people"
                }
        }
    }

    public static User transformPrincipal(HttpServletRequest httpRequest){
        Authentication user = (Authentication) httpRequest.getUserPrincipal();
        return (User) user.getPrincipal();
    }

    public static Map<String, Object> mapPOJO(Class<?> data){
        // Create ObjectMapper instance
        ObjectMapper mapper = new ObjectMapper();

        // Converting POJO to Map
        return mapper.convertValue(data, new TypeReference<Map<String, Object>>() {});

    }

    public static void printSmt(String sql, Object[] objects){

        Integer index;

        sql = sql.replaceAll("\n","");

        for(Object arg: objects){
            index = sql.indexOf("?");
            try {
                sql = sql.substring(0, index)+ "'" + arg + "'" + sql.substring(index + 1);
            }catch(Exception e){
                sql = sql.substring(0, index)+ arg + sql.substring(index + 1);
            }
        }

        logger.info(sql);
    }

}
