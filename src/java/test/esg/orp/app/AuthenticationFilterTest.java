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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.xml.security.x509.BasicX509Credential;

import sun.security.x509.CertificateValidity;
import sun.security.x509.X509CertInfo;

import esg.security.authn.service.api.SAMLAuthentication;
import esg.security.authn.service.api.SAMLAuthenticationStatementFacade;
import esg.security.authn.service.impl.SAMLAuthenticationStatementFacadeImpl;
import esg.security.common.SAMLInvalidStatementException;
import esg.security.utils.ssl.EchoSSLServer;
import esg.security.utils.ssl.TrivialCertGenerator;

public class AuthenticationFilterTest {
    private AuthenticationFilter af;
    private String host;

    @Before
    public void setup() throws Exception {
        af = new AuthenticationFilter();
        host = "bmbf-ipcc-ar5.dkrz.de";

        // setup instance
        setField(af, "openidRelyingPartyUrl", "https://" + host + "/");
        setField(af, "policyService",
                new DummyPolicyServiceFilterCollaborator());
    }

    private static void setField(Object instance, String field, Object value)
            throws Exception {
        Field f = instance.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(instance, value);
    }

    private static Object getField(Object instance, String field)
            throws Exception {
        Field f = instance.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(instance);
    }

    private static Object invoke(Object instance, String method, Object... args)
            throws Exception {

        Class<?>[] c = new Class[args.length];

        for (int i = 0; i < c.length; i++) {
            if (args[i] instanceof Class) {
                c[i] = (Class<?>) args[i];
                args[i] = null;
            } else {
                c[i] = args[i].getClass();
            }
        }
        Method m = instance.getClass().getDeclaredMethod(method, c);
        m.setAccessible(true);
        return m.invoke(instance, args);
    }

    @Test
    public void testCertificateRetrieval() throws Exception {
        // empty at start
        assertNull(getField(af, "orpCert"));

        Certificate c = (Certificate) invoke(af, "retrieveORPCert");
        assertNotNull(c);
        assertNotNull(getField(af, "orpCert")); // got cached
        assertTrue(c == getField(af, "orpCert")); // same object

        // check we got the right cert!
        assertTrue(c instanceof X509Certificate);
        String strDN = ((X509Certificate) c).getSubjectDN().getName();
        Matcher m = Pattern.compile(".*CN=([^,]*),.*").matcher(strDN);
        assertTrue(m.find());
        assertEquals(host, m.group(1));

    }

    /**
     * When the certificate of the server is directly trusted the SSL connection
     * doesn't appear to verify it's validity thus accepting even expired ones.
     * We verify this though after retrieved and if it's expired or still not
     * valid we are going to reject it no matter what.
     * 
     * @throws Exception not expected
     */
    @Test
    public void testExpiredCertificateRetrieval() throws Exception {
        // yep, it was a long day :-)
        long day = 24 * 60 * 60 * 1000L;
        long year = 365 * day;

        KeyPair kp = TrivialCertGenerator.generateRSAKeyPair();
        
        // =========================
        // ***** check expired *****
        // =========================
        
        X509CertInfo info = TrivialCertGenerator.getDefaultInfo(kp,
                "L=DE, CN=localhost");
        Date notBefore = new Date(System.currentTimeMillis() - year);
        Date notAfter = new Date(System.currentTimeMillis() - day);
        info.set(X509CertInfo.VALIDITY, new CertificateValidity(notBefore,
                notAfter));
        Certificate cert = TrivialCertGenerator.createCertificate(info,
                kp.getPrivate());

        // set the server with the expired certificate
        EchoSSLServer server = new EchoSSLServer();
        server.setServerCertificate(kp.getPrivate(), cert);

        // set the proper HTTPsconnnection to trust the test server
        {

            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance("SunX509");
            tmf.init(TrivialCertGenerator.packKeyStore(null, null, null,
                    server.getCertificateChain())); // trust the certifica
            SSLContext sslc = SSLContext.getInstance("SSL");

            sslc.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslc
                    .getSocketFactory());
        }

        // start server
        server.start();

        // empty at start
        assertNull(getField(af, "orpCert"));

        // point to our server
        setField(af, "openidRelyingPartyUrl",
                "https://localhost:" + server.getPort());

        // get the certificate (should be null)
        Certificate c = (Certificate) invoke(af, "retrieveORPCert");
        assertNull(c);


        // =================================
        // ***** check still not valid *****
        // =================================
        
        notBefore = new Date(System.currentTimeMillis() + year);
        notAfter = new Date(notBefore.getTime() + year);
        info.set(X509CertInfo.VALIDITY, new CertificateValidity(notBefore,
                notAfter));
        cert = TrivialCertGenerator.createCertificate(info, kp.getPrivate());
        server.setServerCertificate(kp.getPrivate(), cert);

        // set the proper HTTPsconnnection to trust the test server
        {

            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance("SunX509");
            tmf.init(TrivialCertGenerator.packKeyStore(null, null, null,
                    server.getCertificateChain())); // trust the certifica
            SSLContext sslc = SSLContext.getInstance("SSL");

            sslc.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslc
                    .getSocketFactory());
        }

        // start server
        server.restart();

        // empty at start
        assertNull(getField(af, "orpCert"));

        // point to our server
        setField(af, "openidRelyingPartyUrl",
                "https://localhost:" + server.getPort());

        // get the certificate (should be null)
        c = (Certificate) invoke(af, "retrieveORPCert");
        assertNull(c);

        // stop the server
        server.stop();

    }

    @Test
    public void testAttemptValidation() throws Exception {
        String myOpenID = "https://somehost/someidP/MyId";

        // samlStatementFacade
        EchoSSLServer server = new EchoSSLServer();
        X509Certificate serverCert = (X509Certificate) server.getCertificate();

        // Create the cookie
        String rightCookie, wrongCookie;
        {
            // get the credential of the server and sign the cookie
            BasicX509Credential c = new BasicX509Credential();
            c.setEntityCertificate(serverCert);
            c.setPrivateKey(server.getKeyPair().getPrivate());

            SAMLAuthenticationStatementFacade samlStatementFacade = new SAMLAuthenticationStatementFacadeImpl();
            setField(samlStatementFacade, "signingCredential", c);
            rightCookie = samlStatementFacade
                    .buildSignedAuthenticationStatement(myOpenID);

            KeyPair kp = TrivialCertGenerator.generateRSAKeyPair();
            c.setPrivateKey(kp.getPrivate());
            c.setEntityCertificate(TrivialCertGenerator
                    .createSelfSignedCertificate(kp, "CN=Somewhere else, L=DE"));
            setField(samlStatementFacade, "signingCredential", c);
            wrongCookie = samlStatementFacade
                    .buildSignedAuthenticationStatement("https://somehost/someidP/MyId");
        }

        // set the proper HTTPsconnnection to trust the test server
        {

            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance("SunX509");
            tmf.init(TrivialCertGenerator.packKeyStore(null, null, null,
                    new Certificate[] { serverCert })); // trust the server
            SSLContext sslc = SSLContext.getInstance("SSL");

            sslc.init(null, tmf.getTrustManagers(), null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslc
                    .getSocketFactory());
        }

        // start server
        server.start();

        // empty at start
        assertNull(getField(af, "orpCert"));

        // point to our server
        setField(af, "openidRelyingPartyUrl",
                "https://localhost:" + server.getPort());

        // get the object we want to test
        SAMLAuthenticationStatementFacade fac = (SAMLAuthenticationStatementFacade) getField(
                af, "samlStatementFacade");
        try {
            // this is the call we want to mimic
            fac.getAuthentication((Certificate) invoke(af, "retrieveORPCert"),
                    wrongCookie);
            fail("Shouldn't have validated");
        } catch (SAMLInvalidStatementException e) {
            // ok, expected
        }
        // this is the call we want to mimic
        SAMLAuthentication authentication = fac.getAuthentication(
                (Certificate) invoke(af, "retrieveORPCert"), rightCookie);

        // everything fine? (it should be if we get this far.
        assertEquals(myOpenID, authentication.getIdentity());

        server.stop();
    }

}
