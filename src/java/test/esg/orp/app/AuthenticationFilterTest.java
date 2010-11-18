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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.xml.security.x509.BasicX509Credential;

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

        //get the object we want to test
        SAMLAuthenticationStatementFacade fac = (SAMLAuthenticationStatementFacade) getField(
                af, "samlStatementFacade");
        try {
            //this is the call we want to mimic
            fac.parseAuthenticationStatement(
                    (Certificate) invoke(af, "retrieveORPCert"), wrongCookie);
            fail("Shouldn't have validated");
        } catch (SAMLInvalidStatementException e) {
            // ok, expected
        }
        //this is the call we want to mimic
        String oid = fac.parseAuthenticationStatement(
                (Certificate) invoke(af, "retrieveORPCert"), rightCookie);

        // everything fine? (it should be if we get this far.
        assertEquals(myOpenID, oid);

        server.stop();
    }

}
