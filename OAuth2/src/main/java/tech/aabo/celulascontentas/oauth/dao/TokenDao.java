package tech.aabo.celulascontentas.oauth.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tech.aabo.celulascontentas.oauth.domain.User;
import tech.aabo.celulascontentas.oauth.pojo.CommonTools;
import tech.aabo.celulascontentas.oauth.pojo.RandomString;
import tech.aabo.celulascontentas.oauth.pojo.Result;

import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static tech.aabo.celulascontentas.oauth.filter.GoogleOAuth2Filter.ACCESS_TOKEN;

@SuppressWarnings({"SqlResolve", "Duplicates"})
@Repository
public class TokenDao {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final JdbcTemplate jdbc;
    private final GoogleDao googleDao;

    @Autowired
    public TokenDao(JdbcTemplate jdbc, GoogleDao googleDao) {
        this.jdbc = jdbc;
        this.googleDao = googleDao;
    }

    private Result<Boolean> revokeAccess(String access, String refresh, boolean locked) {
        String smt;
        if(locked) {
            smt = "UPDATE `oauth_token` SET `locked`=?, `status`=? WHERE `access_token`=? AND `refresh_token`=?";
        }else{
            smt = "UPDATE `oauth_token` SET `expired`=?, `status`=? WHERE `access_token`=? AND `refresh_token`=?";
        }

        try {
            Result<Boolean> res = new Result<>();
            if(jdbc.update(smt, true, false, access, refresh) == 1){
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

    public Result<User> insertAccessToken(User user){

        String uuid=UUID.randomUUID().toString();
        String smt;
        ArrayList<Object> data;

        smt = "INSERT INTO `oauth_token` " +
                "(`uuid`,`google_uuid`,`user_uuid`,`client_uuid`,`access_token`,`refresh_token`,`expirationDate`,`roles`,`expired`,`locked`,`status`,`dateCreated`,`dateModified`) VALUES" +
                "(?,?,?,?,?,?,?,?,?,?,?,?,?);";

        data = new ArrayList<>();

        data.add(uuid);
        data.add(user.getAuthorizationUuid());
        data.add(user.getUserUuid());
        data.add(user.getClientUuid());
        data.add(user.getAccessToken());
        data.add(user.getRefreshToken());
        data.add(user.getExpirationDate());
        data.add(Arrays.toString(user.getRoles().toArray()));
        data.add(user.getExpired());
        data.add(user.getLocked());
        data.add(user.getStatus());
        data.add(Timestamp.from(Instant.now()));
        data.add(Timestamp.from(Instant.now()));

        try {
            Result<User> res = new Result<>();
            if(jdbc.update(smt, data.toArray()) == 1){
                res.setData(user);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                user.setStatus(false);
                user.setLocked(true);
                res.setData(user);
                res.setCode(HttpServletResponse.SC_OK);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), (User) null);
        }
    }

    public Result<User> getSession(String token){

        String sql;
        Map<String, Object> data;

        Result<User> res = new Result<>();
        try {
            if(token.length() == 79) {
                sql = "SELECT *, a.uuid AS token_uuid, a.status AS token_status FROM `oauth_token` as `a`" +
                        " INNER JOIN `oauth_user` AS `b` ON a.user_uuid=b.uuid" +
                        " INNER JOIN `oauth_google` AS `c` ON a.google_uuid=c.uuid" +
                        " WHERE a.access_token=?";

                data = jdbc.queryForMap(sql, token);

            }else {
                sql = "SELECT *, a.uuid AS token_uuid, a.status AS token_status FROM `oauth_token` as `a` " +
                        " INNER JOIN `oauth_user` AS `b` ON a.user_uuid=b.uuid" +
                        " INNER JOIN `oauth_google` AS `c` ON a.google_uuid=c.uuid" +
                        " WHERE a.refresh_token=? AND a.status=?";

                data = jdbc.queryForMap(sql, token, true);
            }

            User auth = new User();

            auth.setUuid(String.valueOf(data.get("token_uuid")));
            auth.setUserUuid(String.valueOf(data.get("user_uuid")));
            auth.setClientUuid(String.valueOf(data.get("client_uuid")));
            auth.setAuthorizationUuid(String.valueOf(data.get("google_uuid")));
            auth.setEmail(String.valueOf(data.get("email")));
            auth.setVerifiedEmail(Boolean.valueOf(String.valueOf(data.get("verifiedEmail"))));
            auth.setName(String.valueOf(data.get("name")));
            auth.setPictureURL(String.valueOf(data.get("pictureUrl")));
            auth.setLocale(String.valueOf(data.get("locale")));
            auth.setFamilyName(String.valueOf(data.get("familyName")));
            auth.setGivenName(String.valueOf(data.get("givenName")));

            auth.setAccessToken(String.valueOf(data.get("access_token")));
            auth.setRefreshToken(String.valueOf(data.get("refresh_token")));
            Date expired = (Date) data.get("expirationDate");
            auth.setGExpirationDate(expired);

            auth.setGoogleId(new BigInteger(String.valueOf(data.get("googleId")))); // Use this value as a key to identify a user.
            auth.setGAccessToken(String.valueOf(data.get("gAccessToken")));
            auth.setGRefreshToken(String.valueOf(data.get("gRefreshToken")));
            Date gExpired = (Date) data.get("gExpirationDate");
            auth.setGExpirationDate(gExpired);

            auth.setStatus((Boolean) data.get("token_status"));
            auth.setExpired((Boolean) data.get("expired"));
            auth.setLocked((Boolean) data.get("locked"));

            auth.setRoles(String.valueOf(data.get("roles")));
            auth.setScopeUpdated((Boolean) data.get("scopesUpdated"));

            res.setData(auth);
            res.setCode(HttpServletResponse.SC_OK);
        }catch(IncorrectResultSizeDataAccessException e){
            res.setMessage("Invalid token parameter");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return res;
    }

    public Result<User> renewSession(String refresh_token, String client_uuid){

        Result<User> res = new Result<>();

        String sql = "SELECT *, a.uuid AS token_uuid, a.status AS token_status FROM `oauth_token` as `a` " +
                " INNER JOIN `oauth_user` AS `b` ON a.user_uuid=b.uuid" +
                " WHERE a.dateCreated = (SELECT MAX(d.dateCreated) FROM `oauth_token` AS d " +
                "          WHERE d.user_uuid = (SELECT a.user_uuid from `oauth_token` AS e " +
                "                WHERE e.refresh_token=? GROUP BY a.user_uuid)) " +
                " AND a.refresh_token=? AND a.client_uuid=?";

        try {

            //CommonTools.printSmt(sql,refresh_token, refresh_token, client_uuid);

            Map<String, Object> data = jdbc.queryForMap(sql, refresh_token, refresh_token, client_uuid);

            User auth = new User();

            auth.setUuid(String.valueOf(data.get("token_uuid")));
            auth.setUserUuid(String.valueOf(data.get("user_uuid")));
            auth.setClientUuid(String.valueOf(data.get("client_uuid")));
            auth.setAuthorizationUuid(data.get("google_uuid") == null ? null:String.valueOf(data.get("google_uuid")));
            auth.setEmail(String.valueOf(data.get("email")));
            auth.setVerifiedEmail(Boolean.valueOf(String.valueOf(data.get("verifiedEmail"))));
            auth.setName(String.valueOf(data.get("name")));
            auth.setPictureURL(String.valueOf(data.get("pictureUrl")));
            auth.setLocale(String.valueOf(data.get("locale")));
            auth.setFamilyName(String.valueOf(data.get("familyName")));
            auth.setGivenName(String.valueOf(data.get("givenName")));

            auth.setAccessToken(String.valueOf(data.get("access_token")));
            auth.setRefreshToken(String.valueOf(data.get("refresh_token")));
            auth.setScopeUpdated((Boolean) data.get("scopesUpdated"));
            auth.setRoles(String.valueOf(data.get("roles")));

            Result<Boolean> val = revokeAccess(auth.getAccessToken(), auth.getRefreshToken(), true);

            if(val.getData()) {

                if(auth.getAuthorizationUuid() != null) {

                    sql = "SELECT * FROM `oauth_google` where uuid=?";

                    data = jdbc.queryForMap(sql, auth.getAuthorizationUuid());

                    auth.setGoogleId(new BigInteger(String.valueOf(data.get("googleId")))); // Use this value as a key to identify a user.
                    auth.setGAccessToken(String.valueOf(data.get("gAccessToken")));
                    auth.setGRefreshToken(String.valueOf(data.get("gRefreshToken")));
                    Date gExpired = (Date) data.get("gExpirationDate");
                    auth.setGExpirationDate(gExpired);

                    res = googleDao.gRefreshTokens(auth);
                    if (res.getData() == null) {
                        return res;
                    }
                }

                auth.setAccessToken(new RandomString(79).nextString());
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, ACCESS_TOKEN);
                auth.setExpirationDate(calendar.getTime());

                auth.setStatus(true);
                auth.setExpired(false);
                auth.setLocked(false);

                return insertAccessToken(auth);
            }else{
                res.setCode(val.getCode());
                res.setMessage(val.getMessage());
            }
        }catch(IncorrectResultSizeDataAccessException e){
            e.printStackTrace();
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            res.setMessage("Invalid refresh_token parameter");
        } catch (DataAccessException e){
            e.printStackTrace();
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            res.setMessage(e.getMessage());
        }

        return res;
    }

    public Result<User> validateAccessToken(String access_token){

        String sql = "SELECT *, a.uuid AS token_uuid, a.status AS token_status FROM `oauth_token` as `a` " +
                "INNER JOIN `oauth_user` AS `b` ON a.user_uuid=b.uuid " +
                "WHERE a.`access_token`=?;";

        Result<User> res = new Result<>();
        try {

            Map<String, Object> data = jdbc.queryForMap(sql, access_token);

            User auth = new User();

            auth.setUuid(String.valueOf(data.get("token_uuid")));
            auth.setUserUuid(String.valueOf(data.get("user_uuid")));
            auth.setClientUuid(String.valueOf(data.get("client_uuid")));
            auth.setAuthorizationUuid(data.get("google_uuid") == null ? null:String.valueOf(data.get("google_uuid")));

            auth.setEmail(String.valueOf(data.get("email")));
            auth.setVerifiedEmail(Boolean.valueOf(String.valueOf(data.get("verifiedEmail"))));
            auth.setName(String.valueOf(data.get("name")));
            auth.setPictureURL(String.valueOf(data.get("pictureUrl")));
            auth.setLocale(String.valueOf(data.get("locale")));
            auth.setFamilyName(String.valueOf(data.get("familyName")));
            auth.setGivenName(String.valueOf(data.get("givenName")));

            auth.setAccessToken(String.valueOf(data.get("access_token")));
            auth.setRefreshToken(String.valueOf(data.get("refresh_token")));
            Date expired = (Date) data.get("expirationDate");
            auth.setExpirationDate(expired);

            auth.setStatus((Boolean) data.get("token_status"));
            auth.setExpired((Boolean) data.get("expired"));
            auth.setLocked((Boolean) data.get("locked"));

            auth.setScopeUpdated((Boolean) data.get("scopesUpdated"));

            auth.setRoles(String.valueOf(data.get("roles")));

            if(new Date().after(auth.getExpirationDate())){
                auth.resetRoles();
                auth.setExpired(true);
                auth.setStatus(false);
                revokeAccess(auth.getAccessToken(), auth.getRefreshToken(),false);

                res.setData(auth);
                res.setCode(HttpServletResponse.SC_OK);
            }else{

                if(auth.getAuthorizationUuid() != null) {

                    sql = "SELECT * FROM `oauth_google` where uuid=?";

                    data = jdbc.queryForMap(sql, auth.getAuthorizationUuid());

                    auth.setGoogleId(new BigInteger(String.valueOf(data.get("googleId")))); // Use this value as a key to identify a user.
                    auth.setGAccessToken(String.valueOf(data.get("gAccessToken")));
                    auth.setGRefreshToken(String.valueOf(data.get("gRefreshToken")));
                    Date gExpired = (Date) data.get("gExpirationDate");
                    auth.setGExpirationDate(gExpired);

                    return googleDao.gRefreshTokens(auth);
                }else{
                    res.setCode(HttpServletResponse.SC_OK);
                    res.setData(auth);
                    return res;
                }

            }

        }catch(IncorrectResultSizeDataAccessException e){
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            res.setMessage("Invalid access_token parameter");
        }

        return res;
    }

    public Result<Boolean> cleanAccess(String refreshToken) {
        String smt = "UPDATE `oauth_token` SET `locked`=?, `status`=? WHERE `refresh_token`=?";

        try {
            Result<Boolean> res = new Result<>();
            if(jdbc.update(smt, true, false, refreshToken) != 0){
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Internal Server Error: Non valid credentials found");
                res.setData(false);
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), false);
        }
    }
}
