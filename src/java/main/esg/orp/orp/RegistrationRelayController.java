package esg.orp.orp;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import esg.orp.Parameters;
import esg.orp.utils.HttpUtils;
import esg.security.authn.service.api.SAMLAuthentication;
import esg.security.common.SAMLParameters;
import esg.security.policy.service.api.PolicyAttribute;
import esg.security.policy.web.PolicySerializer;
import esg.security.registration.web.RegistrationRequestUtils;
import esg.security.registration.web.RegistrationResponseUtils;

/**
 * Controller that performs registration relay operations.
 * This controller acts as the ORP client to the ESGF security services.
 *
 * @author Luca Cinquini
 */
@Controller
@RequestMapping("/registration-relay.htm") 
public class RegistrationRelayController {
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    private final static String POLICY_ATTRIBUTES_KEY = "policyAttributes";
    private final static String POLICY_SERVICE_URI = "/esgf-security/secure/policyService.htm";
    private final static String ACTION = "Read";
    
    /**
     * GET method invokes the remote PolicyService, parses the XML response, and displays the group selection form.
     * 
     * @param resource : the URL of the resource to be accessed
     * @return
     * @throws ServletException
     */
    @RequestMapping(method = { RequestMethod.GET } )
    public String doGet(final HttpServletRequest request, final HttpServletResponse response, final Model model) throws ServletException {  

        final String resource = request.getParameter(Parameters.HTTP_PARAMETER_RESOURCE);
        if (LOG.isDebugEnabled()) LOG.debug("Requested resource="+resource);
        
        // build policy service URL (hosted on the same servlet container)
        final String url = "https://"+request.getServerName()+":"+request.getServerPort()+POLICY_SERVICE_URI;
        final Map<String,String> pars= new HashMap<String,String>();
        pars.put(SAMLParameters.HTTP_PARAMETER_RESOURCE, resource);
        pars.put(SAMLParameters.HTTP_PARAMETER_ACTION, ACTION);
        
        if (LOG.isDebugEnabled()) LOG.debug("Invoking policy service at: "+url);
                                
        try {
            
            // execute HTTP GET request to PolicyService
            String xml = HttpUtils.get(url, pars);       
            
            // deserialize XML
            Map<PolicyAttribute, List<URL>> attributes = PolicySerializer.deserialize(xml);
            
            // return required attributes to view
            model.addAttribute(POLICY_ATTRIBUTES_KEY, attributes);
            
        } catch(Exception e) {
            throw new ServletException(e.getMessage());
        }
        
        return "registration-relay";
                
    }
    
    /**
     * POST method invokes the remote registration service, parses the XML response, and redirects to the view.
     * 
     * @param request
     * @param response
     * @param model
     * @return
     * @throws ServletException
     */
    @RequestMapping(method = { RequestMethod.POST } )
    public void doPost(final HttpServletRequest request, final HttpServletResponse response, final Model model) throws ServletException {  
        
        // retrieve POST request parameters
        final String user = request.getParameter(Parameters.HTTP_PARAMETER_USER);
        final String group = request.getParameter(Parameters.HTTP_PARAMETER_GROUP);
        final String role = request.getParameter(Parameters.HTTP_PARAMETER_ROLE);
        final String url = request.getParameter(Parameters.HTTP_PARAMETER_URL);
        final String resource = request.getParameter(Parameters.HTTP_PARAMETER_RESOURCE);
        
        // compare user to session authentication
        final SAMLAuthentication authentication = (SAMLAuthentication)request.getSession(true).getAttribute(Parameters.SESSION_AUTH);
        if (authentication==null) throw new ServletException("User not authenticated");
        if (!user.equals(authentication.getIdentity())) {
            throw new ServletException("Identity mismatch: post parameter="+user+" session identity="+authentication.getIdentity());
        }
       
        try {
        
            // build registration request XML
            String xmlRequest = RegistrationRequestUtils.serialize(user, group, role);
            if (LOG.isInfoEnabled()) LOG.info("Submitting registration request: "+xmlRequest+"\n to URL: "+url);
            
            // execute HTTP POST request to PolicyService
            final Map<String,String> pars = new HashMap<String,String>();
            pars.put(Parameters.HTTP_PARAMETER_XML, xmlRequest);
            String xmlResponse = HttpUtils.post(url, pars);      
            if (LOG.isInfoEnabled()) LOG.info("Received registration response: "+xmlResponse);
            
            // deserialize XML
            final String result = RegistrationResponseUtils.deserialize(xmlResponse);
            
            // FIXME
            response.setContentType("text/xml");
            response.getWriter().write( xmlResponse );
            
            
        } catch(Exception e) {
            throw new ServletException(e);
        }
        

    }
    
}
