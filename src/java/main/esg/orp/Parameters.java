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
package esg.orp;

/**
 * Interface containing common application constants.
 */
public interface Parameters {
	
	// HTTP request parameters
	final static String OPENID_REMEMBERME = "rememberOpenid";
	final static String OPENID_IDENTIFIER = "openid_identifier";
	final static String OPENID_IDENTITY_COOKIE = "esg.openid.identity.cookie";
	final static int OPENID_IDENTITY_COOKIE_LIFETIME = 86400*365*10; // ten years
	
	// OpenID RP target URL
	final static String OPENID_URL = "/j_spring_openid_security_check.htm";
	
	// OpenID Relying Party parameters
	final static String KEYSTORE_PATH = "kestorePath";
	final static String KEYSTORE_PASSWORD = "kestorePassword";
	final static String KEYSTORE_ALIAS = "kestoreAlias";
	
	// Secured application parameters
	final static String POLICY_SERVICE = "policyServiceClass";
	final static String AUTHORIZATION_SERVICE = "authorizationServiceClass";
	final static String TRUSTORE_FILE = "trustoreFile";
	final static String TRUSTORE_PASSWORD = "trustorePassword";
	final static String OPENID_RP_URL = "openidRelyingPartyUrl";
	final static String AUTHORIZATION_SERVICE_URL = "authorizationServiceUrl";
    final static String AUTHORIZATION_REQUEST_ATTRIBUTE = "eske.model.security.AuthorizationToken"; // legacy value compatible with old TDS filter
    final static String AUTHENTICATION_REQUEST_ATTRIBUTE = "esg.openid";
    final static String AUTHENTICATION_ONLY_FLAG = "authenticationOnlyFlag";
    final static String AUTHORIZED_IP = "authorizedIp";
	
	// shared parameters
	final static String OPENID_REDIRECT = "redirect";
	final static String OPENID_IDENTITY = "openid";
	final static String OPENID_SAML_COOKIE = "esg.openid.saml.cookie";
	
	// test file
	final static String TEST_FILE = "esg-saml-test-file.xml";
	final static String TEST_OPENID = "http://JoeTester.myopenid.com/";

}
