package com.proindiemusic.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proindiemusic.backend.domain.User;
import com.proindiemusic.backend.pojo.CommonTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("Duplicates")
@Component
@PropertySource("classpath:config.properties")
public class JwtAuthorizationTokenFilter extends OncePerRequestFilter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String tokenHeader;

    private final String check;

    public JwtAuthorizationTokenFilter(@Value("${jwt.header}") String tokenHeader, @Value("${user.endpoint}") String check ) {
        this.check = check;
        this.tokenHeader = tokenHeader;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String requestHeader = request.getHeader(this.tokenHeader);

        String authToken;

        logger.debug(requestHeader);
        if (requestHeader != null && requestHeader.startsWith("Bearer ")) {
            authToken = requestHeader.substring(7);


            URL url = new URL(check);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Authorization", "Bearer "+authToken);

            int status = con.getResponseCode();

            if(status == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();

                try {
                    JSONObject json = new JSONObject(content.toString());

                    Authentication authentication = new Authentication() {

                        Boolean b= false;

                        @Override
                        public Collection<? extends GrantedAuthority> getAuthorities() {
                            List<GrantedAuthority> roles = new ArrayList<>();
                            try {
                                String role = String.valueOf(json.getJSONObject("authentication").get("authorities"));
                                if(role.contains("[")){
                                    if(!role.equalsIgnoreCase("[]")) {
                                        String[] ar = role.substring(1, role.length() - 1).split(",");
                                        for (String anAr : ar) {
                                            roles.add(new SimpleGrantedAuthority(anAr.trim()));
                                        }
                                    }
                                }else {
                                    roles.add(new SimpleGrantedAuthority(role.trim()));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return roles;
                        }

                        @Override
                        public Object getCredentials() {
                            return authToken;
                        }

                        @Override
                        public Object getDetails() {
                            return null;
                        }

                        @Override
                        public Object getPrincipal() {
                            try {
                                JSONObject principal= json.getJSONObject("principal");
                                ObjectMapper m = new ObjectMapper();
                                try {
                                    User user = m.readValue(principal.toString(), User.class);
                                    user.setAccessToken(authToken);
                                    user.setRoles(String.valueOf(json.getJSONObject("authentication").get("authorities")));
                                    return user;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return principal;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            return null;
                        }

                        @Override
                        public boolean isAuthenticated() {
                            return b;
                        }

                        @Override
                        public void setAuthenticated(boolean b) throws IllegalArgumentException {
                            this.b = b;
                        }

                        @Override
                        public String getName() {
                            try {
                                return String.valueOf(json.getJSONObject("principal").get("email"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            return null;
                        }
                    };

                    authentication.setAuthenticated(true);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                chain.doFilter(request, response);
            }else if(status == 401){
                CommonTools.setResponse(response, "Not Authorized, token expired", HttpServletResponse.SC_UNAUTHORIZED);
            }else{
                logger.debug(String.valueOf(status));
                CommonTools.setResponse(response, "Unknown Error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } else {
            if(!request.getRequestURI().contains("/login")) {
                CommonTools.setResponse(response, "Not Authorized, no bearer token", HttpServletResponse.SC_UNAUTHORIZED);
            }else{
                chain.doFilter(request, response);
            }
        }

    }
}
