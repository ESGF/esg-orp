package esg.orp.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.orp.Parameters;
import esg.orp.Utils;
import esg.orp.app.cookie.DecryptionException;
import esg.orp.app.cookie.UserDetailsCookie;

/**
 * Servlet Filter implementation class AuthRedirectFilter
 * 
 * @author William Tucker
 */
public class AuthenticationRedirectFilter extends AccessControlFilterTemplate
{

    private PolicyServiceFilterCollaborator policyService;
    
    private String requestAttribute;
    
    private URL authenticateUrl;
    private String returnQueryName;
    private String sessionCookieName;
    private String secretKey;
    
    private static final String RETURN_QUERY_NAME_DEFAULT = "r";
    
    private static final Log LOG = LogFactory.getLog(AuthenticationRedirectFilter.class);
    
    @Override
    void attemptValidation(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        // check URL policy before attempting authentication
        if (!this.policyService.isSecure(request))
        {
            // authorize this request
            if (LOG.isDebugEnabled())
            {
                String url = Utils.getFullRequestUrl(request);
                LOG.debug(String.format(
                        "URL=%s is NOT secure, request is authorized", url));
            }
            
            this.assertIsValid(request);
        }
        else
        {
            // retrieve session cookie
            String cookieValue = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null)
            {
                for (Cookie cookie: cookies)
                {
                    if (cookie.getName().equals(this.sessionCookieName))
                    {
                        cookieValue = cookie.getValue();
                        
                        if (LOG.isDebugEnabled())
                            LOG.debug(String.format("Found session cookie: %s", this.sessionCookieName));
                    }
                }
            }
            
            if (cookieValue == null)
            {
                // session cookie not found
                // redirect request to authentication service
                StringBuffer requestUrl = request.getRequestURL();
                
                String query = request.getQueryString();
                if (query != null)
                {
                    requestUrl.append('?').append(query);
                }
                
                try
                {
                    String redirectUrl = getRedirectUrl(requestUrl.toString());
                    
                    // send the redirect
                    response.sendRedirect(redirectUrl);
                    
                    if (LOG.isDebugEnabled())
                        LOG.debug(String.format(
                                "Session cookie not found; redirecting to: %s", redirectUrl));
                }
                catch (MalformedURLException | UnsupportedEncodingException e)
                {
                    LOG.error("Failed to construct redirect reponse.", e);
                }
            }
            else
            {
                // determine userID from session cookie
                String userID = null;
                try
                {
                    // parse a user ID from the cookie value
                    UserDetailsCookie sessionCookie = UserDetailsCookie.parseCookie(
                            cookieValue,
                            this.secretKey);
                    userID = sessionCookie.getUserID();
                    
                    if (LOG.isDebugEnabled())
                        LOG.debug(String.format("Found user ID: %s, cookie timestamp: %s",
                                userID, sessionCookie.getTimestamp()));
                }
                catch (NoSuchAlgorithmException | NoSuchPaddingException e)
                {
                    LOG.error("Failed to load decoding/decryption handlers.", e);
                }
                catch (DecoderException | DecryptionException e)
                {
                    if (LOG.isDebugEnabled())
                        LOG.debug(String.format("Problem parsing cookie value: %s", cookieValue), e);
                }
                
                if (userID == null)
                {
                    // userID not found in cookie
                    // send 401 response
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found.");
                }
                else
                {
                    // set request attribute indicating authentication success
                    request.setAttribute(this.requestAttribute, userID);
                    if (LOG.isDebugEnabled())
                        LOG.debug(String.format("Setting '%s' attribute", this.requestAttribute));
                }
            }
        }
    }
    
    /**
     * @see Filter#destroy()
     */
    public void destroy()
    {
        super.destroy();
    }
    
    /**
     * @see Filter#init(FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        super.init(filterConfig);
        
        // mandatory settings
        this.setAuthenticateUrl(this.getMandatoryFilterParameter("authenticateUrl"));
        this.setSessionCookieName(this.getMandatoryFilterParameter("sessionCookieName"));
        this.setSecretKey(this.getMandatoryFilterParameter("secretKey"));
        this.setRequestAttribute(this.getMandatoryFilterParameter("requestAttribute"));
        
        // optional
        this.setReturnQueryName(this.getOptionalFilterParameter("returnQueryName"));
        if (this.returnQueryName == null)
        {
            this.returnQueryName = RETURN_QUERY_NAME_DEFAULT;
        }
        
        // instantiate and initialize PolicyService
        try
        {
            final String policyServiceClass = this.getMandatoryFilterParameter(Parameters.POLICY_SERVICE);
            this.policyService = (PolicyServiceFilterCollaborator)Class.forName(policyServiceClass).newInstance();
            this.policyService.init(filterConfig);
        }
        catch(ClassNotFoundException | InstantiationException | IllegalAccessException e)
        {
            throw new ServletException(e.getMessage());
        }
    }
    
    /**
     * Construct a redirection URL based on config settings
     * 
     * @param returnUrl URL to return to after authentication
     * @return  redirect URL
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    public String getRedirectUrl(String returnUrl) throws MalformedURLException, UnsupportedEncodingException
    {
        String query = this.authenticateUrl.getQuery();
        
        String queryPrefix = "";
        if (query != null)
        {
            if (query != "" && !query.endsWith("&"))
            {
                queryPrefix = "&";
            }
        }
        else
        {
            queryPrefix = "?";
        }
        
        returnUrl = URLEncoder.encode(returnUrl, "UTF-8");
        
        URL redirectUrl = new URL(String.format("%s%s%s=%s",
                this.authenticateUrl,
                queryPrefix,
                this.returnQueryName,
                returnUrl
            ));
        
        return redirectUrl.toString();
    }
    
    /**
     * Setter for requestAttribute
     * 
     * @param requestAttribute  Attribute name indicating authentication success
     */
    public void setRequestAttribute(String requestAttribute)
    {
        this.requestAttribute = requestAttribute;
    }
    
    /**
     * Setter for authenticateUrl
     * 
     * @param authenticateUrl   URL to redirect requests to for authentication
     */
    public void setAuthenticateUrl(String authenticateUrl)
    {
        this.authenticateUrl = null;
        
        if (authenticateUrl != null)
        {
            try
            {
                this.authenticateUrl = new URL(authenticateUrl);
            }
            catch (MalformedURLException e)
            {
                LOG.error(String.format("%s is not a valid URL", authenticateUrl), e);
            }
        }
    }
    
    /**
     * Setter for returnQueryName
     * 
     * @param returnQueryName   Redirect URL query parameter name
     */
    public void setReturnQueryName(String returnQueryName)
    {
        this.returnQueryName = returnQueryName;
    }
    
    /**
     * Setter for sessionCookieName
     * 
     * @param sessionCookieName Name of the authentication service's authentication cookie
     */
    public void setSessionCookieName(String sessionCookieName)
    {
        this.sessionCookieName = sessionCookieName;
    }
    
    /**
     * Setter for secretKey
     * 
     * @param secretKey The secret key used to encyrpt the user authentication cookie
     */
    public void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }

}
