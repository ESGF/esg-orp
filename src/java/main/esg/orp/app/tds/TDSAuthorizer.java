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
package esg.orp.app.tds;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import thredds.servlet.restrict.Authorizer;
import thredds.servlet.restrict.RoleSource;
import esg.orp.Parameters;
import esg.orp.app.AuthorizationEnforcer;

/**
 * Implementation of TDS {@link Authorizer} interface
 * that establishes access control based on a request attribute set by upstream filters.
 */
public class TDSAuthorizer implements Authorizer, AuthorizationEnforcer {

	private final Log LOG = LogFactory.getLog(this.getClass());

	/**
	 * This implementation returns the result of previous authorization decision as stored in the HTTP request attribute.
	 */
	public boolean authorize(HttpServletRequest request, HttpServletResponse response, String role) throws IOException, ServletException {

        final Boolean authzAtt = (Boolean)request.getAttribute(Parameters.AUTHORIZATION_REQUEST_ATTRIBUTE);
		if (LOG.isDebugEnabled()) LOG.debug("Authorization Request Attribute:"+authzAtt);
        
		// access denied
        if (authzAtt == null || authzAtt.booleanValue()==false) {
            if (!response.isCommitted()) {
            	response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
            }
			return false;
			
		// access allowed
		} else {
			return true;
		}

	}

	/**
	 * Dummy implementation of interface method (not needed).
	 */
	public void init(HttpServlet servlet) throws ServletException {}
	
	/**
	 * Dummy implementation of interface method (not needed).
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {}

	/**
	 * Dummy implementation of interface method (not needed).
	 */
	public void setRoleSource(RoleSource roleSource) {}
	
}
