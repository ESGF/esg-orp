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
     * 
     * @param user
     * @param resource
     * @param operation
     * 
     * @return
     */
    public boolean authorize(final String user, final String resource, final String operation);


}
