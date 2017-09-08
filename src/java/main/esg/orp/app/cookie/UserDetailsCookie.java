package esg.orp.app.cookie;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.regex.Pattern;

import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class encapsulating a cookie containing user details
 * 
 * @author William Tucker
 */
public class UserDetailsCookie extends SecureCookie
{
    public static String BODY_SEPARATOR = "!";
    public static String TIMESTAMP_FORMAT = "%08d";
    
    private Timestamp timestamp;
    private String userID;
    private String[] tokens;
    private String userData;

    private static final Log LOG = LogFactory.getLog(UserDetailsCookie.class);
    
    /**
     * Constructor taking a cookie name and user information
     * 
     * @param name      cookie name
     * @param key       secret key for decryption
     * @param timestamp cookie creation timestamp
     * @param userID    cookie user ID
     * @param tokens    cookie tokens
     * @param userData  cookie user data
     */
    public UserDetailsCookie(String key, Timestamp timestamp, String userID, String[] tokens, String userData)
    {
        super(key);
        
        this.timestamp = timestamp;
        this.userID = userID;
        this.tokens = tokens;
        this.userData = userData;
    }
    
    /**
     * Parses an encrypted user details cookie value
     * 
     * @param name          cookie name
     * @param encodedValue  encoded value
     * @param key           secret key for decryption
     * @return  parsed value
     * @throws DecryptionException 
     * @throws DecoderException 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     */
    public static UserDetailsCookie parseCookie(String encodedValue, String key)
            throws NoSuchAlgorithmException, NoSuchPaddingException, DecoderException,
                    DecryptionException
    {
        SecureCookie cookie = SecureCookie.parseCookie(encodedValue, key);
        String cookieContent = cookie.getValue();
        
        Timestamp timestamp = null;
        String userID = null;
        String[] tokens = null;
        String userData = null;
        if (cookieContent != null)
        {
            try
            {
                timestamp = new Timestamp(Long.parseLong(cookieContent.substring(0, 8), 16));
            }
            catch (NumberFormatException e)
            {
                e.printStackTrace();
            }
            
            String cookieBody = cookieContent.substring(8);
            if (!cookieBody.contains(BODY_SEPARATOR))
            {
                if (LOG.isDebugEnabled())
                    LOG.debug("Bad cookie format");
            }
            
            String[] parts = cookieBody.split(Pattern.quote(BODY_SEPARATOR), 2);
            try
            {
                userID = URLDecoder.decode(parts[0], "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                userID = parts[0];
            }
            
            if (parts.length > 1)
            {
                parts = parts[1].split(Pattern.quote(BODY_SEPARATOR));
                if (parts.length == 2)
                {
                    // tokens are comma separated
                    tokens = parts[0].split(Pattern.quote(","));
                    userData = parts[1];
                }
                else if (parts.length == 1)
                {
                    userData = parts[0];
                }
            }
        }
        
        UserDetailsCookie details = new UserDetailsCookie(key, timestamp, userID, tokens, userData);
        
        return details;
    }
    
    /**
     * @see SecureCookie#getValue()
     */
    @Override
    public String getValue()
    {
        return super.getValue();
    }
    
    /**
     * Get the timestamp of the cookie
     * 
     * @return  cookie timestamp
     */
    public Timestamp getTimestamp()
    {
        return timestamp;
    }
    
    /**
     * Get the cookie user ID
     * 
     * @return  cookie user ID
     */
    public String getUserID()
    {
        return userID;
    }
    
    /**
     * Get a list of tokens from the cookie
     * 
     * @return  cookie tokens
     */
    public String[] getTokens()
    {
        return tokens;
    }
    
    /**
     * Get the cookie user data
     * 
     * @return  cookie user data
     */
    public String getUserData()
    {
        return userData;
    }
}
