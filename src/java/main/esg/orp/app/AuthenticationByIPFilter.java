/*******************************************************************************
 * Copyright (c) 2010 Earth System Grid Federation
 * ALL RIGHTS RESERVED. 
 * U.S. Government sponsorship acknowledged.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package esg.orp.app;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.orp.Parameters;

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