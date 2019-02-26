package tech.aabo.celulascontentas.oauth.dao;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tech.aabo.celulascontentas.oauth.OauthApplication;
import tech.aabo.celulascontentas.oauth.domain.Client;
import tech.aabo.celulascontentas.oauth.domain.User;
import tech.aabo.celulascontentas.oauth.filter.GoogleOAuth2Filter;
import tech.aabo.celulascontentas.oauth.pojo.CommonTools;
import tech.aabo.celulascontentas.oauth.pojo.RandomString;
import tech.aabo.celulascontentas.oauth.pojo.Result;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import static java.lang.Math.toIntExact;

@SuppressWarnings({"SqlResolve", "Duplicates"})
@Repository
public class GoogleDao {

    @Autowired
    private JdbcTemplate jdbc;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    CommonTools commonTools;

    private Result<Boolean> disableGAccessByCredentials(String access, String refresh) {
        String smt = "UPDATE `oauth_google` SET `status`=? WHERE `gAccessToken`=? AND `gRefreshToken`=?";

        try {
            Result<Boolean> res = new Result<>();
            if(jdbc.update(smt, false, access, refresh) == 1){
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                res.setData(false);
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), false);
        }
    }

    private Result<Boolean> disableGAccessByRefresh(String refresh) {
        String smt = "UPDATE `oauth_google` SET `status`=? WHERE `gRefreshToken`=?";
        try {
            Result<Boolean> res = new Result<>();
            if(jdbc.update(smt, false, refresh) == 1){
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                res.setData(false);
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), false);
        }
    }

    public Result<Boolean> disableGAccessByUuid(String uuid) {
        String smt = "UPDATE `oauth_google` SET `status`=? WHERE `uuid`=?";
        try {
            Result<Boolean> res = new Result<>();
            if(jdbc.update(smt, false, uuid) == 1){
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                res.setData(false);
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), false);
        }
    }

    public Result<Boolean> revokeGAccess(String refresh) {

        try {
            URL url = new URL("https://accounts.google.com/o/oauth2/revoke");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            Map<String, String> parameters = new HashMap<>();
            parameters.put("token", refresh);

            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(CommonTools.getParamsString(parameters));
            out.flush();
            out.close();

            con.disconnect();

            return disableGAccessByRefresh(refresh);

        } catch (IOException e) {
            Result<Boolean> res = new Result<>();
            res.setData(false);
            res.setMessage("Internal Server Error: couldn't revoke access from Google");
            res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return res;
        }
    }

    public Result<Boolean> revokeGAccess(String access, String refresh){

        try {
            URL url = new URL("https://accounts.google.com/o/oauth2/revoke");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            Map<String, String> parameters = new HashMap<>();
            parameters.put("token", access);

            con.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());
            out.writeBytes(CommonTools.getParamsString(parameters));
            out.flush();
            out.close();

            con.disconnect();

            return disableGAccessByCredentials(access, refresh);

        } catch (IOException e) {
            Result<Boolean> res = new Result<>();
            res.setData(false);
            res.setMessage("Internal Server Error: couldn't revoke access from Google");
            res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return res;
        }
    }

    public Result<User> insertGAuthorization (User auth){

        String uuid=UUID.randomUUID().toString();
        String smt;
        ArrayList<Object> data;

        Result<User> res = new Result();

        try {
            smt = "INSERT INTO `oauth_google`" +
                    " (`uuid`,`googleId`,`gAccessToken`,`gRefreshToken`,`gExpirationDate`,`status`,`dateCreated`,`dateModified`) " +
                    "VALUES (?,?,?,?,?,?,?,?);";

            data = new ArrayList<>();

            data.add(uuid);
            data.add(auth.getGoogleId());
            data.add(auth.getGAcessToken());
            data.add(auth.getGRefreshToken());
            data.add(auth.getGExpirationDate());
            data.add(true);
            data.add(Timestamp.from(Instant.now()));
            data.add(Timestamp.from(Instant.now()));

            if(jdbc.update(smt,data.toArray()) == 1){
                auth.setAuthorizationUuid(uuid);
                res.setData(auth);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            if (String.valueOf(f.getCause()).contains("oauth_gAccessToken_key")) {
                String sql = "SELECT uuid FROM `oauth_google` WHERE `gAccessToken`=?";
                Map<String, Object> map = jdbc.queryForMap(sql, auth.getAccessToken());
                auth.setAuthorizationUuid(String.valueOf(map.get("uuid")));
                res.setData(auth);
                res.setCode(HttpServletResponse.SC_OK);
                return res;
            } else {
                return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), (User) null);
            }
        }
    }

    Result<User> gRefreshTokens(User previous){
        Result<User> res = new Result<>();

        Date day = new Date(System.currentTimeMillis() - 60 * 5 * 1000);

        if(day.after(previous.getGExpirationDate())){
            try {
                Calendar calendar = Calendar.getInstance();

                GoogleClientSecrets clientSecrets = commonTools.loadSecret();

                try {
                    Result <Boolean> val = disableGAccessByCredentials(previous.getGAcessToken(), previous.getGRefreshToken());

                    if(val.getData()) {
                        GoogleRefreshTokenRequest peticion = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                                previous.getGRefreshToken(),
                                clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret()
                        );

                        peticion.setGrantType("refresh_token");

                        TokenResponse response = peticion.execute();

                        previous.setGAccessToken(response.getAccessToken());
                        calendar.add(Calendar.SECOND, toIntExact(response.getExpiresInSeconds()));
                        previous.setGExpirationDate(calendar.getTime());

                        return insertGAuthorization(previous);
                    }else{
                        res.setCode(val.getCode());
                        res.setMessage(val.getMessage());
                        return res;
                    }
                } catch (TokenResponseException e) {
                    e.printStackTrace();
                    if (e.getDetails() != null) {
                        res.setMessage("Error: " + e.getDetails().getError());
                        logger.info(res.getMessage());
                        if (e.getDetails().getErrorDescription() != null) {
                            logger.info(e.getDetails().getErrorDescription());
                        }
                        if (e.getDetails().getErrorUri() != null) {
                            logger.info(e.getDetails().getErrorUri());
                        }
                    } else {
                        logger.info(e.getMessage());
                    }
                    res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return res;
                }
            } catch (Exception e) {
                e.printStackTrace();
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                res.setMessage("Internal Server Error: "+ e.getLocalizedMessage());
                return res;
            }
        }else{
            res.setCode(HttpServletResponse.SC_OK);
            res.setData(previous);
            return res;
        }
    }
}
