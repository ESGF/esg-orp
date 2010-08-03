package esg.datanode.security.app;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.datanode.security.Parameters;

/**
 * Filter that establishes authentication by checking if the request originates from a trusted IP address.
 * 
 * The pluggable implementation of {@link PolicyServiceFilterCollaborator} allows to bypass the access control workflow
 * for resources that are designated for unrestricted access - use {@link StrictPolicyService} to always enforce access control
 * for all resources.
 * 
 */
public class AuthenticationByIPFilter extends AccessControlFilterTemplate {
	
	
	private PolicyServiceFilterCollaborator policyService;
	private String authorizedIP;
	
	private final Log LOG = LogFactory.getLog(this.getClass());
			
	/**
	 * {@inheritDoc}
	 */
	public void attemptValidation(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain chain) 
				throws IOException, ServletException {
				
		final String url = this.getUrl(req);
		
		// check URL policy
		if (policyService.isSecure(req)) {
			
			if (LOG.isDebugEnabled()) LOG.debug("URL="+url+" is secure");
			
			String addr = req.getRemoteAddr();
			if ( authorizedIP.equals(addr) ) {
				req.setAttribute(Parameters.AUTHORIZATION_REQUEST_ATTRIBUTE, true);
			}

		} else {
			// authorize this request
			if (LOG.isDebugEnabled()) LOG.debug("URL="+url+" is NOT secure, request is authorized");
			this.assertIsValid(req);
		}		

	}

	public void init(FilterConfig filterConfig) throws ServletException { 
		
		super.init(filterConfig);
		
		// read trusted IP address from filter configuration
		this.authorizedIP = this.getMandatoryFilterParameter(Parameters.AUTHORIZED_IP);
		
		
		// instantiate and initialize PolicyService
		try {
			final String policyServiceClass = this.getMandatoryFilterParameter(Parameters.POLICY_SERVICE);
			this.policyService = (PolicyServiceFilterCollaborator)Class.forName(policyServiceClass).newInstance();
			this.policyService.init(filterConfig);
		} catch(ClassNotFoundException e) {
			throw new ServletException(e.getMessage());
		} catch(InstantiationException e) {
			throw new ServletException(e.getMessage());
		} catch(IllegalAccessException e) {
			throw new ServletException(e.getMessage());
		}
		
	}
	   
}