/*******************************************************************************
 * Copyright (c) 2011 Earth System Grid Federation
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

import javax.servlet.FilterConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import esg.orp.Parameters;

/**
 * This filter can be configured with a comma-separated list of Authorization Services to query, in sequence.
 * The actual invocation of the (one or more) Authorization Services is delegated to the underlying SAML Authorizer.
 * 
 * @author Luca Cinquini
 */
public class SAMLAuthorizationServiceFilterCollaborator implements AuthorizationServiceFilterCollaborator {
		
	/**
	 * Client used to query the remote Authorization Services.
	 */
	private final SAMLAuthorizer authorizer;
	
	private final Log LOG = LogFactory.getLog(this.getClass());
		
	public SAMLAuthorizationServiceFilterCollaborator() {
		
	    authorizer = new SAMLAuthorizer();
		
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean authorize(final String user, final String url, final String operation)  {
		
	    return authorizer.authorize(user, url, operation);
	
	}

	public void init(FilterConfig filterConfig) {
				
	    // remove white spaces and split at ","
		String[] endpoints = filterConfig.getInitParameter(Parameters.AUTHORIZATION_SERVICE_URL).replaceAll("\\s+", "").split(",");
		Assert.isTrue(endpoints.length>0, "Missing Authorization Service URL(s) in filter configuration");
		for (int i=0; i<endpoints.length; i++) {
		    log("Authorization Filter configured with service endpoint="+endpoints[i]);
		}
		authorizer.setEndpoints(endpoints);

	}
	
    public void destroy() {}
	
	private void log(final String message) {
		if (LOG.isDebugEnabled()) LOG.debug(message);
	}
	
}
