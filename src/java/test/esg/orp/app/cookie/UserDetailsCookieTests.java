package esg.orp.app.cookie;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.stream.Stream;

import javax.crypto.NoSuchPaddingException;

import org.junit.Before;
import org.junit.Test;
import org.apache.commons.codec.DecoderException;

public class UserDetailsCookieTests
{

    String secretKey;
    String userID;
    String[] tokens;
    String userData;
    String cookieValue;
    
    @Before
    public void setUp() throws IOException, URISyntaxException
    {
        ClassLoader loader = Test.class.getClassLoader();
        Path cookieInfoPath = Paths.get(loader.getResource(
                "esg/orp/app/cookie/sample_cookies/user-details-cookie-info").toURI());
        
        Stream<String> stream = Files.lines(cookieInfoPath);
        HashMap<String, String> valueMap = new HashMap<String, String>();
        stream.forEach(line -> {
            String[] parts = line.split(" ", 2);
            if (parts.length > 1)
            {
                String key = parts[0].replaceAll(":", "");
                String value = parts[1];
                
                valueMap.put(key, value);
            }
        });
        stream.close();
        
        secretKey = valueMap.get("encoded_secret_key");
        cookieValue = valueMap.get("cookie_value");
        
        userID = valueMap.get("userid");
        userData = valueMap.get("user_data");
        if (userData == null)
        {
            userData = "";
        }
        if (valueMap.containsKey("tokens"))
        {
            tokens = valueMap.get("tokens").split(",");
        }
    }

    @Test
    public void testParseCookie()
            throws NoSuchAlgorithmException, NoSuchPaddingException, DecoderException,
                InvalidKeyException, InvalidAlgorithmParameterException, DecryptionException
    {
        UserDetailsCookie cookie = UserDetailsCookie.parseCookie(this.cookieValue, this.secretKey);
        
        assertEquals(this.userID, cookie.getUserID());
        
        String[] tokens = cookie.getTokens();
        if (this.tokens != null && this.tokens.length > 0)
        {
            for (int i = 0; i > tokens.length; i++)
            {
                assertEquals(this.tokens[i], tokens[i]);
            }
        }
        
        assertEquals(this.userData, cookie.getUserData());
    }

}
