package tech.aabo.celulascontentas.oauth.pojo;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tech.aabo.celulascontentas.oauth.dao.AuthorizationDao;
import tech.aabo.celulascontentas.oauth.domain.Client;
import tech.aabo.celulascontentas.oauth.domain.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Duplicates")
@Service
public class CommonTools {

    private static final Logger logger = LoggerFactory.getLogger(CommonTools.class);
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss zz");

    @Autowired
    private AuthorizationDao authorizationDao;

    @Autowired
    private ResourceLoader resourceLoader;

    public Client validateCredentials(HttpServletResponse response, String client_id, String client_secret){
        if(client_id == null){
            setResponse(response, "Missing client_id parameter", HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }else if(client_secret == null){
            setResponse(response, "Missing client_secret parameter", HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }else{
            Client client = authorizationDao.getClientDao().getCredentialValidation(client_id, client_secret).getData();
            if(client == null){
                setResponse(response,"Invalid client_id / client_secret parameter(s)", HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }else{
                return client;
            }
        }
    }

    public Client validateCredentials(HttpServletResponse response, String client_id){
        if(client_id == null){
            setResponse(response, "Missing client_id parameter", HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }else{
            Client client = authorizationDao.getClientDao().getCredentialValidation(client_id).getData();
            if(client == null){
                setResponse(response,"Invalid client_id parameter", HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }else{
                return client;
            }
        }
    }

    public Client validateState(HttpServletResponse response, String state){
        if(state == null){
            setResponse(response, "Missing state parameter", HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }else{
            String uuid = authorizationDao.getStateValidation(state).getData();
            Client client = authorizationDao.getClientDao().getClientByUuid(uuid).getData();
            if(client == null){
                setResponse(response,"Invalid state parameter", HttpServletResponse.SC_BAD_REQUEST);
                return null;
            }else{
                return client;
            }
        }
    }

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
            values.put("payload", JSONObject.NULL);

            logger.info(values.toString());

            out.print(values.toString());
            out.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void setResponse(HttpServletResponse response, String msg, Integer status, HashMap payload){
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
            values.put("payload", new JSONObject(payload));

            out.print(values.toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
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

    public static void printSmt(String sql, Object ...args){

        Integer index;

        sql = sql.replaceAll("\n","");

        for(Object arg: args){
            index = sql.indexOf("?");
            try {
                sql = sql.substring(0, index)+ "'" + arg + "'" + sql.substring(index + 1);
            }catch(Exception e){
                sql = sql.substring(0, index)+ arg + sql.substring(index + 1);
            }
        }

        logger.info(sql);
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

    public GoogleClientSecrets loadSecret(){
        String CLIENT_SECRET_FILE = "classpath:client_secret.json";

        try{
            InputStream file = resourceLoader.getResource(CLIENT_SECRET_FILE).getInputStream();
            return GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new InputStreamReader(file));
        }catch (Exception e){
            e.printStackTrace();
            CLIENT_SECRET_FILE = "client_secret.json";
            ClassPathResource resource = new ClassPathResource(CLIENT_SECRET_FILE);
            try {
                return GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), new FileReader(resource.getFile()));
            } catch (IOException e1) {
                return null;
            }
        }
    }

    public static void printMap(Map<String, String[]> query){
        for (Map.Entry<String, String[]> entry : query.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + Arrays.toString(entry.getValue()));
        }
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

    public static Result<String> getErrorInValidation(String text, String b){
        Result<String> res = new Result<>();
        res.setData(b);
        text = checkCodes(text);
        if(text != null){
            res.setMessage(text);
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }else{
            res.setMessage("Internal Server Error: Contact Administrator");
            res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }

    public static Result<Client> getErrorInValidation(String text, Client b){
        Result<Client> res = new Result<>();
        res.setData(b);
        text = checkCodes(text);
        if(text != null){
            res.setMessage(text);
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }else{
            res.setMessage("Internal Server Error: Contact Administrator");
            res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }

    public static Result<User> getErrorInValidation(String text, User b){
        Result<User> res = new Result<>();
        res.setData(b);
        text = checkCodes(text);
        if(text != null){
            res.setMessage(text);
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }else{
            res.setMessage("Internal Server Error: Contact Administrator");
            res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }

    public static Result<Boolean> getErrorInValidation(String text, Boolean b){
        Result<Boolean> res = new Result<>();
        res.setData(b);
        text = checkCodes(text);
        if(text != null){
            res.setMessage(text);
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }else{
            res.setMessage("Internal Server Error: Contact Administrator");
            res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }
    
    private static String checkCodes(String text){
        if(text.contains("CONSTRAINT")){
            int type = text.indexOf("CONSTRAINT");
            text = text.substring(type+12);
            text = text.substring(0, text.indexOf("`"));
            switch (text){
                case "authorization_client_fk":
                    return "Invalid authorization code";
                case "token_client_fk":
                    return "Invalid client credentials";
                case "token_google_fk":
                    return "Invalid google credentials";
                case "token_user_fk":
                    return "Invalid user credentials";
                case "authorization_redirect_fk":
                    return "Invalid redirect url";
                default:
                    logger.info(text);
                    return null;
            }
        }else if(text.contains("for key")){
            int type = text.indexOf("for key");
            text = text.substring(type+9,text.length()-1);
            switch (text){
                case "oauth_authorization_key":
                    return "Duplicate value on authorization code";
                case "oauth_email_key":
                    return "Duplicate value on email";
                case "oauth_uuid_key":
                    return "Duplicate value on uuid";
                case "oauth_gAccess_key":
                    return "Duplicate value on Google access_token";
                case "oauth_access_key":
                    return "Duplicate value on system access_token";
                default:
                    logger.info(text);
                    return null;
            }
        }
        logger.info(text);
        return null;
    }


    public static GoogleCredential createCredentialWithRefreshToken(HttpTransport transport,
                                                                    JsonFactory jsonFactory,
                                                                    GoogleClientSecrets clientSecrets,
                                                                    TokenResponse tokenResponse) {
        return new GoogleCredential.Builder().setTransport(transport)
                .setJsonFactory(jsonFactory)
                .setClientSecrets(clientSecrets)
                .build()
                .setFromTokenResponse(tokenResponse);
    }

    public static HashMap<String, String> getQueryMap(String query)
    {
        try {
            String[] params = query.split("&");
            HashMap<String, String> map = new HashMap<>();
            for (String param : params) {
                try {
                    String name = param.split("=")[0];
                    String value = param.split("=")[1];
                    map.put(name, value);
                }catch(Exception e ){
                    logger.debug("No value in param");
                }
            }
            return map;
        }catch (NullPointerException e){
            return new HashMap<>();
        }
    }

    public static HashMap<String, Cookie> getCookieMap(Cookie[] cookies)
    {
        try {
            HashMap<String, Cookie> map = new HashMap<>();
            for(Cookie cookie : cookies){
                map.put(cookie.getName(), cookie);
            }
            return map;
        }catch (NullPointerException e){
            return new HashMap<>();
        }
    }

    public static String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

}
