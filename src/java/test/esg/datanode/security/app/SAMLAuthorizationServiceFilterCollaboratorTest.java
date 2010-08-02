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
package esg.datanode.security.app;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.xml.ConfigurationException;
import org.springframework.core.io.ClassPathResource;

import esg.saml.common.SAMLBuilder;
import esg.saml.common.SAMLUnknownPrincipalException;


public class SAMLAuthorizationServiceFilterCollaboratorTest {
	
	private final static String PERIMIT_STATEMENT = "esg/datanode/security/app/authorizationStatementPermit.xml";
	private final static String DENY_STATEMENT = "esg/datanode/security/app/authorizationStatementDeny.xml";
	
	private final static String OPENID = "https://esg.ucar.edu/myopenid/rootAdmin";
	private final static String RESOURCE = "http://tds.prototype.ucar.edu/thredds/fileServer/datazone/narccap/data/CRCM/cgcm3-current/table1/sic_CRCM_1968010106.nc";
	private final static String OPERATION = "read";
	
	private SAMLAuthorizationServiceFilterCollaborator service; 
	
	@Before
	public void beforeSetup() throws ConfigurationException, SAMLUnknownPrincipalException {
						
		if (SAMLBuilder.isInitailized()) {
			 service = new SAMLAuthorizationServiceFilterCollaborator();
		}
				
	}

	@Test
	public void testParseAuthorizationStatementPermit() throws Exception {
		
		if (SAMLBuilder.isInitailized()) {
			final File file = new ClassPathResource(PERIMIT_STATEMENT).getFile();
			final String authzStatement = FileUtils.readFileToString(file);
			final boolean authorized = service.parseAuthorizationStatement(authzStatement, OPENID, RESOURCE, OPERATION);
			Assert.assertTrue("Wrong authorization resulted from parsing", authorized);
		}
		
	}
	
	@Test
	public void testParseAuthorizationStatementDeny() throws Exception {
		
		if (SAMLBuilder.isInitailized()) {
			final File file = new ClassPathResource(DENY_STATEMENT).getFile();
			final String authzStatement = FileUtils.readFileToString(file);
			final boolean authorized = service.parseAuthorizationStatement(authzStatement, OPENID, RESOURCE, OPERATION);
			Assert.assertFalse("Wrong authorization resulted from parsing", authorized);
		}
		
	}
	
	@Test
	public void testParseMatchFieldsInAuthorizationStatement() throws Exception {
		
		if (SAMLBuilder.isInitailized()) {
			final File file = new ClassPathResource(PERIMIT_STATEMENT).getFile();
			final String authzStatement = FileUtils.readFileToString(file);
			boolean authorized = service.parseAuthorizationStatement(authzStatement, OPENID+"x", RESOURCE, OPERATION);
			Assert.assertFalse("Wrong authorization resulted from parsing with wrong openid", authorized);
			authorized = service.parseAuthorizationStatement(authzStatement, OPENID, RESOURCE+"x", OPERATION);
			Assert.assertFalse("Wrong authorization resulted from parsing with wrong resource", authorized);
			authorized = service.parseAuthorizationStatement(authzStatement, OPENID, RESOURCE, OPERATION+"x");
			Assert.assertFalse("Wrong authorization resulted from parsing with wrong operation", authorized);
		}
		
	}
	
}
