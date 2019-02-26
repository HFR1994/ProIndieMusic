package tech.aabo.celulascontentas.oauth.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tech.aabo.celulascontentas.oauth.domain.Client;
import tech.aabo.celulascontentas.oauth.pojo.CommonTools;
import tech.aabo.celulascontentas.oauth.pojo.Result;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@SuppressWarnings({"SqlResolve", "Duplicates"})
@Repository
public class ClientDao {

    @Autowired
    private JdbcTemplate jdbc;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Result<Client> insertClient(Client client){
        String smt = "INSERT INTO `oauth_client`(`uuid`,`fullName`,`cEmail`," +
                "`verifiedCEmail`,`client_id`,`client_secret`,`status`," +
                "`dateCreated`,`dateModified`)" +
                " VALUES(?,?,?,?,?,?,?,?,?);";

        ArrayList<Object> data = new ArrayList<>();

        String uuid = UUID.randomUUID().toString();

        data.add(uuid);
        data.add(client.getFullName());
        data.add(client.getcEmail());
        data.add(client.getVerifiedCEmail());
        data.add(client.getClientId());
        data.add(client.getClientSecret());
        data.add(client.getStatus());
        data.add(Timestamp.from(Instant.now()));
        data.add(Timestamp.from(Instant.now()));

        try {
            Result<Client> res = new Result<>();
            if(jdbc.update(smt, data.toArray()) == 1){
                client.setUuid(uuid);
                Result<Boolean> val = setRedirectURL(client.getUuid(),client.getRedirects());
                if(val.getData()){
                    res.setData(client);
                    res.setCode(HttpServletResponse.SC_OK);
                }else{
                    res.setData(client);
                    res.setMessage(val.getMessage());
                    res.setCode(val.getCode());
                }
            }else{
                res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), (Client) null);
        }
    }

    private Result<Boolean> setRedirectURL(String client_uuid, HashSet<String> redirects){

        String uuid;
        Result<Boolean> res = new Result<>();

        for(String url: redirects) {

            uuid=UUID.randomUUID().toString();

            String smt = "INSERT INTO `oauth_redirect`" +
                    "(`uuid`,`url`,`status`,`dateCreated`,`dateModified`)" +
                    "VALUES (?,?,?,?,?);";

            ArrayList<Object> data = new ArrayList<>();

            data.add(uuid);
            data.add(url);
            data.add(true);
            data.add(Timestamp.from(Instant.now()));
            data.add(Timestamp.from(Instant.now()));

            try {
                if (jdbc.update(smt, data.toArray()) != 1) {
                    res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                    res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return res;
                }
            } catch (DataIntegrityViolationException f) {

                if (String.valueOf(f.getCause()).contains("oauth_url_key")) {
                    String sql = "SELECT uuid FROM `oauth_redirect` WHERE `url`=?";
                    Map<String, Object> map = jdbc.queryForMap(sql, url);
                    uuid = String.valueOf(map.get("uuid"));
                } else {
                    return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), false);
                }
            }

            smt = "INSERT INTO `oauth_client_redirects`" +
                    "(`uuid`,`client_uuid`,`redirect_uuid`,`status`,`dateCreated`,`dateModified`)" +
                    "VALUES (?,?,?,?,?,?);";

            data = new ArrayList<>();

            data.add(UUID.randomUUID().toString());
            data.add(client_uuid);
            data.add(uuid);
            data.add(true);
            data.add(Timestamp.from(Instant.now()));
            data.add(Timestamp.from(Instant.now()));

            try {
                if (jdbc.update(smt, data.toArray()) != 1) {
                    res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                    res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return res;
                }
            } catch (Exception f) {
                logger.debug("Relationship already exists");
            }

        }
        res.setData(true);
        res.setCode(HttpServletResponse.SC_OK);
        return res;
    }

    public Result<Client> getCredentialValidation(String id, String secret){
        String smt = "SELECT * FROM `oauth_client` WHERE `client_id`=? AND `client_secret`=? AND `status`=?";

        Result<Client> res = new Result<>();
        try {
            BeanPropertyRowMapper<Client> rowMapper = new BeanPropertyRowMapper<>(Client.class);
            res.setData(jdbc.queryForObject(smt, rowMapper, id, secret, true));
            res.setCode(HttpServletResponse.SC_OK);
            return getRedirects(res.getData());
        } catch (EmptyResultDataAccessException e) {
            res.setMessage("Invalid client credentials");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return res;
    }

    private Result<Client> getRedirects(Client client){

        Result<Client> res = new Result<>();
        String smt = "SELECT b.url AS url " +
                "FROM oauth_client_redirects AS a " +
                "INNER JOIN oauth_redirect AS b ON a.redirect_uuid = b.uuid " +
                "WHERE a.client_uuid = ?";

        try {
            List<Map<String, Object>> list = jdbc.queryForList(smt, client.getUuid());

            for(Map<String,Object> ele : list){
               client.setRedirects(String.valueOf(ele.get("url")));
            }

            res.setData(client);
            res.setCode(HttpServletResponse.SC_OK);
        } catch (EmptyResultDataAccessException e) {
            res.setData(null);
            res.setMessage("Invalid client credentials");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }

        return res;
    }

    public Result<String> validateRedirect(String client_uuid, String url){

        Result<String> res = new Result<>();
        String smt = "SELECT a.redirect_uuid AS uuid " +
                "FROM oauth_client_redirects AS a " +
                "INNER JOIN oauth_redirect AS b ON a.redirect_uuid = b.uuid " +
                "WHERE a.client_uuid = ? AND b.url=?";

        try {
            Map<String, Object> list = jdbc.queryForMap(smt, client_uuid, url);
            res.setData(String.valueOf(list.get("uuid")));
            res.setCode(HttpServletResponse.SC_OK);
        } catch (EmptyResultDataAccessException e) {
            res.setData(null);
            res.setMessage("Invalid url parameter");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }

        return res;

    }

    public Result<Client> getCredentialValidation(String id){
        String smt = "SELECT * FROM `oauth_client` WHERE `client_id`=? AND `status`=?";

        Result<Client> res = new Result<>();
        try {
            BeanPropertyRowMapper<Client> rowMapper = new BeanPropertyRowMapper<>(Client.class);
            res.setData(jdbc.queryForObject(smt, rowMapper, id, true));
            res.setCode(HttpServletResponse.SC_OK);
            return getRedirects(res.getData());
        } catch (EmptyResultDataAccessException e) {
            res.setMessage("Invalid client_id parameter");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return res;
    }

    public Result<Client> getClientByUuid(String uuid){
        String smt = "SELECT * FROM `oauth_client` WHERE `uuid`=? AND `status`=?";

        Result<Client> res = new Result<>();
        try {
            BeanPropertyRowMapper<Client> rowMapper = new BeanPropertyRowMapper<>(Client.class);
            res.setData(jdbc.queryForObject(smt, rowMapper, uuid, true));
            res.setCode(HttpServletResponse.SC_OK);
            return getRedirects(res.getData());
        } catch (EmptyResultDataAccessException e) {
            res.setMessage("Invalid uuid parameter");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }
        return res;
    }

    public Result<String> getNumberRecords(){
        String smt = "SELECT `AUTO_INCREMENT`" +
                "FROM  INFORMATION_SCHEMA.TABLES\n" +
                "WHERE TABLE_SCHEMA = 'oauth2'" +
                "AND   TABLE_NAME   = 'oauth_client';";

        Result<String> res = new Result<>();
        try {
            Map<String, Object> val = jdbc.queryForMap(smt);

            String format = String.format("%08d", Long.parseLong(String.valueOf(val.get("AUTO_INCREMENT"))));
            if(format.length() == 8){
                res.setData("1"+format);
            }else{
                res.setData(format);
            }
            res.setCode(HttpServletResponse.SC_OK);
            return res;
        } catch (EmptyResultDataAccessException e) {
            res.setMessage("Internal Server Error: No tuple found");
            res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return res;
    }

    /*private Result<Boolean> revokeClient(String client_id){

        Result<Client> val = getCredentialValidation(client_id);

        Result<Boolean> res = new Result<>();
        if(val.getData() == null){
            res.setMessage(val.getMessage());
            res.setData(false);
            res.setCode(val.getCode());
        }else{
            String smt = "UPDATE `oauth_client` SET `status`=? WHERE `client_id`=?";
            if(jdbc.update(smt, false, client_id) == 1){

                Client client = getCredentialValidation(client_id).getData();

                smt = "SELECT `user_uuid`,`access_token`,`refresh_token`,`google_uuid` " +
                        "FROM `oauth_token` WHERE `client_uuid`=?";

                if(client != null) {
                    try {
                        List<Map<String, Object>> tokens = jdbc.queryForList(smt, client.getUuid());

                        String clientUuid = String.valueOf(tokens.get(0).get("client_uuid"));

                        Result<Boolean> vals = authorizationDao.deleteAuthorization(clientUuid);

                        if (vals.getData()) {
                            for (Map<String, Object> user : tokens) {
                                userDao.updateUser(String.valueOf(user.get("user_uuid")));
                                tokenDao.revokeAccess((String.valueOf(user.get("access_token"))), (String.valueOf(user.get("refresh_token"))));
                                googleDao.disableGAccessByUuid((String.valueOf(user.get("google_uuid"))));
                            }
                            res.setData(true);
                            res.setCode(HttpServletResponse.SC_OK);
                        } else {
                            res.setMessage(vals.getMessage());
                            res.setCode(vals.getCode());
                            res.setData(false);
                        }
                    } catch (EmptyResultDataAccessException e) {
                        res.setMessage("Internal Server Error: Can't validate client");
                        res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        res.setData(false);
                    }
                }else{
                    res.setMessage("Invalid client_id parameter");
                    res.setCode(HttpServletResponse.SC_BAD_REQUEST);
                    res.setData(false);
                }

            }else{
                res.setData(false);
                res.setCode(HttpServletResponse.SC_BAD_REQUEST);
                res.setMessage("Invalid client_id parameter");
            }
        }

        return res;
    }*/

}
