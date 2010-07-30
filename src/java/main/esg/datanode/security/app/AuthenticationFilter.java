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
package esg.datanode.security.app;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import esg.datanode.security.Parameters;
import esg.saml.auth.service.api.SAMLAuthenticationStatementFacade;
import esg.saml.auth.service.impl.SAMLAuthenticationStatementFacadeImpl;
import esg.saml.common.SAMLInvalidStatementException;

/**
 * Filter used to establish user authentication.
 * If a SAML-encoded cookie is NOT found as part of the HTTP request, the filter will redirect the request 
 * to an OpenID Relying Party end-point, with eventual redirection to the original requested URL.
 * If the cookie is found, the filter will validate its signature (versus its configured truststore), extract the user's OpenID,
 * and place it as the value of a request-scope attribute that can be used by downstream filters to establish authorization.
 * 
 * The pluggable implementation of {@link PolicyServiceFilterCollaborator} allows to bypass the access control workflow
 * for resources that are designated for unrestricted access - use {@link StrictPolicyService} to always enforce access control
 * for all resources.
 * 
 */
public class AuthenticationFilter extends AccessControlFilterTemplate {
	
	
	private PolicyServiceFilterCollaborator policyService;
	private String openidRelyingPartyUrl;
	
	private SAMLAuthenticationStatementFacade samlStatementFacade = new SAMLAuthenticationStatementFacadeImpl();

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
			
			// check authentication cookie in request, redirect if not found
			final Cookie cookie = WebUtils.getCookie(req, Parameters.OPENID_SAML_COOKIE);
			if (cookie==null) {
								
				// redirect to OpenID relying party, with redirect back to this URL
				// include "openid" request parameter if supplied
				final String redirectUrl = this.openidRelyingPartyUrl
				                         +"?"+Parameters.OPENID_REDIRECT+"="+URLEncoder.encode(url.toString(),"UTF-8") 
										 + (StringUtils.hasText(req.getParameter(Parameters.OPENID_IDENTITY)) ?
										    "&"+Parameters.OPENID_IDENTITY+"="+req.getParameter(Parameters.OPENID_IDENTITY) : "");
				if (LOG.isDebugEnabled()) LOG.debug("Authentication COOKIE NOT FOUND IN REQUEST, redirecting to: "+redirectUrl);				
				resp.sendRedirect(redirectUrl);

			} else {
				
				// extract OpenID from cookie, store in request
				if (LOG.isDebugEnabled()) LOG.debug("Authentication COOKIE FOUND in request: name="+cookie.getName()+" value="+cookie.getValue());
				try {
					final String authnStatement = URLDecoder.decode(cookie.getValue(),"UTF-8");
					final boolean validateSignature = true;
					final String openid = samlStatementFacade.parseAuthenticationStatement(authnStatement, validateSignature);
					if (LOG.isDebugEnabled()) LOG.debug("Extracted openid="+openid);
					req.setAttribute(Parameters.AUTHENTICATION_REQUEST_ATTRIBUTE, openid);
				} catch(SAMLInvalidStatementException e) {
					throw new ServletException(e);
				}
				
			}

		} else {
			// authorize this request
			if (LOG.isDebugEnabled()) LOG.debug("URL="+url+" is NOT secure, request is authorized");
			this.assertIsValid(req);
		}		

	}

	public void init(FilterConfig filterConfig) throws ServletException { 
		
		super.init(filterConfig);
		
		// set OpenID Relying Party URL
		this.openidRelyingPartyUrl = this.getMandatoryFilterParameter(Parameters.OPENID_RP_URL);
		
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
				
		// set trustore
		try {
			final String trustoreFile = this.getMandatoryFilterParameter(Parameters.TRUSTORE_FILE);
			final String trustorePassword = this.getMandatoryFilterParameter(Parameters.TRUSTORE_PASSWORD);
			final File trustore = new File(trustoreFile);
			samlStatementFacade.setTrustedCredentials(trustore, trustorePassword);
		} catch(Exception e) {
			throw new ServletException(e);
		}

		
		// embedded test to verify that assertions signed by Openid Relying Party can be validated
		// Note: this test depend on the user settings for the filter trustore
		if (LOG.isDebugEnabled()) {
			
			try {
				
				final String tmpDir = System.getProperty("java.io.tmpdir");
				final File file = new File(tmpDir,Parameters.TEST_FILE);
				LOG.debug("Reading test XML assertion from file:"+file.getAbsolutePath());
				final String xml = URLDecoder.decode(FileUtils.readFileToString(file),"UTF-8"); // simulate cookie decoding
				LOG.debug(xml);
		        final boolean validate = true;
		        final String identity = samlStatementFacade.parseAuthenticationStatement(xml, validate);
		        LOG.debug("Comparing assertion identites, result="+identity.equals(Parameters.TEST_OPENID));
				
			} catch(Exception e) {
				LOG.warn(e);
			}
			
		}
		
	}
	   
}
