package esg.orp.orp;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import esg.orp.Parameters;
import esg.security.policy.service.api.PolicyAttribute;
import esg.security.policy.web.PolicySerializer;

/**
 * Controller that displays a page containing registration services endpoints.
 *
 * @author Luca Cinquini
 */
@Controller
public class RegistrationRelayController {
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    private final static String POLICY_ATTRIBUTES_KEY = "policyAttributes";
    
    @RequestMapping("/registration-relay.htm")  
    public String execute(final HttpServletRequest request, final HttpServletResponse response, final Model model) throws ServletException {  

        final String resource = request.getParameter(Parameters.RESOURCE);
        if (LOG.isDebugEnabled()) LOG.debug("Requested resource="+resource);
        final String action = "Read";
        // FIXME ?
        //final String url = "https://"+request.getServerName()+":8443/esgf-security/policyService.htm"
        final String url = "http://"+request.getServerName()+":8080/esgf-security/policyService.htm"
                         + "?resource="+resource
                         + "&action="+action;
        if (LOG.isDebugEnabled()) LOG.debug("Invoking policy service at: "+url);
                        
        // execute HTTP query to (local) PolicyService
        String xml = this.getXml(url);       
        
        try {
            // deserialize XML
            Map<PolicyAttribute, List<URL>> attributes = PolicySerializer.deserialize(xml);
            // return required attributes to view
            model.addAttribute(POLICY_ATTRIBUTES_KEY, attributes);
        } catch(Exception e) {
            throw new ServletException(e.getMessage());
        }
        
        return "registration-relay";
        
    }
    
    private String getXml(String url) throws ServletException {
        
        // create an instance of HttpClient.
        HttpClient client = new HttpClient();

        // create a method instance.
        GetMethod method = new GetMethod(url);
        
        // provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        try {
            
          // execute the method.
          int statusCode = client.executeMethod(method);

          if (statusCode != HttpStatus.SC_OK) {
            throw new ServletException("Invocation of policy service at:"+url+" failed: " + method.getStatusLine());
          }

          // read the response body.
          byte[] responseBody = method.getResponseBody();

          // use caution: ensure correct character encoding and is not binary data
          return(new String(responseBody));

        } catch (HttpException e) {
            LOG.warn(e.getMessage());
            throw new ServletException("Fatal protocol violation: " + e.getMessage());
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            throw new ServletException("Fatal transport error: " + e.getMessage());
        } finally {
          // release the connection.
          method.releaseConnection();
        }  
        
    }
    
}
