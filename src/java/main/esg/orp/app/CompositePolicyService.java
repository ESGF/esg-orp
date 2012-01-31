package esg.orp.app;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.orp.Parameters;

/**
 * Manager implementation of {@link PolicyServiceFilterCollaborator}.
 * This class returns isSecure=true by default, unless at least one of its underlying
 * {@link PolicyServiceFilterCollaborator}s returns isSecure=false.
 * @author Luca Cinquini
 *
 */
public class CompositePolicyService implements PolicyServiceFilterCollaborator {
    
    private final Log LOG = LogFactory.getLog(this.getClass());
    
    final List<PolicyServiceFilterCollaborator> policyServices = new ArrayList<PolicyServiceFilterCollaborator>();

    @Override
    public void init(final FilterConfig filterConfig) {
        
        final String classes = filterConfig.getInitParameter(Parameters.POLICY_SERVICES);
        for (final String clazz : classes.split("\\s*,\\s*")) {
            
            // instantiate and initialize one PolicyService at a time
            // log error if any class is not found
            try {
                PolicyServiceFilterCollaborator policyService = (PolicyServiceFilterCollaborator)Class.forName(clazz).newInstance();
                policyService.init(filterConfig);
                policyServices.add(policyService);
                LOG.info("Adding policy service class: "+clazz);
            } catch(ClassNotFoundException e) {
                LOG.error(e.getMessage());
            } catch(InstantiationException e) {
                LOG.error(e.getMessage());
            } catch(IllegalAccessException e) {
                LOG.error(e.getMessage());
            }
            
        }

    }

    @Override
    public boolean isSecure(HttpServletRequest request) {
        
        // loop over managed policy services
        for (final PolicyServiceFilterCollaborator policyService : this.policyServices) {
            // request is not secure for at least one policy service
            if (!policyService.isSecure(request)) return false;
        }
        
        return true;
    }

    @Override
    public void destroy() {}

}
