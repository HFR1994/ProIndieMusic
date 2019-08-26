package tech.aabo.celulascontentas.oauth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tech.aabo.celulascontentas.oauth.dao.AuthorizationDao;
import tech.aabo.celulascontentas.oauth.dao.GoogleDao;
import tech.aabo.celulascontentas.oauth.dao.TokenDao;
import tech.aabo.celulascontentas.oauth.dao.UserDao;
import tech.aabo.celulascontentas.oauth.domain.Client;
import tech.aabo.celulascontentas.oauth.domain.User;
import tech.aabo.celulascontentas.oauth.pojo.CommonTools;
import tech.aabo.celulascontentas.oauth.pojo.RandomString;
import tech.aabo.celulascontentas.oauth.pojo.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.*;

import static java.lang.Math.toIntExact;

/**
 * Created by colorado on 9/03/17.
 */
@SuppressWarnings("Duplicates")
@Service
public class GoogleOAuth2Filter{

    public static final Integer ACCESS_TOKEN = 3601;

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuth2Filter.class);

    @Autowired
    private AuthorizationDao authorizationDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private GoogleDao googleDao;

    @Autowired
    private CommonTools commonTools;

    private JSONObject values;

    public Authentication refreshToken(HttpServletResponse response, HashMap<String, String> query, String cred) {
        String refresh_token = query.get("refresh_token");

        try {
            if (refresh_token == null || refresh_token.length()==0) {
                CommonTools.setResponse(response,"No refresh token was provided", HttpServletResponse.SC_BAD_REQUEST);
            } else {
                Result<User> user = tokenDao.renewSession(refresh_token, cred);

                if(user.getData() != null) {
                    if (user.getData().isScopeUpdated()) {
                        if (user.getData() != null) {
                            values = new JSONObject();

                            values.put("access_token", user.getData().getAccessToken());
                            values.put("token_type", "Bearer");
                            values.put("expires_in", String.valueOf((user.getData().getExpirationDate().getTime() - new Date().getTime()) / 1000));

                            response.setContentType("application/json");
                            response.setCharacterEncoding("utf-8");
                            response.setStatus(HttpServletResponse.SC_OK);

                            PrintWriter out = response.getWriter();

                            out.print(values.toString());
                        } else {
                            CommonTools.setResponse(response, user.getMessage(), user.getCode());
                        }
                    } else {
                        CommonTools.setResponse(response, "Scope was updated must sign-in again", HttpServletResponse.SC_NOT_ACCEPTABLE);
                    }
                }else{
                    CommonTools.setResponse(response,user.getMessage(), user.getCode());
                }
            }
        } catch (Exception e) {
            CommonTools.setResponse(response,"Internal Server Error: " + e.getLocalizedMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        return null;
    }

    public void authorizationCode(String code, HttpServletRequest request, HttpServletResponse response, String cred, ArrayList<String> scopes){
        CommonTools.eraseCookies(response, request.getCookies());

        try {
            if (code != null) {

                GoogleClientSecrets clientSecrets = commonTools.loadSecret();

                if (clientSecrets == null) {
                    CommonTools.setResponse(response, "Internal Server Error, can't load credentials", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } else {

                    Result<Map> res = authorizationDao.getAuthorizationValidation(code);

                    if(res.getData() != null) {

                        Calendar calendar = Calendar.getInstance();

                        if (res.getData().get("gAuthorization") != null) {


                            NetHttpTransport net = new NetHttpTransport();
                            JacksonFactory jackson = new JacksonFactory();
                            User auth = new User();

                            try {
                                GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(net, jackson,
                                        clientSecrets.getDetails().getClientId(), clientSecrets.getDetails().getClientSecret(),
                                        String.valueOf(res.getData().get("gAuthorization")), CommonTools.transformName(request, 2) + "/oauth2/auth")
                                        .execute();

                                calendar.add(Calendar.SECOND, toIntExact(tokenResponse.getExpiresInSeconds()));

                                calendar = Calendar.getInstance();

                                // Use access token to call API
                                GoogleCredential credential;

                                if (tokenResponse.getRefreshToken() == null) {
                                    credential = new GoogleCredential();
                                    credential.setFromTokenResponse(tokenResponse);
                                } else {
                                    credential = CommonTools.createCredentialWithRefreshToken(net, jackson, clientSecrets, tokenResponse);
                                }

                                Plus plus = new Plus.Builder(new NetHttpTransport(),
                                        JacksonFactory.getDefaultInstance(), credential)
                                        .setApplicationName("Google Plus Profile Info")
                                        .build();

                                Person profile = plus.people().get("me").execute();

                                // Get profile info from ID token
                                GoogleIdToken idToken = tokenResponse.parseIdToken();
                                GoogleIdToken.Payload payload = idToken.getPayload();

                                auth.setGAccessToken(tokenResponse.getAccessToken());
                                auth.setGRefreshToken(tokenResponse.getRefreshToken());
                                auth.setGoogleId(new BigInteger(payload.getSubject().trim())); // Use this value as a key to identify a user.
                                calendar.add(Calendar.SECOND, toIntExact(tokenResponse.getExpiresInSeconds()));
                                auth.setGExpirationDate(calendar.getTime());

                                auth.setEmail(payload.getEmail());
                                auth.setVerifiedEmail(payload.getEmailVerified());
                                auth.setName(profile.getDisplayName());
                                auth.setPictureURL(profile.getImage().getUrl());
                                auth.setLocale(profile.getLanguage());
                                auth.setFamilyName(profile.getName().getFamilyName());
                                auth.setGivenName(profile.getName().getGivenName());
                                auth.setStatus(true);
                                auth.setExpired(false);
                                auth.setLocked(false);
                                auth.setDateCreated(calendar.getTime());
                                auth.setClientUuid(cred);

                                auth.setDateModified(Calendar.getInstance().getTime());

                                for(String scope : scopes){
                                    auth.setRoles(scope.trim());
                                }

                                auth.setAccessToken(new RandomString(79).nextString());

                                auth.setRefreshToken(new RandomString(40).nextString());
                                calendar = Calendar.getInstance();
                                calendar.add(Calendar.SECOND, ACCESS_TOKEN);
                                auth.setExpirationDate(calendar.getTime());

                                googleDao.insertGAuthorization(auth);
                                userDao.insertUser(auth);
                                tokenDao.insertAccessToken(auth);

                                values = new JSONObject();

                                values.put("access_token", auth.getAccessToken());
                                values.put("token_type", "Bearer");
                                values.put("expires_in", String.valueOf((auth.getExpirationDate().getTime() - new Date().getTime()) / 1000));
                                values.put("refresh_token", auth.getRefreshToken());

                                response.setContentType("application/json");
                                response.setCharacterEncoding("utf-8");
                                response.setStatus(HttpServletResponse.SC_OK);

                                PrintWriter out = response.getWriter();

                                out.print(values.toString());

                            } catch (IOException e) {
                                e.printStackTrace();
                                CommonTools.setResponse(response, "Google Validation failed, token endpoint mismatch", HttpServletResponse.SC_BAD_REQUEST);
                            }
                        } else {

                            User auth = userDao.getUser(code).getData();

                            if(auth != null){

                                auth.setStatus(true);
                                auth.setExpired(false);
                                auth.setLocked(false);
                                auth.setDateCreated(calendar.getTime());
                                auth.setClientUuid(cred);
                                auth.setDateModified(Calendar.getInstance().getTime());

                                for(String scope : scopes){
                                    auth.setRoles(scope.trim());
                                }

                                auth.setAccessToken(new RandomString(79).nextString());
                                auth.setRefreshToken(new RandomString(40).nextString());

                                calendar = Calendar.getInstance();
                                calendar.add(Calendar.SECOND, ACCESS_TOKEN);
                                auth.setExpirationDate(calendar.getTime());

                                userDao.insertUser(auth);
                                tokenDao.insertAccessToken(auth);

                                values = new JSONObject();

                                values.put("access_token", auth.getAccessToken());
                                values.put("token_type", "Bearer");
                                values.put("expires_in", String.valueOf((auth.getExpirationDate().getTime() - new Date().getTime()) / 1000));
                                values.put("refresh_token", auth.getRefreshToken());

                                response.setContentType("application/json");
                                response.setCharacterEncoding("utf-8");
                                response.setStatus(HttpServletResponse.SC_OK);

                                PrintWriter out = response.getWriter();

                                out.print(values.toString());

                            }else{
                                CommonTools.setResponse(response, "Authorization code is invalid", HttpServletResponse.SC_BAD_REQUEST);
                            }

                        }
                    }else{
                        CommonTools.setResponse(response, res.getMessage(), res.getCode());
                    }
                }
            } else {
                CommonTools.setResponse(response, "Missing authorization_code parameter", HttpServletResponse.SC_BAD_REQUEST);
            }
        }catch(NullPointerException e){
            CommonTools.setResponse(response, "Missing authorization_code parameter", HttpServletResponse.SC_BAD_REQUEST);
        }catch (Exception e) {
            e.printStackTrace();
            CommonTools.setResponse(response,"Internal Server Error: " + e.getLocalizedMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
