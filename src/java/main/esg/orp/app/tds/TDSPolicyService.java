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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import thredds.servlet.DatasetHandler;
import esg.orp.app.PolicyServiceFilterCollaborator;

/**
 * Implementation of {@link PolicyServiceFilterCollaborator} 
 * to be used with a Thredds Data Server (TDS) application server.
 */
public class TDSPolicyService implements PolicyServiceFilterCollaborator {
	public static final String PROP_TRIM_REGEX = "trimURIRegEx";
	
	private final Log LOG = LogFactory.getLog(this.getClass());
    private Pattern pattern;

	public void destroy() {}

	public void init(FilterConfig filterConfig) {
	    
	    final String suffix= filterConfig.getInitParameter(PROP_TRIM_REGEX);
	    LOG.info("Filter configuration: "+PROP_TRIM_REGEX+"="+suffix);
        if (suffix != null) {
            final String regEx = "^\\/(.*)(" + suffix.replace(',', '|') + ")$";
            if (LOG.isInfoEnabled()) LOG.info("Setting trim regEx to " + regEx);
            pattern = Pattern.compile(regEx);
        }
	    
	}

	/**
	 * Implementation of {@link PolicyServiceFilterCollaborator} method that checks the resource control established by the TDS.
	 */
	public boolean isSecure(final HttpServletRequest request) {
		
		boolean isSecure = false;
		String uri = request.getPathInfo();
        if (uri != null) {
            
            // check access control before URI change
            if (LOG.isDebugEnabled()) LOG.debug("URI=" + uri + " resource control=" + DatasetHandler.findResourceControl(uri) );
            
            if (pattern != null) {
                Matcher m = pattern.matcher(uri);
                if (m.find()) {
                    //trim as required
                    uri = m.group(1);
                    LOG.debug("Uri changed.");
                }
            }
            // check access control after URI change
            final String rc = DatasetHandler.findResourceControl(uri);
            if (StringUtils.hasText(rc)) isSecure = true;
            if (LOG.isDebugEnabled()) LOG.debug("URI=" + uri + " resource control=" + rc + " is secure=" + isSecure);
            
        }

		return isSecure;
		
	}

}
