package esg.orp.app;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.orp.Parameters;
import esg.orp.app.cookie.DecryptionException;
import esg.orp.app.cookie.UserDetailsCookie;

/**
 * Filter which establishes a user's identity by reading their ID from an encrypted cookie.
 * 
 * @author William Tucker
 */
public class CookieAuthenticationFilter extends AccessControlFilterTemplate {

    private String sessionCookieName;
    private String secretKey;

    private final Log LOG = LogFactory.getLog(this.getClass());

    /**
     * @see Filter#destroy()
     */
    public void destroy() {
    }

    @Override
    void attemptValidation(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // retrieve session cookie
        String cookieValue = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(this.sessionCookieName)) {
                    cookieValue = cookie.getValue();

                    if (LOG.isDebugEnabled())
                        LOG.debug(String.format("Found session cookie: %s", this.sessionCookieName));
                }
            }
        }

        if (cookieValue != null) {
            // determine userID from session cookie
            String userID = null;
            try {
                // parse a user ID from the cookie value
                UserDetailsCookie sessionCookie = UserDetailsCookie.parseCookie(cookieValue, this.secretKey);
                userID = sessionCookie.getUserID();

                if (LOG.isDebugEnabled())
                    LOG.debug(String.format("Found user ID: %s, cookie timestamp: %s", userID,
                            sessionCookie.getTimestamp()));
            }
            catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                LOG.error("Failed to load decoding/decryption handlers.", e);
            }
            catch (DecoderException | DecryptionException e) {
                if (LOG.isDebugEnabled())
                    LOG.debug(String.format("Problem parsing cookie value: %s", cookieValue), e);
            }

            if (userID == null) {
                LOG.warn("userID not found in cookie.");
            }
            else {
                // set request attribute indicating authentication success
                request.setAttribute(Parameters.AUTHENTICATION_REQUEST_ATTRIBUTE, userID);
                if (LOG.isDebugEnabled())
                    LOG.debug(String.format("Setting '%s' attribute", Parameters.AUTHENTICATION_REQUEST_ATTRIBUTE));
            }
        }
        else if (LOG.isDebugEnabled()) {
            LOG.debug(String.format("Session cookie (%s) not found. Skipping authentication.", this.sessionCookieName));
        }

        // pass the request along the filter chain
        chain.doFilter(request, response);
    }

    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();

        String sessionCookieName = servletContext.getInitParameter("sessionCookieName");
        if (sessionCookieName == null)
            LOG.error("Missing context parameter: sessionCookieName");
        this.setSessionCookieName(sessionCookieName);

        String sessionCookieSecret = servletContext.getInitParameter("sessionCookieSecret");
        if (sessionCookieSecret == null)
            LOG.error("Missing context parameter: sessionCookieSecret");
        this.setSecretKey(sessionCookieSecret);

        if (sessionCookieName != null && sessionCookieSecret != null)
            LOG.info(String.format("Authentication Filter configured with cookie: %s", this.sessionCookieName));
    }

    /**
     * Setter for sessionCookieName
     * 
     * @param sessionCookieName Name of the authentication service's authentication
     *                          cookie
     */
    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    /**
     * Setter for secretKey
     * 
     * @param secretKey The secret key used to encyrpt the user authentication
     *                  cookie
     */
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

}
