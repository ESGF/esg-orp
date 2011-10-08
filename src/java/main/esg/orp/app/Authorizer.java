package esg.orp.app;

/**
 * API for authorizing a user to execute an operation on a given resource.
 * 
 * @author Luca Cinquini
 *
 */
public interface Authorizer {
    
    /**
     * Method to execute an authorization decision.
     * @param url
     * @return
     */
    public boolean authorize(final String user, final String url, final String operation);


}
