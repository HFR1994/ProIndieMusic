package tech.aabo.celulascontentas.oauth;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import tech.aabo.celulascontentas.oauth.dao.*;
import tech.aabo.celulascontentas.oauth.domain.Client;
import tech.aabo.celulascontentas.oauth.domain.User;
import tech.aabo.celulascontentas.oauth.filter.GoogleOAuth2Filter;
import tech.aabo.celulascontentas.oauth.filter.JwtAuthorizationTokenFilter;
import tech.aabo.celulascontentas.oauth.filter.UnauthorizedHandler;
import tech.aabo.celulascontentas.oauth.pojo.CommonTools;
import tech.aabo.celulascontentas.oauth.pojo.RandomString;
import tech.aabo.celulascontentas.oauth.pojo.Result;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Duplicates")
@SpringBootApplication
@RestController
@EnableOAuth2Client
public class OauthApplication extends WebSecurityConfigurerAdapter {

    @Autowired
    private GoogleOAuth2Filter googleOAuth2Filter;

    @Autowired
    private AuthorizationDao authorizationDao;

    @Autowired
    private UserAuthorizationDao userAuthorizationDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private TokenDao tokenDao;

    @Autowired
    private GoogleDao googleDao;

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private CommonTools commonTools;

    @Autowired
    private UnauthorizedHandler unauthorizedHandler;

    @Autowired
    JwtAuthorizationTokenFilter authenticationTokenFilter;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void main(String[] args) {
        SpringApplication.run(OauthApplication.class, args);
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }

    @RequestMapping(value = "/oauth2/token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void token(HttpServletRequest request, HttpServletResponse response) {

        try{
            HashMap<String, String> query = new ObjectMapper().readValue(CharStreams.toString(request.getReader()), HashMap.class);
            String grant_type = query.get("grant_type");
            String client_id = query.get("client_id");
            String client_secret = query.get("client_secret");
            ArrayList<String> scopes;

            if(query.containsKey("scope")){
                scopes = new ArrayList<>(Arrays.asList(query.get("scope")));
                if(scopes.size() == 0) {
                    scopes.add("USER");
                }else if (!scopes.contains("USER")) {
                    scopes.add("USER");
                }
            }else{
                scopes = new ArrayList<>();
                scopes.add("USER");
            }

            Client cred = commonTools.validateCredentials(response, client_id, client_secret);

            if(cred != null) {
                if (query.isEmpty()) {
                    CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    if (grant_type == null) {
                        CommonTools.setResponse(response, "Missing grant_type paramater", HttpServletResponse.SC_BAD_REQUEST);
                    } else {
                        switch (grant_type) {
                            case "authorization_code":
                                Result<String> redirect_uri = clientDao.validateRedirect(cred.getUuid(), query.get("redirect_uri"));
                                if(redirect_uri.getData() != null) {
                                    String code = (String) query.get("code");
                                    googleOAuth2Filter.authorizationCode(code ,request, response, cred.getUuid(), scopes);
                                }else{
                                    CommonTools.setResponse(response, redirect_uri.getMessage(), redirect_uri.getCode());
                                }
                                break;
                            case "refresh_token":
                                googleOAuth2Filter.refreshToken(request, response, cred.getUuid());
                                break;
                            default:
                                CommonTools.setResponse(response,"Invalid grant_type parameter", HttpServletResponse.SC_BAD_REQUEST);
                                break;
                        }
                    }
                }
            }
        }catch(NullPointerException e) {
            e.printStackTrace();
            CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
            CommonTools.setResponse(response, "Not a valid JSON response", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/oauth2/user/password", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void addPassword(HttpServletRequest request, HttpServletResponse response) {

        try {
            HashMap<String, String> query = new ObjectMapper().readValue(CharStreams.toString(request.getReader()), HashMap.class);
            String email = query.get("email");
            String password = query.get("password");

            Result resl = userAuthorizationDao.updatePssword(email, password);

            CommonTools.setResponse(response,resl.getMessage(), resl.getCode());

        }catch(NullPointerException e) {
            e.printStackTrace();
            CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
            CommonTools.setResponse(response, "Not a valid JSON response", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/oauth2/client", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void addClient(HttpServletRequest request, HttpServletResponse response) {

        try {
            HashMap<String, String> query = new ObjectMapper().readValue(CharStreams.toString(request.getReader()), HashMap.class);
            if (query.isEmpty()) {
                CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
            } else {
                Client client = new Client();

                client.setFullName(query.get("fullName"));
                client.setcEmail(query.get("email"));
                client.setRedirects(query.get("redirect_uri"));
                client.setVerifiedCEmail(true);

                /* AutoGenerated */

                String sb = clientDao.getNumberRecords().getData() +
                        "-" +
                        new RandomString(45).nextString() +
                        ".oauth2user.aabo.tech";

                client.setClientId(sb);

                client.setClientSecret(new RandomString(30).nextString());

                client.setStatus(true);

                Result<Client> res = clientDao.insertClient(client);

                if (res.getData() == null) {
                    CommonTools.setResponse(response, res.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } else {
                    try {

                        response.setContentType("application/json");
                        response.setCharacterEncoding("utf-8");
                        response.setStatus(HttpServletResponse.SC_OK);
                        PrintWriter out = response.getWriter();

                        //create Json Object
                        JSONObject values = new JSONObject(client.toString());

                        out.print(values.toString());
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }catch(NullPointerException e){
            CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
            CommonTools.setResponse(response, "Not a valid JSON response", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/oauth2/auth", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void localAuthenticate(HttpServletRequest request, HttpServletResponse response) {


        try {
            HashMap query = new ObjectMapper().readValue(CharStreams.toString(request.getReader()), HashMap.class);
            String client = (String) query.get("client_id");
            String redirect_uri = (String) query.get("redirect_uri");
            String state = (String) query.get("state");
            String email = (String) query.get("email");
            String password = (String) query.get("password");

            Client cred = commonTools.validateCredentials(response, client);

            if(cred != null) {
                if (query.isEmpty()) {
                    CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
                } else {
                    Result<Boolean> val = authorizationDao.initializeAuthorization(cred.getUuid(), state, redirect_uri);
                    if (val.getData()) {
                        Result<String> url = authorizationDao.getRedirectURL(state);
                        String auth = authorizationDao.finalizeAuthorization(null, state).getData();

                        if(auth != null && !auth.isEmpty()) {
                            Result<Boolean> mapping = userAuthorizationDao.initializeMapping(auth, email, password);

                            if (mapping.getData()) {
                                if (CommonTools.transformName(request, 1).equals(url.getData())) {
                                    CommonTools.setResponse(response, auth, HttpServletResponse.SC_OK);
                                } else {
                                    if (url.getData() != null) {
                                        HashMap<String, String> value = new HashMap<>();
                                        value.put("state", state);
                                        value.put("code", auth);
                                        CommonTools.setResponse(response, url.getMessage(), url.getCode(), value);
                                    } else {
                                        CommonTools.setResponse(response, url.getMessage(), url.getCode());
                                    }
                                }
                            } else {
                                CommonTools.setResponse(response, mapping.getMessage(), mapping.getCode());
                            }
                        }else{
                            CommonTools.setResponse(response, "Invalid state, already in use", HttpServletResponse.SC_BAD_REQUEST);
                        }
                    } else {
                        CommonTools.setResponse(response, val.getMessage(),val.getCode());
                    }
                }
            }
        }catch(NullPointerException e) {
            e.printStackTrace();
            CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
            CommonTools.setResponse(response, "Not a valid JSON response", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public void ping(HttpServletRequest request, HttpServletResponse response) {
        CommonTools.setResponse(response, "pong", 200);
    }

    @RequestMapping(value = "/oauth2/auth", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void authenticate(HttpServletRequest request, HttpServletResponse response) {

        HashMap<String, String> query = CommonTools.getQueryMap(request.getQueryString());

        try {
            if(query.isEmpty()) {
                CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
            }else{

                String client = query.get("client_id");
                String redirect_uri = String.valueOf(query.get("redirect_uri"));


                GoogleClientSecrets clientSecrets = commonTools.loadSecret();

                if (clientSecrets == null) {
                    CommonTools.setResponse(response, "Internal Server Error, can't load credentials", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } else {
                    if (query.get("code") == null) {

                        Client cred = commonTools.validateCredentials(response, client);

                        if(cred != null) {
                            try {
                                if (query.get("redirect_uri") == null) {
                                    CommonTools.setResponse(response, "Missing redirect_uri parameter", HttpServletResponse.SC_BAD_REQUEST);
                                }
                                String state = String.valueOf(query.get("state"));

                                Result<Boolean> val = authorizationDao.initializeAuthorization(cred.getUuid(), state, redirect_uri);
                                if (val.getData()) {
                                    GoogleAuthorizationCodeRequestUrl auth = new GoogleAuthorizationCodeRequestUrl(clientSecrets.getDetails().getClientId(),
                                            CommonTools.transformName(request,2)+"/oauth2/auth", Arrays.asList(
                                            "https://www.googleapis.com/auth/plus.login",
                                            "https://www.googleapis.com/auth/plus.me",
                                            "https://www.googleapis.com/auth/plus.profile.emails.read",
                                            "https://www.googleapis.com/auth/calendar",
                                            "https://mail.google.com/",
                                            "https://www.googleapis.com/auth/gmail.labels"
                                    )).setState(state);
                                    auth.setAccessType("offline");
                                    auth.setApprovalPrompt("force");
                                    response.sendRedirect(auth.build());
                                } else {
                                    CommonTools.setResponse(response, val.getMessage(),val.getCode());
                                }
                            } catch (IOException e) {
                                CommonTools.setResponse(response, "Google Validation failed, authorization endpoint mismatch", HttpServletResponse.SC_BAD_REQUEST);
                            }
                        }
                    } else {
                        //noinspection MismatchedQueryAndUpdateOfCollection
                        AuthorizationCodeResponseUrl authResponse = new AuthorizationCodeResponseUrl(CommonTools.transformName(request, 0));
                        Client cred = commonTools.validateState(response,authResponse.getState());

                        if(cred != null) {

                            try {
                                // check for user-denied error
                                if (authResponse.getError() != null) {
                                    CommonTools.setResponse(response, "Could not authenticate using Google", HttpServletResponse.SC_FORBIDDEN);
                                } else {
                                    Result<String> url = authorizationDao.getRedirectURL(authResponse.getState());
                                    String auth = authorizationDao.finalizeAuthorization(authResponse.getCode(),authResponse.getState()).getData();
                                    if (auth != null) {
                                        try {
                                            if (CommonTools.transformName(request, 1).equals(url.getData())) {
                                                CommonTools.setResponse(response, auth, HttpServletResponse.SC_OK);
                                            } else {
                                                if(url.getData() != null) {
                                                    response.sendRedirect(url.getData() + "?code=" + auth+"&state="+authResponse.getState());
                                                }else{
                                                    CommonTools.setResponse(response, url.getMessage(), url.getCode());
                                                }
                                            }
                                        } catch (IOException e) {
                                            CommonTools.setResponse(response, "Missing redirect_uri parameter", HttpServletResponse.SC_BAD_REQUEST);
                                        }
                                    } else {
                                        CommonTools.setResponse(response, "Google Validation failed, invalid state", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                                    }
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                CommonTools.setResponse(response, "Invalid redirect_uri parameter", HttpServletResponse.SC_BAD_REQUEST);
                            }
                        }else{
                            CommonTools.setResponse(response, "Invalid state", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }
                    }
                }
            }
        }catch(NullPointerException e){
            CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/oauth2/user", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void account(HttpServletResponse response, Authentication authentication) {

        User user = (User) authentication.getPrincipal();

        if (user != null) {

            if(user.isScopeUpdated()) {
                try {
                    JSONObject map = new JSONObject();
                    JSONObject values = new JSONObject();

                    if (user.getStatus()) {
                        map.put("uuid", user.getUserUuid());
                        map.put("name", user.getName());
                        map.put("firstName", user.getGivenName());
                        map.put("lastName", user.getFamilyName());
                        map.put("email", user.getEmail());
                        map.put("picture", user.getPictureURL());
                        map.put("locale", user.getLocale());

                        values.put("principal", map);
                    } else {
                        values.put("principal", JSONObject.NULL);
                    }

                    map = new JSONObject();

                    map.put("credentialsExpired", user.getExpired());
                    map.put("locked", user.getLocked());
                    map.put("authorities", user.getRoles());
                    map.put("status", user.getStatus());
                    map.put("expirateDate", new SimpleDateFormat("yyyy-MM-dd kk:mm:ss zz").format(user.getExpirationDate()));

                    values.put("authentication", map);
                    values.put("timestamp", String.valueOf(Timestamp.from(Instant.now())));
                    values.put("code", user.getStatus() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_UNAUTHORIZED);
                    values.put("message", user.getStatus() ? "Successfully authenticated" : "Not Authorized");
                    response.setContentType("application/json");
                    response.setCharacterEncoding("utf-8");
                    response.setStatus(user.getStatus() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_UNAUTHORIZED);
                    PrintWriter out = response.getWriter();

                    out.print(values.toString());
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }else{
                CommonTools.setResponse(response, "Scopes must be updated", HttpServletResponse.SC_NOT_ACCEPTABLE);
            }
        } else {
            CommonTools.setResponse(response, "Invalid access_token parameter", HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    @RequestMapping(value = "/oauth2/user", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void register(HttpServletRequest request, HttpServletResponse response) {

        try {
            HashMap<String, String> query = new ObjectMapper().readValue(CharStreams.toString(request.getReader()), HashMap.class);
            if (query.isEmpty()) {
                CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
            } else {
                User user = new User();

                user.setEmail(query.get("email"));
                user.setVerifiedEmail(true);
                user.setName(query.get("name"));
                user.setStatus(true);

                if(query.get("picture") != null){
                    user.setPictureURL(query.get("picture"));
                }else{
                    user.setPictureURL("https://avatars.servers.getgo.com/2205256774854474505_medium.jpg");
                }

                user.setLocale(query.get("locale"));
                user.setGivenName(query.get("firstName"));
                user.setFamilyName(query.get("lastName"));

                Result<Boolean> res = userDao.insertUser(user);

                if (res.getData() == null) {
                    CommonTools.setResponse(response, res.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                } else {
                    try {

                        String email = query.get("email");
                        String password = query.get("password");

                        Result resl = userAuthorizationDao.updatePssword(email, password);

                        if(resl.getCode() == HttpServletResponse.SC_OK) {

                            JSONObject map = new JSONObject();
                            map.put("uuid", user.getUserUuid());
                            map.put("name", user.getName());
                            map.put("firstName", user.getGivenName());
                            map.put("lastName", user.getFamilyName());
                            map.put("email", user.getEmail());
                            map.put("picture", user.getPictureURL());
                            map.put("locale", user.getLocale());
                            map.put("status", true);

                            response.setContentType("application/json");
                            response.setCharacterEncoding("utf-8");
                            response.setStatus(user.getStatus() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_UNAUTHORIZED);
                            PrintWriter out = response.getWriter();

                            out.print(map.toString());
                        }else{
                            response.setContentType("application/json");
                            response.setCharacterEncoding("utf-8");
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                            CommonTools.setResponse(response, "Internal Server Error, Contact Administrator", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        }

                    } catch (IOException | JSONException e) {
                        CommonTools.setResponse(response, "Internal Server Error, Contact Administrator", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                }

            }
        }catch(NullPointerException e){
            CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
            CommonTools.setResponse(response, "Not a valid JSON response", HttpServletResponse.SC_BAD_REQUEST);
        }


    }

    @RequestMapping(value = "/oauth2/revoke", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        HashMap<String, String> query = CommonTools.getQueryMap(request.getQueryString());

        String token = query.get("token");

        if(query.isEmpty()) {
            CommonTools.setResponse(response, "Missing required parameters", HttpServletResponse.SC_BAD_REQUEST);
        }else{
            if(token == null || token.length()==0){
                CommonTools.setResponse(response, "Invalid token parameter", HttpServletResponse.SC_BAD_REQUEST);
            }else{
                User user = tokenDao.getSession(token).getData();

                if(user == null){
                    CommonTools.setResponse(response, "Token is not active", HttpServletResponse.SC_BAD_REQUEST);
                }else{
                    tokenDao.cleanAccess(user.getRefreshToken());
                    googleDao.revokeGAccess(user.getGRefreshToken());

                    Cookie[] cookies = request.getCookies();
                    CommonTools.eraseCookies(response, cookies);
                    CommonTools.setResponse(response, "OK logout successful", HttpServletResponse.SC_OK);
                }
            }
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.csrf().disable()
                .antMatcher("/**")
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/","/oauth2/revoke", "/oauth2/auth","/error**").permitAll()
                .antMatchers(HttpMethod.POST,"/oauth2/user","/oauth2/token", "/oauth2/client", "/oauth2/auth", "/oauth2/user/password").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(authenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        // @formatter:on
    }

    @Override
    public void configure(WebSecurity web) {
        // AuthenticationTokenFilter will ignore the below paths
        web.ignoring()
                .antMatchers(
                        HttpMethod.GET,
                        "/favicon.ico"
                );
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

}
