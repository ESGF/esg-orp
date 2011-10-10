package esg.orp.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class containing utility for issuing HTTP GET/POST requests.
 * @author Luca Cinquini
 *
 */
public class HttpUtils {
    
    private static final Log LOG = LogFactory.getLog(HttpUtils.class);
    
    /**
     * Method to execute a GET request.
     * 
     * @param uri : the URL to be requested without any query parameters
     * @param params: optional map of HTPP (name,value) query parameters
     * @return
     */
    public final static String get(final String uri, final Map<String, String> pars) throws Exception {
        
        // create an instance of HttpClient.
        HttpClient client = new HttpClient();
        
        // build full URL with query string
        String url = uri;
        String delimiter = "?";
        for (final String key : pars.keySet()) {
            url += delimiter + URLEncoder.encode(key,"UTF-8") + "=" + URLEncoder.encode(pars.get(key),"UTF-8");
            delimiter = "&";
        }

        // create a method instance.
        GetMethod method = new GetMethod(url);
        
        // provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        try {
            
          // execute the method.
          int statusCode = client.executeMethod(method);

          if (statusCode != HttpStatus.SC_OK) {
            throw new Exception("HTTP GET request failed: url="+url+" error=" + method.getStatusLine());
          }

          // read the response body.
          byte[] responseBody = method.getResponseBody();

          // use caution: ensure correct character encoding and is not binary data
          return(new String(responseBody));

        } catch (HttpException e) {
            LOG.warn(e.getMessage());
            throw new Exception("Fatal protocol violation: " + e.getMessage());
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            throw new Exception("Fatal transport error: " + e.getMessage());
        } finally {
          // release the connection.
          method.releaseConnection();
        }  
        
    }
    
    /**
     * Method to execute a POST request.
     * 
     * @param url : the URL to be requested without any query parameters
     * @param params: optional map of HTPP (name,value) query parameters
     * @return
     */
    public final static String post(final String url, final Map<String, String> pars) throws Exception {
        
        // create an instance of HttpClient.
        HttpClient client = new HttpClient();
        
        // create a method instance.
        PostMethod method = new PostMethod(url);
        
        // add request parameters
        for (final String key : pars.keySet()) {
           method.addParameter(key, pars.get(key));
        }
        
        // provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        try {
            
          // execute the method.
          int statusCode = client.executeMethod(method);

          if (statusCode != HttpStatus.SC_OK) {
            throw new Exception("HTTP POST request failed: url="+url+" error=" + method.getStatusLine());
          }

          // read the response body.
          byte[] responseBody = method.getResponseBody();

          // use caution: ensure correct character encoding and is not binary data
          return(new String(responseBody));

        } catch (HttpException e) {
            LOG.warn(e.getMessage());
            throw new Exception("Fatal protocol violation: " + e.getMessage());
        } catch (IOException e) {
            LOG.warn(e.getMessage());
            throw new Exception("Fatal transport error: " + e.getMessage());
        } finally {
          // release the connection.
          method.releaseConnection();
        }  
        
    }

}
