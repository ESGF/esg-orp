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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import esg.orp.Parameters;

/**
 * Abstract template for access control filters deployed in front of a restricted web application.
 * Each implementing filter can attempt to validate the HTTP request in turn (in the order they are deployed in web.xml), 
 * and will flag a positive result with a request-scope attribute. 
 * The functionality of the downstream filters will not be executed if the request has been already validated by an upstream filter.
 */
public abstract class AccessControlFilterTemplate implements Filter {

	private FilterConfig filterConfig;
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		
		final HttpServletRequest req = (HttpServletRequest)request;
		final HttpServletResponse resp = (HttpServletResponse)response;
		final HttpSession session = req.getSession();
		final Boolean authzAtt = (Boolean)request.getAttribute(Parameters.AUTHORIZATION_REQUEST_ATTRIBUTE);
		if (LOG.isDebugEnabled()) LOG.debug("Establishing access control for URL="+this.getUrl(req)+" session id="+session.getId()+" current authorization attribute="+authzAtt);

		// proceed only if not authorized already by upstream filters
		if (authzAtt==null || authzAtt.booleanValue()==false) {
			this.attemptValidation(req, resp, chain);
		}
		
   		// keep processing
   		chain.doFilter(request, response);   

	}
	
	/**
	 * Template method to be implemented by subclasses. 
	 * This method will be invoked only if the request has not yet been validate by the filters deployed upstream.
	 * @param request
	 * @param response
	 */
	abstract void attemptValidation(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) 
	              throws IOException, ServletException;
	
	/**
	 * Method to assert that the HTTP request has been positively validated.
	 * @param request
	 */
	protected void assertIsValid(final HttpServletRequest request) {
		request.setAttribute(Parameters.AUTHORIZATION_REQUEST_ATTRIBUTE, true);
	}

	public void init(final FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig; 
	}
	
	public void destroy() { this.filterConfig = null; }
	
	/**
	 * Utility method to retrieve and check a mandatory filter configuration parameter.
	 * @param name
	 * @return
	 */
	protected String getMandatoryFilterParameter(final String name) {
		final String value = filterConfig.getInitParameter(name);
		Assert.isTrue(StringUtils.hasText(value), "Missing filter configuration parameter: "+name);
		return value;
	}
	
	/**
	 * Utility method to compose the full HTTP request URL.
	 * @param req
	 * @return
	 */
	protected String getUrl(final HttpServletRequest req) {
		return req.getRequestURL().toString() + (StringUtils.hasText(req.getQueryString()) ? "?"+req.getQueryString() : "");
	}

}
