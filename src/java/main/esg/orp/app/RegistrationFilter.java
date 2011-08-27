package esg.orp.app;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import esg.orp.Parameters;

/**
 * Filter that redirects the request to a registration relay page,
 * if the request has not been already authorized by the upstream filter chain.
 * This filter must come LAST in the access control filter chain.
 * 
 * @author Luca Cinquini
 *
 */
public class RegistrationFilter implements Filter {
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    private FilterConfig filterConfig;
    
    private String registrationRelayUrl;

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        
        final HttpServletRequest req = (HttpServletRequest)request;
        final HttpServletResponse resp = (HttpServletResponse)response;
        final Boolean authzAtt = (Boolean)request.getAttribute(Parameters.AUTHORIZATION_REQUEST_ATTRIBUTE);
        if (LOG.isDebugEnabled()) LOG.debug("RegistrationFilter: current authorization attribute="+authzAtt);

        // request not authorized > redirect to registration relay page
        if (authzAtt==null || authzAtt.booleanValue()==false) {
            
            if (!response.isCommitted()) {
                if (LOG.isDebugEnabled()) LOG.debug("Redirecting to: "+registrationRelayUrl);
                resp.sendRedirect(registrationRelayUrl);
            }
        
        // request authorized > keep processing
        } else {
            chain.doFilter(request, response);
        }
    }

    public void init(final FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig; 
        this.registrationRelayUrl = filterConfig.getInitParameter(Parameters.REGISTRATION_RELAY_URL);
        Assert.isTrue(StringUtils.hasText(this.registrationRelayUrl), "Missing filter configuration parameter: "+Parameters.REGISTRATION_RELAY_URL);   
    }
    
    public void destroy() { this.filterConfig = null; }

}
