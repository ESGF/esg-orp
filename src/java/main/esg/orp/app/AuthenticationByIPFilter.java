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

import java.io.IOException;
import java.net.*;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import esg.orp.Parameters;
import esg.security.registry.service.api.RegistryService;
import esg.security.registry.service.impl.RegistryServiceLocalXmlImpl;

/**
 * Filter that establishes authentication by checking if the request originates from a trusted IP address.
 * 
 * The pluggable implementation of {@link PolicyServiceFilterCollaborator} allows to bypass the access control workflow
 * for resources that are designated for unrestricted access - use {@link StrictPolicyService} to always enforce access control
 * for all resources.
 * 
 */
public class AuthenticationByIPFilter extends AccessControlFilterTemplate {
	
	
	String authorizedIP = null;
	long[][] authorizedIpRanges = null;
	RegistryService registryService = null;
	
	private final Log LOG = LogFactory.getLog(this.getClass());
			
	/**
	 * {@inheritDoc}
	 */
	public void attemptValidation(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain chain) 
				throws IOException, ServletException {
						
		// IP address versus AUTHORIZED_IP, AUTHORIZED_IP_RANGES, IP_WHITELIST
		final String addr = req.getRemoteAddr();

		// prepare check IP address versus AUTHORIZED_IP_RANGES
		boolean isInRange = false;
		long address = ipToLong(InetAddress.getByName(addr));

		if (authorizedIpRanges != null) {
			for (int i=0; i<authorizedIpRanges.length; i++) {
			 //check IP address between ipLo and ipHi
				if (address >= authorizedIpRanges[i][0] && address <= authorizedIpRanges[i][1]) {
					isInRange = true;
				}
			}	
		}

		// do check 
		if (LOG.isDebugEnabled()) LOG.debug("Checking authorization for remote host IP:"+addr);
		if (   (authorizedIP!=null && authorizedIP.equals(addr))
		    || (registryService!=null && registryService.getLasServers().contains(addr))
		    || (isInRange == true) ) {
                if (LOG.isDebugEnabled()) LOG.debug("Remote host IP: "+addr+" found in white list, request is authorized");
                this.assertIsValid(req);
		}

	}

	public static long ipToLong(InetAddress ip) {
        	byte[] octets = ip.getAddress();
        	long result = 0;
        	for (byte octet : octets) {
            		result <<= 8;
            		result |= octet & 0xff;
        	}
        	return result;
    	}

	public void init(FilterConfig filterConfig) throws ServletException { 
		
		super.init(filterConfig);
		
		/**
		 *  <init-param>
         *    <param-name>authorizedIpRanges</param-name>
         *    <param-value>137.78.210.102-137.78.210.105,137.78.210.106-137.78.210.110</param-value>
         *  </init-param>
		 */
		String authorizedIpRanges = filterConfig.getInitParameter(Parameters.AUTHORIZED_IP_RANGES);
		if (StringUtils.hasText(authorizedIpRanges)) {
			String[] ranges = authorizedIpRanges.split(",");
		
			// Convert ranges from string to long
			long[][] longRanges = new long[ranges.length][2];
			for (int i=0; i<ranges.length; i++) {

				try {
					// Convert ipLo
					longRanges[i][0] = ipToLong(InetAddress.getByName(ranges[i].split("-")[0]));
					// Convert ipHi
					longRanges[i][1] = ipToLong(InetAddress.getByName(ranges[i].split("-")[1]));
				}
				catch (Exception e) {
					LOG.warn(e.getMessage());
                        		e.printStackTrace();
                        		throw new ServletException(e.getMessage());
				}
			}

			if (longRanges != null) {
				this.authorizedIpRanges = longRanges;
				for (int j=0; j<ranges.length; j++) {
					LOG.info("Authorizing IP Range: "+ranges[j]);
				}
			}
		}

	        /**
		 *  <init-param>
 	  *    <param-name>authorizedIp</param-name>
          *    <param-value>137.78.210.102</param-value>
          * </init-param>
 		  */
                String authorizedIp = filterConfig.getInitParameter(Parameters.AUTHORIZED_IP);
                if (StringUtils.hasText(authorizedIp)) {
                    LOG.info("Authorizing IP: "+authorizedIp);
                    this.authorizedIP = authorizedIp;
                }

		
	    /**
         *  <init-param>
         *    <param-name>ip_whitelist</param-name>
         *    <param-value>/esg/content/las/conf/server/las_servers.xml</param-value>
         *  </init-param>
         */
		String ipWhitelist = filterConfig.getInitParameter(Parameters.IP_WHITELIST);
		if (StringUtils.hasText(ipWhitelist)) {   
		    try {
		        LOG.info("Using IP white-list files: "+ipWhitelist);
		        registryService = new RegistryServiceLocalXmlImpl(ipWhitelist);
		    } catch(Exception e) {
		        LOG.warn(e.getMessage());
		        e.printStackTrace();
		        throw new ServletException(e.getMessage());
		    }
		}
		
	}
	   
}
