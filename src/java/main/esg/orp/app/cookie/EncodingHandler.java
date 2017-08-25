package esg.orp.app.cookie;

import org.apache.commons.codec.binary.Base64;

import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class for parsing encoded cookie values.
 * 
 * @author William Tucker
 */
public class EncodingHandler
{
    public static String DEFAULT_DELIMITER = "-";
    
    private String delimiter;
    
    private byte[] keyBytes;
    private EncryptionHandler encryptionHandler;
    
    private static final Log LOG = LogFactory.getLog(EncodingHandler.class);
    
    /**
     * Constructor specifying the secret key used for encryption
     * 
     * @param   key   secure secret key
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     */
    public EncodingHandler(String key) throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        this.delimiter = DEFAULT_DELIMITER;
        
        this.keyBytes = Base64.decodeBase64(key);
        this.encryptionHandler = new EncryptionHandler(keyBytes);
    }
    
    /**
     * Decodes an encoded cookie value
     * 
     * @param   message   the text to decode
     * @return  the decoded message
     * @throws DecoderException 
     * @throws DecryptionException 
     */
    public String decode(String message) throws DecoderException, DecryptionException
    {
        String encodedCipherText;
        String encodedIV;
        String encodedDigest;
        try
        {
            String[] content = message.split(this.delimiter);
            
            encodedCipherText = content[0];
            encodedIV = content[1];
            encodedDigest = content[2];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new DecoderException("Invalid cookie format", e);
        }
        
        byte[] cipherTextBytes = Hex.decodeHex(encodedCipherText.toCharArray());
        byte[] ivBytes = Hex.decodeHex(encodedIV.toCharArray());
        byte[] digestBytes = Hex.decodeHex(encodedDigest.toCharArray());
        
        String cookieContent = null;
        if (this.keyBytes != null && cipherTextBytes != null && ivBytes != null && digestBytes != null)
        {
            if (LOG.isDebugEnabled())
                LOG.debug("Verifying signature");
            if (VerifySignature(encodedCipherText.getBytes(), digestBytes, this.keyBytes))
            {
                if (LOG.isDebugEnabled())
                    LOG.debug("Decrypting bytes");
                
                cookieContent = this.encryptionHandler.decrypt(cipherTextBytes, ivBytes);
            }
            else
            {
                if (LOG.isDebugEnabled())
                    LOG.debug("Digests do not match");
            }
        }
        
        return cookieContent;
    }
    
    /**
     * Verifies the signature of encrypted text with a digest
     * 
     * @param   cipherText  text to verify as a byte array
     * @param   digest      digest to compare as a byte array
     * @param   key         secret key as a byte array
     * @return  whether the signature matched or not
     */
    public static boolean VerifySignature(byte[] cipherText, byte[] digest, byte[] key)
    {
        String originalDigest = new String(digest);
        String calculatedDigest = Sign(key, cipherText);
        
        return calculatedDigest.equals(originalDigest);
    }
    
    /**
     * Calculate a digest for a message from a key
     * 
     * @param   key     the secret key
     * @param   message text to sign
     * @return  the resulting digest
     */
    public static String Sign(byte[] key, byte[] message)
    {
        byte[] digestBytes = HmacUtils.hmacSha256(key, message);
        
        String digest = new String(digestBytes);
        
        return digest;
    }
}
