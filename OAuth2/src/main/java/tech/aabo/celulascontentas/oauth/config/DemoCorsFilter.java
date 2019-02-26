package tech.aabo.celulascontentas.oauth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.aabo.celulascontentas.oauth.pojo.CommonTools;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cors settings to allow connections to all origins.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DemoCorsFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        HttpServletResponse response = (HttpServletResponse)res;
        HttpServletRequest request = (HttpServletRequest)req;

        String origin = CommonTools.transformName(request, 1);

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET");
        response.setHeader("Access-Control-Allow-Headers",  "Origin, X-Requested-With, Content-Type, Accept, Authorization");


        if (!request.getMethod().equalsIgnoreCase("OPTIONS")) {
            try {
                chain.doFilter(req, res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void init(FilterConfig filterConfig) { }

    public void destroy() { }
}
