package esg.orp.app.cookie;

/**
 * Exception thrown when a problem occurs during decryption
 * 
 * @author William Tucker
 */
public class DecryptionException extends Exception
{

    private static final long serialVersionUID = 1L;

    public DecryptionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DecryptionException(String message)
    {
        super(message);
    }

}
