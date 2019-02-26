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
import tech.aabo.celulascontentas.oauth.pojo.CommonTools;
import tech.aabo.celulascontentas.oauth.pojo.RandomString;
import tech.aabo.celulascontentas.oauth.pojo.Result;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"SqlResolve", "Duplicates"})
@Repository
public class AuthorizationDao {

    @Autowired
    private JdbcTemplate jdbc;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    ClientDao clientDao;

    public ClientDao getClientDao() {
        return clientDao;
    }

    public Result<Boolean> initializeAuthorization(String client_uuid, String state, String redirect_uri){
        String smt = "INSERT INTO `oauth_authorization`(`uuid`,`client_uuid`,`redirect_uuid`,`state`,`status`,`dateCreated`,`dateModified`) VALUES(?,?,?,?,?,?,?);";

        Result<Boolean> res = new Result<>();
        ArrayList<Object> data = new ArrayList<>();

        data.add(UUID.randomUUID().toString());
        data.add(client_uuid);

        Result<String> val = clientDao.validateRedirect(client_uuid, redirect_uri);

        if(val.getData() == null){
           res.setMessage(val.getMessage());
           res.setCode(val.getCode());
           res.setData(false);
           return res;
        }else{
            data.add(val.getData());
        }
        data.add(state);
        data.add(true);
        data.add(Timestamp.from(Instant.now()));
        data.add(Timestamp.from(Instant.now()));

        try {
            if(jdbc.update(smt, data.toArray()) == 1){
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                res.setData(false);
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()),false);
        }
    }

    public Result<String> finalizeAuthorization(String code, String state){
        String smt = "UPDATE `oauth_authorization` SET `authorization`=?, `gAuthorization`=?, `dateModified`=? WHERE `state`=? AND `status`=?";

        ArrayList<Object> data = new ArrayList<>();

        String auth = new RandomString(60).nextString();

        data.add(auth);
        data.add(code);
        data.add(Timestamp.from(Instant.now()));
        data.add(state);
        data.add(true);

        try {
            Result<String> res = new Result<>();
            if(jdbc.update(smt, data.toArray()) == 1){
                res.setData(auth);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return res;
        } catch (DataAccessException f) {
            f.getStackTrace();
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()),"");
        }

    }

    public Result<String> getRedirectURL(String state) {
        String smt = "SELECT b.url AS url " +
                "FROM `oauth_authorization` AS a "+
                "INNER JOIN `oauth_redirect` AS b ON a.redirect_uuid = b.uuid "+
                "WHERE a.state=? and a.status=? LIMIT 1";

        Result<String> res = new Result<>();

        try {
            Map<String, Object> map = jdbc.queryForMap(smt, state, true);

            if(!map.isEmpty()){
                res.setData(String.valueOf(map.get("url")));
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Invalid state parameter");
                res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            e.printStackTrace();
            res.setMessage("Invalid state parameter");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return res;
    }
    public Result<Map> getAuthorizationValidation(String code){
        String smt = "SELECT gAuthorization, authorization FROM `oauth_authorization` WHERE `authorization`=? AND `status`=?";


        Result<Map> res = new Result<>();

        try {
            Map<String, Object> map = jdbc.queryForMap(smt, code, true);

            if(!map.isEmpty()){
                if(updateAuthorization(code).getData()){
                    res.setData(map);
                    res.setCode(HttpServletResponse.SC_OK);
                }else{
                    res.setMessage("Internal Server Error: Could not invalidate authorization code");
                    res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }else{
                res.setMessage("Invalid authorization code");
                res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (EmptyResultDataAccessException e) {
            res.setMessage("Invalid authorization code");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return res;
    }

    public Result<String> getStateValidation(String state){
        String smt = "SELECT client_uuid FROM `oauth_authorization` WHERE `state`=? AND `status`=?";

        Result<String> res = new Result<>();
        try {
            Map<String, Object> map = jdbc.queryForMap(smt, state, true);
            if(!map.isEmpty()){
                res.setData(String.valueOf(map.get("client_uuid")));
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Invalid state parameter");
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            res.setMessage("Invalid authorization code");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return res;
    }

    public Result<String> getIdByCode(String code){
        String smt = "SELECT id FROM `oauth_authorization` WHERE `authorization`=? AND `status`=?";

        Result<String> res = new Result<>();
        try {
            Map<String, Object> map = jdbc.queryForMap(smt, code, true);
            if(!map.isEmpty()){
                res.setData(String.valueOf(map.get("id")));
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Invalid authorization code");
                res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            res.setMessage("Invalid authorization code");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return res;
    }

    private Result<Boolean> updateAuthorization(String code){
        String smt = "UPDATE `oauth_authorization` SET `status`=? WHERE `authorization`=?";

        try {
            Result<Boolean> res = new Result<>();
            if(jdbc.update(smt, false, code) == 1){
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setData(false);
                res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()),false);
        }
    }

    Result<Boolean> deleteAuthorization(String uuid){
        String smt = "UPDATE `oauth_authorization` SET `status`=? WHERE `client_uuid`=?";

        try {
            Result<Boolean> res = new Result<>();
            if(jdbc.update(smt, false, uuid) != 0){
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setData(false);
                res.setMessage("Internal Server Error: Non valid authorization codes found");
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()),false);
        }
    }

}
