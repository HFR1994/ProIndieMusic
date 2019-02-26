package tech.aabo.celulascontentas.oauth.filter;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tech.aabo.celulascontentas.oauth.pojo.CommonTools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

@Component
public class UnauthorizedHandler implements AuthenticationEntryPoint, Serializable {


    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException e){

        CommonTools.setResponse(response,"Full authentication is required to access this resource",HttpServletResponse.SC_UNAUTHORIZED);
    }
}


