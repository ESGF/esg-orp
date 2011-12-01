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
package esg.orp.orp;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.consumer.ConsumerException;
import org.springframework.security.openid.OpenID4JavaConsumer;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDConsumerException;

import esg.security.registry.service.api.RegistryService;
import esg.security.registry.service.impl.RegistryServiceLocalXmlImpl;

/**
 * Subclass of {@link OpenID4JavaConsumer} that allows optional white-listing of OpenID providers.
 * If no white list is supplied, all Identity Providers will be trusted.
 * @author luca.cinquini
 */
public class MyOpenID4JavaConsumer extends OpenID4JavaConsumer {
	
	private final Log LOG = LogFactory.getLog(this.getClass());
	
	/**
	 * Location on local system of IdP white list file.
	 * If provided, it populates the list from the file content.
	 */
	private String idpWhiteListFile = null;
	//private final static Namespace NS = Namespace.getNamespace("http://www.esgf.org/whitelist");
	//private long idpWhiteListFileLastModTime = 0L; // Unix Epoch
	
	/**
     * Service holding the white list of trusted Identity Providers.
     */
    private RegistryService registryService;
	
	/**
	 * List of trusted IdP providers.
	 */
	//private List<String> idpWhiteList = new ArrayList<String>();

	public MyOpenID4JavaConsumer() throws ConsumerException {
		super();
	}
	
	public MyOpenID4JavaConsumer(java.util.List<OpenIDAttribute> attributes) throws ConsumerException {
		super(attributes);
	}

	@Override
	public String beginConsumption(final HttpServletRequest request, final String response, final String returnToUrl, final String realm) throws OpenIDConsumerException {
		
		// invoke superclass method to determine IdP URL
		String idpurl =  super.beginConsumption(request, response, returnToUrl, realm);
		if (LOG.isDebugEnabled()) LOG.debug("Resolved IdP URL="+idpurl);

		// optional whitelisting
		if (registryService!=null) {
			
			final String _idpUrl = idpurl.substring(0,idpurl.indexOf("?"));
			for (final URL url : registryService.getIdentityProviders()) {
			    // FIXME
			    System.out.println("examining idp url ="+url.toString());
				if (url.toString().equals(_idpUrl)) return idpurl;
			}
			LOG.warn("URL: "+_idpUrl+" not found in white list of trusted Identity Providers");
			throw new OpenIDConsumerException("URL: "+_idpUrl+" not found in white list of trusted Identity Providers");
			
		}
		
		return idpurl;
		
	}

	/** Method to read the list of trusted Identity Providers from a URL
	 * 
	 * @param idpWhiteListURL
	 */
    public void setIdpWhiteListFile(final String idpWhiteListFile) {
        this.idpWhiteListFile = idpWhiteListFile;
    }
    
    /**
     * Method that initializes the list of of trusted Identity Providers, if provided.
     */
    public void init() throws Exception {
        
        if (idpWhiteListFile!=null) {
            registryService = new RegistryServiceLocalXmlImpl(idpWhiteListFile);
        }
                
    }
	
}
