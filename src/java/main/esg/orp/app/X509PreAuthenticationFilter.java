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
import java.security.cert.X509Certificate;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.orp.Parameters;

/**
 * Servlet filter that tries to detect the presence of an X509 client certificate,
 * and if found sets the authentication request attribute, thus avoiding an HTTP redirect
 * response to the Relying Party. 
 * It must be invoked before the {@link AuthenticationFilter} in the filter chain.
 * 
 * Note: the X509 client certificate can only be detected for HTTPS requests.
 * Note: DO NOT USE THIS FILTER IN PRODUCTION (not fully tested).
 * @author luca.cinquini
 *
 */
public class X509PreAuthenticationFilter extends AccessControlFilterTemplate {
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	private final static String X509_ATTRIBUTE = "javax.servlet.request.X509Certificate";
			
	/**
	 * {@inheritDoc}
	 */
	public void attemptValidation(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain chain) 
		throws IOException, ServletException {
				
		X509Certificate[] certs = (X509Certificate[])req.getAttribute(X509_ATTRIBUTE);
		
		if (certs!=null && certs.length>0) {
			
			// set authentication attribute
			X509Certificate cert = certs[0];
			final String principal = cert.getSubjectDN().getName();
			if (LOG.isInfoEnabled()) LOG.info("X509 client ertificate="+cert+" principal="+principal);
			req.setAttribute(Parameters.AUTHENTICATION_REQUEST_ATTRIBUTE, principal);
			
			// bypass authorization
			this.assertIsValid(req);
			
		}

	}

	public void init(FilterConfig filterConfig) throws ServletException { 
		super.init(filterConfig);
	}
	   
}
