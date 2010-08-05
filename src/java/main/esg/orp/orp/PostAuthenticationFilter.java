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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.Assert;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.util.WebUtils;

import esg.orp.Parameters;
import esg.saml.auth.service.api.SAMLAuthenticationStatementFacade;
import esg.saml.auth.service.impl.SAMLAuthenticationStatementFacadeImpl;

/**
 * Filter that detects a positive user authentication and sets a domain-wide session cookie.
 * This filter must be invoked last in the chain.
 */
public class PostAuthenticationFilter implements Filter, InitializingBean {
		
	private String keystoreFile;
	private String keystorePassword;
	private String keystoreAlias;

	private final SAMLAuthenticationStatementFacade samlStatementFacade = new SAMLAuthenticationStatementFacadeImpl();
		
	private final Log LOG = LogFactory.getLog(this.getClass());

	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		
		final HttpServletRequest req = (HttpServletRequest)request;
		final HttpServletResponse resp = (HttpServletResponse)response;	
		final HttpSession session = req.getSession();
		final String url = req.getRequestURL().toString();
		final URL reqURL = new URL(url);
				
		final SecurityContext secCtx = SecurityContextHolder.getContext();
		final Authentication auth = secCtx.getAuthentication();
		if (LOG.isDebugEnabled()) LOG.debug("URL="+url+" Security context authentication="+auth+" Session id="+session.getId());

		if (auth!=null && (auth instanceof OpenIDAuthenticationToken || auth instanceof PreAuthenticatedAuthenticationToken)) {
			
			final String openid = auth.getName();
			if (LOG.isDebugEnabled())LOG.debug("User is authenticated, openid="+openid);
			
			// retrieve OpenID Exchange Attributes
			if (auth instanceof OpenIDAuthenticationToken) {
				final OpenIDAuthenticationToken token = (OpenIDAuthenticationToken)auth;
				final List<OpenIDAttribute> attributes = token.getAttributes();
				if (LOG.isDebugEnabled()) {
					for (OpenIDAttribute attribute : attributes) {
						LOG.info("Retrieved OpenID attribute="+attribute.getName()+" type="+attribute.getType()+" value="+attribute.getValues().get(0));
					}
				}
			}
			
			if (WebUtils.getCookie(req, Parameters.OPENID_SAML_COOKIE)==null) {
				
				try {
							
					// build signed authentication statement		
					final String authstmt = samlStatementFacade.buildSignedAuthenticationStatement(openid);
					
					// set cookie (value must be encoded)
					final Cookie cookie = new Cookie(Parameters.OPENID_SAML_COOKIE, URLEncoder.encode(authstmt,"UTF-8"));
					//cookie.setSecure(true);  // cookie will be transmitted only over HTTPS
					cookie.setMaxAge(-1);    // cookie will be deleted when session is terminated
					cookie.setDomain(reqURL.getHost()); // cookie sent to all applications on this host
					cookie.setPath("/");     // cookie will be sent to all pages in web application
					resp.addCookie(cookie);
					if (LOG.isDebugEnabled()) {
						LOG.debug("Setting authentication cookie:");
						this.printCookie(cookie);
					}
				
				} catch(Exception e) {
					throw new ServletException(e);
				}
				
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found authentication cookie:");
					this.printCookie(WebUtils.getCookie(req, Parameters.OPENID_SAML_COOKIE));
				}
			}
			
			// redirect
			if (session.getAttribute(Parameters.OPENID_REDIRECT)!=null) {
				final String redirect = (String)session.getAttribute(Parameters.OPENID_REDIRECT);
				if (LOG.isDebugEnabled()) LOG.debug("Redirecting to: "+redirect);
				resp.sendRedirect( redirect );
			} else {
				chain.doFilter(request, response);
			} 
						
		} else {
		
	   		// set HTTP STATUS CODE = 401 while returning login page
			resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			
			// keep processing
	  		chain.doFilter(request, response);
				   		
		}

	}
	
	/**
	 * Method to check that the keystore parameters have been properly initialized by the user.
	 */
	public void afterPropertiesSet() throws Exception {
		
		Assert.hasText(keystoreFile, "Parameter keystoreFile not initialized");
		Assert.hasText(keystoreAlias, "Parameter keystoreAlias not initialized");
		Assert.hasText(keystorePassword, "Parameter keystorePassword not initialized");
		
	}
	
	/**
	 * Initialization method allows for suer customization of keystore parameters.
	 * @throws ServletException
	 */
	public void init() throws Exception { 
						
		final File keystore = new File(keystoreFile);
		samlStatementFacade.setSigningCredential(keystore, keystorePassword, keystoreAlias);
		if (LOG.isDebugEnabled()) LOG.debug("Will sign statements with keystore="+keystoreFile+" alias="+keystoreAlias);
		
		// embedded test to catch up signature validation problems
		// Note: this test depend on the user settings for the filter keystore
		if (LOG.isDebugEnabled()) {
			
			try {
				final String tmpDir = System.getProperty("java.io.tmpdir");
				final File file = new File(tmpDir,Parameters.TEST_FILE);
				String xml = samlStatementFacade.buildSignedAuthenticationStatement(Parameters.TEST_OPENID);
				FileUtils.writeStringToFile(file, URLEncoder.encode(xml,"UTF-8")); // simulate cookie encoding
				LOG.debug("Written test SAML assertion to file:"+file.getAbsolutePath());
				LOG.debug(xml);
			} catch(Exception e) {
				LOG.warn(e);
			}
			
		}
	
	}

	/**
	 * Note: this method is never called by the Spring {@link DelegatingFilterProxy}.
	 */
	public void init(final FilterConfig filterConfig) throws ServletException {}
	  
	/**
	 * Note: this method is never called by the Spring {@link DelegatingFilterProxy}.
	 */
	public void destroy() { }
	
	private void printCookie(final Cookie cookie) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Cookie name="+cookie.getName()
					 +" domain="+cookie.getDomain()+" path="+cookie.getPath()
					 +" expires="+cookie.getMaxAge()+" secure="+cookie.getSecure()+" value="+cookie.getValue());
		}

	}

	public void setKeystoreFile(String keystoreFile) {
		this.keystoreFile = keystoreFile;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public void setKeystoreAlias(String keystoreAlias) {
		this.keystoreAlias = keystoreAlias;
	}
	

}
