package esg.orp.app.cookie;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * Class for parsing encrypted text
 * 
 * @author William Tucker
 */
public class EncryptionHandler
{
    public static char DEFAULT_PADDING_CHAR = ' ';
    public static String DEFAULT_SECRET_KEY_SPEC = "AES";
    public static String DEFAULT_CIPHER = "AES/CBC/NoPadding";
    
    private SecretKey key;
    private Cipher cipher;
    private char paddingChar;
    
    /**
     * Constructor taking a secret key and an IV
     * 
     * @param   keyBytes    secret key
     * @param   ivBytes     encryption iv
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     */
    public EncryptionHandler(byte[] keyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        this.key = new SecretKeySpec(keyBytes, 0, keyBytes.length,
                DEFAULT_SECRET_KEY_SPEC);
        this.paddingChar = DEFAULT_PADDING_CHAR;
        
        this.cipher = Cipher.getInstance(DEFAULT_CIPHER);
    }
    
    /**
     * Decrypt some text
     * 
     * @param   cipherTextBytes byte array of the text
     * @return  decrypted text
     * @throws DecryptionException 
     */
    public String decrypt(byte[] cipherTextBytes, byte[] ivBytes) throws DecryptionException
    {
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        
        String textValue = null;
        try
        {
            this.cipher.init(Cipher.DECRYPT_MODE, this.key, iv);
            
            byte[] plainTextBytes = this.cipher.doFinal(cipherTextBytes);
            textValue = new String(plainTextBytes, "UTF-8");
            
            String regex = String.format("%s+$", Pattern.quote(String.valueOf(this.paddingChar)));
            textValue = textValue.replaceAll(regex, "");
        }
        catch (BadPaddingException | IllegalBlockSizeException | UnsupportedEncodingException |
                InvalidKeyException | InvalidAlgorithmParameterException e)
        {
            throw new DecryptionException("Problem decrypting bytes", e);
        }
        
        return textValue;
    }
}
