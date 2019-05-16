package esg.orp.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

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

import esg.orp.Parameters;

/**
 * Implementation of AuthorizationEnforcer which will redirect unauthorized and
 * anonymous requests to a designated authentication service.
 * 
 * @author William Tucker
 */
public class RedirectAuthorizationEnforcer implements Filter, AuthorizationEnforcer {

    private URL authenticateUrl;
    private String returnQueryName;

    private static final String RETURN_QUERY_NAME_DEFAULT = "r";

    private static final Log LOG = LogFactory.getLog(RedirectAuthorizationEnforcer.class);

    /**
     * @see Filter#destroy()
     */
    public void destroy() {
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        boolean authorized = false;

        Object attribute = request.getAttribute(Parameters.AUTHORIZATION_REQUEST_ATTRIBUTE);
        if (attribute instanceof Boolean)
            authorized = (boolean)attribute;

        if (!authorized) {
            // Check authentication
            String user = (String)request.getAttribute(Parameters.AUTHENTICATION_REQUEST_ATTRIBUTE);
            if (user == null) {
                // Not authenticated
                // Redirect request to authentication service
                StringBuffer requestUrl = httpRequest.getRequestURL();

                String query = httpRequest.getQueryString();
                if (query != null) {
                    requestUrl.append('?').append(query);
                }

                try {
                    String redirectUrl = getRedirectUrl(requestUrl.toString());

                    // send the redirect
                    httpResponse.sendRedirect(redirectUrl);

                    if (LOG.isDebugEnabled())
                        LOG.debug(String.format("Session cookie not found; redirecting to: %s", redirectUrl));
                } catch (MalformedURLException | UnsupportedEncodingException e) {
                    LOG.error("Failed to construct redirect reponse.", e);
                }
            } else {
                if (!response.isCommitted()) {
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
                }
            }
        }

        // pass the request along the filter chain
        chain.doFilter(request, response);
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        if (filterConfig != null) {
            this.setAuthenticateUrl(filterConfig.getInitParameter("authenticateUrl"));
            this.setReturnQueryName(filterConfig.getInitParameter("returnQueryName"));
        }

        if (this.returnQueryName == null) {
            this.returnQueryName = RETURN_QUERY_NAME_DEFAULT;
        }
    }

    /**
     * Construct a redirection URL based on config settings
     * 
     * @param returnUrl URL to return to after authentication
     * @return redirect URL
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    public String getRedirectUrl(String returnUrl) throws MalformedURLException, UnsupportedEncodingException {
        String query = this.authenticateUrl.getQuery();

        String queryPrefix = "";
        if (query != null) {
            if (query != "" && !query.endsWith("&")) {
                queryPrefix = "&";
            }
        } else {
            queryPrefix = "?";
        }

        returnUrl = URLEncoder.encode(returnUrl, "UTF-8");

        URL redirectUrl = new URL(
                String.format("%s%s%s=%s", this.authenticateUrl, queryPrefix, this.returnQueryName, returnUrl));

        return redirectUrl.toString();
    }

    /**
     * Setter for authenticateUrl
     * 
     * @param authenticateUrl URL to redirect requests to for authentication
     */
    public void setAuthenticateUrl(String authenticateUrl) {
        this.authenticateUrl = null;

        if (authenticateUrl != null) {
            try {
                this.authenticateUrl = new URL(authenticateUrl);
            } catch (MalformedURLException e) {
                LOG.error(String.format("%s is not a valid URL", authenticateUrl), e);
            }
        }
    }

    /**
     * Setter for returnQueryName
     * 
     * @param returnQueryName Redirect URL query parameter name
     */
    public void setReturnQueryName(String returnQueryName) {
        this.returnQueryName = returnQueryName;
    }

}
