package tech.aabo.celulascontentas.oauth.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tech.aabo.celulascontentas.oauth.pojo.CommonTools;
import tech.aabo.celulascontentas.oauth.pojo.Result;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"SqlResolve", "Duplicates"})
@Repository
public class UserAuthorizationDao {

    @Autowired
    private JdbcTemplate jdbc;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserDao userDao;

    @Autowired
    AuthorizationDao authorizationDao;


    public Result<Boolean> initializeMapping(String authorization_code, String email, String password) {
        String smt = "INSERT INTO `oauth_user_authorization`(`uuid`,`authorization`,`user`, `status`,`dateCreated`,`dateModified`) VALUES(?,?,?,?,?,?);";

        Result<Boolean> res = new Result<>();
        ArrayList<Object> data = new ArrayList<>();

        data.add(UUID.randomUUID().toString());

        Result<Map<String, Object>> result = userDao.validateUser(email, password);
        Result<String> id = authorizationDao.getIdByCode(authorization_code);

        if (id.getCode() == HttpServletResponse.SC_OK) {
            if (result.getCode() == HttpServletResponse.SC_OK) {
                data.add(String.valueOf(id.getData()));
                data.add(String.valueOf(result.getData().get("id")));
                data.add(true);
                data.add(Timestamp.from(Instant.now()));
                data.add(Timestamp.from(Instant.now()));

                try {
                    if (jdbc.update(smt, data.toArray()) == 1) {
                        res.setData(true);
                        res.setCode(HttpServletResponse.SC_OK);
                    } else {
                        res.setMessage("Internal Server Error: Duplicate keys found on tuple");
                        res.setData(false);
                        res.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    return res;
                } catch (DataIntegrityViolationException f) {
                    return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), false);
                }
            } else {
                res.setMessage("Email or password is invalid");
                res.setData(false);
                res.setCode(HttpServletResponse.SC_BAD_REQUEST);
                return res;
            }
        } else {
            res.setData(false);
            res.setMessage("Invalid authorization code");
            res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            return res;
        }
    }

    public Result<Boolean> updatePssword(String email, String password){
        String smt = "UPDATE `oauth_user` SET `password`=? WHERE `email`=?";

        try {
            Result<Boolean> res = new Result<>();
            if(jdbc.update(smt, BCrypt.hashpw(password, BCrypt.gensalt()) , email) == 1){
                res.setData(true);
                res.setCode(HttpServletResponse.SC_OK);
                res.setMessage("Password updated successfully");
            }else{
                res.setMessage("Invalid email parameter");
                res.setData(false);
                res.setCode(HttpServletResponse.SC_BAD_REQUEST);
            }
            return res;
        } catch (DataIntegrityViolationException f) {
            return CommonTools.getErrorInValidation(String.valueOf(f.getCause()), false);
        }
    }
}
