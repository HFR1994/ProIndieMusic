package tech.aabo.celulascontentas.oauth.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Repository;
import tech.aabo.celulascontentas.oauth.domain.User;
import tech.aabo.celulascontentas.oauth.pojo.CommonTools;
import tech.aabo.celulascontentas.oauth.pojo.Result;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SqlResolve")
@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate jdbc;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Result<Boolean> updateUser(String uuid){
        String smt = "UPDATE `oauth_user` SET `status`=? WHERE `uuid`=?";

        try {
            Result<Boolean> res = new Result<>();
            if(jdbc.update(smt, false, uuid) == 1){
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Invalid uuid parameter");
                res.setData(false);
                res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), false);
        }
    }

    public Result<Map<String, Object>> validateUser(String email, String password) {
        String smt = "SELECT `id`,`password` FROM `oauth_user` WHERE `email`=?";

        Result<Map<String, Object>> res = new Result<>();

        try {
            Map<String, Object> map = jdbc.queryForMap(smt, email);

            if(!map.isEmpty()){
                String hashed = String.valueOf(map.get("password"));
                if (BCrypt.checkpw(password, hashed)) {
                    res.setData(map);
                    res.setCode(HttpServletResponse.SC_OK);
                }else {
                    res.setMessage("Email or password is invalid");
                    res.setData(null);
                    res.setCode(HttpServletResponse.SC_BAD_REQUEST);
                }
            }else{
                res.setMessage("Email or password is invalid");
                res.setData(null);
                res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            }
            return res;
        } catch (IncorrectResultSizeDataAccessException e) {
            e.printStackTrace();
            res.setMessage("Email or password is invalid");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            return res;
        }
    }

    public Result<User> getUser(String authorization_code) {
        String smt = "SELECT a.* FROM `oauth_user` as `a` INNER JOIN `oauth_user_authorization`" +
                " AS `b` ON a.id=b.user INNER JOIN `oauth_authorization` AS `c` ON b.authorization=c.id " +
                "WHERE c.authorization=?";

        Result<User> res = new Result<>();

        try {

            BeanPropertyRowMapper<User> rowMapper = new BeanPropertyRowMapper<>(User.class);
            User user=jdbc.queryForObject(smt, rowMapper, authorization_code);

            if(user != null){
                res.setData(user);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Invalid authorization code");
                res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            }
            return res;
        } catch (IncorrectResultSizeDataAccessException e) {
            e.printStackTrace();
            res.setMessage("Invalid authorization code");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
        }

        return res;
    }

    public Result<Boolean> resetAlert(String uuid){
        String smt = "UPDATE `oauth_user` SET `scopesUpdated`=? WHERE `uuid`=?";

        try {
            Result<Boolean> res = new Result<>();
            if(jdbc.update(smt, true, uuid) == 1){
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Invalid uuid parameter");
                res.setData(false);
                res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), false);
        }
    }


    public Result<Boolean> insertUser(User user) {


        Result<Boolean> res = new Result<>();
        String uuid=UUID.randomUUID().toString();
        user.setUserUuid(uuid);
        ArrayList<Object> data = new ArrayList<>();

        String smt = "INSERT INTO `oauth_user`" +
                " (`uuid`,`email`,`verifiedEmail`,`name`,`pictureURL`,`locale`,`familyName`,`givenName`,`status`,`dateCreated`,`dateModified`) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?);";


        data.add(uuid);
        data.add(user.getEmail());
        data.add(user.getVerifiedEmail());
        data.add(user.getName());
        data.add(user.getPictureURL());
        data.add(user.getLocale());
        data.add(user.getFamilyName());
        data.add(user.getGivenName());
        data.add(true);
        data.add(Timestamp.from(Instant.now()));
        data.add(Timestamp.from(Instant.now()));

        try {
            if(jdbc.update(smt, data.toArray()) == 1){
                user.setUserUuid(uuid);
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
            }else{
                res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                res.setData(false);
                res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (DataIntegrityViolationException f) {

            if (String.valueOf(f.getCause()).contains("oauth_email_key")) {
                String sql = "SELECT uuid, scopesUpdated FROM `oauth_user` WHERE `email`=?";
                Map<String, Object> map = jdbc.queryForMap(sql, user.getEmail());
                user.setUserUuid(String.valueOf(map.get("uuid")));
                if(!((Boolean) map.get("scopesUpdated"))){
                    resetAlert(user.getUserUuid());
                }
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
                return res;
            } else {
                return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), false);
            }
        }
        return res;
    }
}
