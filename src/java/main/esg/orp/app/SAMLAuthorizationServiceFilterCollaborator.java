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

import javax.servlet.FilterConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.DecisionTypeEnumeration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import esg.orp.Parameters;
import esg.saml.authz.service.api.SAMLAuthorization;
import esg.saml.authz.service.api.SAMLAuthorizations;
import esg.saml.authz.service.impl.SAMLAuthorizationServiceClientSoapImpl;
import esg.saml.common.SOAPServiceClient;

public class SAMLAuthorizationServiceFilterCollaborator implements AuthorizationServiceFilterCollaborator {
	
	/**
	 * The URL of the remote SAML Authorization Service.
	 */
	private String endpoint;
	
	/**
	 * Client used to encode/decode the authorization request into/from the SAML/SOAP document.
	 */
	private final SAMLAuthorizationServiceClientSoapImpl encoder;
	
	/**
	 * Client used for SOAP/HTTP communication to the SAML Authorization Service.
	 */
	private final SOAPServiceClient transmitter;
	
	private final Log LOG = LogFactory.getLog(this.getClass());
		
	public SAMLAuthorizationServiceFilterCollaborator() {
		
		encoder = new SAMLAuthorizationServiceClientSoapImpl("test issuer");
		transmitter = new SOAPServiceClient();
		
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean authorize(final String user, final String url, final String operation)  {
		
		log("Authorizing user="+user+" url="+url+" operation="+operation);
		
		try {
			
			String soapRequest = encoder.buildAuthorizationRequest(user, url, operation);
			log(soapRequest);
			String soapResponse = transmitter.doSoap(endpoint, soapRequest);
			log(soapResponse);
			
			// parse the authorization statements
			return this.parseAuthorizationStatement(soapResponse, user, url, operation);
			
		} catch(Exception e) {
			LOG.warn(e.getMessage(), e);
			return false;
		}
	
	}
	
	/**
	 * Testable method that parses the authorization statement and matches it versus the requested authorization parameters
	 */
	boolean parseAuthorizationStatement(final String authzStatement, final String user, final String url, final String operation) throws Exception {
		
		boolean authorized = false;
					
		// parse the authorization statements
		final SAMLAuthorizations authorizations = encoder.parseAuthorizationResponse(authzStatement);
		
		if (authorizations.getIdentity().equals(user)) {
			log("Matched user="+user);
			for (final SAMLAuthorization authz : authorizations.getAuthorizations()) {
				
				if (authz.getResource().equals(url)) {
					log("Matched URL="+url);
					for (final String action : authz.getActions()) {
						if (action.equalsIgnoreCase(operation)) {
							log("Matched operation="+operation);
							if (authz.getDecision().equals(DecisionTypeEnumeration.PERMIT.toString())) authorized = true;
							else return false;
						}
					}
				}
				
			}
		}
			
		return authorized;
	
	}
	
	

	public void destroy() {}

	public void init(FilterConfig filterConfig) {
				
		endpoint = filterConfig.getInitParameter(Parameters.AUTHORIZATION_SERVICE_URL);
		Assert.isTrue(StringUtils.hasText(endpoint), "Missing Authorization Service URL in filter configuration");

	}
	
	private void log(final String message) {
		if (LOG.isDebugEnabled()) LOG.debug(message);
	}
	
}
