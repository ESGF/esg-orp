package esg.orp.app;

import java.net.InetAddress;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.DecisionTypeEnumeration;
import org.springframework.util.StringUtils;

import esg.security.authz.service.api.SAMLAuthorization;
import esg.security.authz.service.api.SAMLAuthorizations;
import esg.security.authz.service.impl.SAMLAuthorizationServiceClientSoapImpl;
import esg.security.common.SOAPServiceClient;


/**
 * Implementation of {@link Authorizer} that acts as a client to one or more SAML authorization services.
 * 
 * @author Luca Cinquini
 * 
 */
public class SAMLAuthorizer implements Authorizer {
    
    /**
     * The URLs of the remote SAML Authorization Services to query.
     */
    private String[] endpoints = new String[]{};
    
    /**
     * Client used to encode/decode the authorization request into/from the SAML/SOAP document.
     */
    private final SAMLAuthorizationServiceClientSoapImpl encoder;
    
    /**
     * Client used for SOAP/HTTP communication to the SAML Authorization Service.
     */
    private final SOAPServiceClient transmitter;
    
    private final Log LOG = LogFactory.getLog(this.getClass());
        
    public SAMLAuthorizer() {
        
        encoder = new SAMLAuthorizationServiceClientSoapImpl("ESGF Authorization Filter");
        transmitter = SOAPServiceClient.getInstance();
        
    }

    /**
     * {@inheritDoc}
     */
    public boolean authorize(final String user, final String url, final String operation)  {
        
        for (final String endpoint : endpoints) {
            
            if (LOG.isDebugEnabled()) LOG.debug("Authorizing user="+user+" url="+url+" operation="+operation+" with service="+endpoint);
            
            try {
                
                String soapRequest = encoder.buildAuthorizationRequest(user, url, operation);
                if (LOG.isTraceEnabled()) LOG.trace(soapRequest);
                String soapResponse = transmitter.doSoap(endpoint, soapRequest);
                if (LOG.isTraceEnabled()) LOG.trace(soapResponse);
                
                // parse the authorization statements
                if (parseAuthorizationStatement(soapResponse, user, url, operation)) {
                    // return first positive authorization statement
                    return true;
                }
                
            } catch(Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        }
        
        // return negative authorization by default
        return false; 
    
    }
    
    /**
     * Testable method that parses the authorization statement and matches it versus the requested authorization parameters
     */
    boolean parseAuthorizationStatement(final String authzStatement, final String user, final String url, final String operation) throws Exception {
        
        boolean authorized = false;
                    
        // parse the authorization statements
        final SAMLAuthorizations authorizations = encoder.parseAuthorizationResponse(authzStatement);
        
        // ensure statement identity and the user match, even if both are null
        if (Objects.equals(authorizations.getIdentity(), user)) {
            if (LOG.isTraceEnabled() && user != null) LOG.trace("Matched user="+user);
            for (final SAMLAuthorization authz : authorizations.getAuthorizations()) {
                
                if (authz.getResource().equals(url)) {
                    if (LOG.isTraceEnabled()) LOG.trace("Matched URL="+url);
                    for (final String action : authz.getActions()) {
                        if (action.equalsIgnoreCase(operation)) {
                            if (LOG.isTraceEnabled()) LOG.trace("Matched operation="+operation);
                            if (authz.getDecision().equals(DecisionTypeEnumeration.PERMIT.toString())) authorized = true;
                            else return false;
                        }
                    }
                }
                
            }
        }
            
        return authorized;
    
    }

    /**
     * Setter method for URL endpoints of SAML authorization services.
     * @param endpoints
     */
    public void setEndpoints(String[] endpoints) {  
    	        
    	// assign endpoints from configuration file
    	this.endpoints = endpoints;
    			
        // try replacing "localhost" with FQDN
        try {
	        String hostName = InetAddress.getLocalHost().getHostName();
	        LOG.info("Detected hostName="+hostName);
	        if (StringUtils.hasText(hostName)) {
	        	for (int i = 0; i<this.endpoints.length; i++) {
	        		this.endpoints[i] = this.endpoints[i].replace("localhost", hostName);
	        	}
	        }
        } catch(Exception e) {
        	LOG.warn(e.getMessage());
        }
          
        for (String endpoint : this.endpoints) {
        	LOG.info("SAML Authorizer: using endpoint="+endpoint);
        }
        
    }
    

}
