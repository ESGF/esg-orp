package esg.orp.app;

import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.Action;

import esg.orp.Parameters;
import esg.security.common.SAMLParameters;
import esg.security.policy.service.api.PolicyAttribute;
import esg.security.policy.service.api.PolicyService;
import esg.security.policy.service.impl.PolicyServiceLocalXmlImpl;


/**
 * Implementation of {@link PolicyServiceFilterCollaborator} that delegates to a 
 * {@link PolicyService} implemented through a local XML files.
 * 
 * @author Luca Cinquini
 *
 */
public class LocalXmlPolicyService implements PolicyServiceFilterCollaborator {
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    private PolicyService policyService;

    @Override
    public void init(FilterConfig filterConfig) {
        
        try {
            
            // initialize PolicyService from local XML files
            final String policyFiles = filterConfig.getInitParameter(Parameters.POLICY_FILES);
            policyService = new PolicyServiceLocalXmlImpl(policyFiles);
            if (LOG.isInfoEnabled()) LOG.info("Initialized PolicyService from XML policy files: "+policyFiles);
        
        } catch(Exception e) {
            LOG.error(e.getMessage());
        }

    }

    @Override
    public boolean isSecure(HttpServletRequest request) {

        // retrieve policies matching resource, operation
        final String resource = request.getRequestURL().toString();
        final String operation = Action.READ_ACTION;
        if (LOG.isDebugEnabled()) LOG.debug("Checking security on resource="+resource+" for operation="+operation);
        final List<PolicyAttribute> policies = policyService.getRequiredAttributes(resource, operation);
        
        // check for free access
        for (final PolicyAttribute policy : policies) {
            // found attribute that entitles free access
            if (policy.getType().equalsIgnoreCase(SAMLParameters.FREE_RESOURCE_ATTRIBUTE_TYPE)) return false;
        }

        // resource is secure
        return true;
        
    }

    @Override
    public void destroy() {}

}
