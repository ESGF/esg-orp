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
package esg.orp.orp;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import esg.orp.Parameters;

/**
 * Filter that detects the request parameter "redirect" and stores it in the session
 * so that it can be later used to redirect authenticated users.
 * This filter must be invoked first in the chain.
 */
public class PreAuthenticationFilter implements Filter {
	
	private FilterConfig filterConfig;
			
	private final Log LOG = LogFactory.getLog(this.getClass());

	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		
		final HttpServletRequest req = (HttpServletRequest)request;
		final HttpServletResponse resp = (HttpServletResponse)response;
		final HttpSession session = req.getSession();
		final String redirect = req.getParameter(Parameters.OPENID_REDIRECT);
		final String openid = req.getParameter(Parameters.OPENID_IDENTIFIER);
		final String rememberme = req.getParameter(Parameters.OPENID_REMEMBERME);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Session id="+session.getId()+" Request URL="+req.getRequestURI());
			LOG.debug("Parameter name="+Parameters.OPENID_REDIRECT+" value="+redirect);
			LOG.debug("Parameter name="+Parameters.OPENID_IDENTIFIER+" value="+openid);
			LOG.debug("Parameter name="+Parameters.OPENID_REMEMBERME+" value="+rememberme);
		}
		
		// save redirect URL into session
		if (StringUtils.hasText(redirect)) {
			session.setAttribute(Parameters.OPENID_REDIRECT, redirect);
			if (LOG.isDebugEnabled()) LOG.debug("Stored into session: "+Parameters.OPENID_REDIRECT+"="+redirect);
		}
		
		// remember openid identity
		if (StringUtils.hasText(openid)) {
			final Cookie cookie = new Cookie(Parameters.OPENID_IDENTITY_COOKIE, openid);
			if (StringUtils.hasText(rememberme) && rememberme.equals("on")) {
				cookie.setMaxAge(Parameters.OPENID_IDENTITY_COOKIE_LIFETIME);
				if (LOG.isDebugEnabled()) LOG.debug("Set cookie name="+cookie.getName()+" value="+cookie.getValue());
			} else {
				cookie.setMaxAge(0);
				if (LOG.isDebugEnabled()) LOG.debug("Removing cookie name="+cookie.getName()+" value="+cookie.getValue());
			}
			resp.addCookie(cookie);
		}
		
		
   		// keep processing
   		chain.doFilter(request, response);

	}

	public void init(FilterConfig filterConfig) throws ServletException { 
		
		this.filterConfig = filterConfig; 
		
	}
	   
	public void destroy() { this.filterConfig = null; }

}
