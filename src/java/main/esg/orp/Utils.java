package esg.orp;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

public class Utils {
    
    /**
     * Utility method to compose the full HTTP request URL.
     * @param req
     * @return
     */
    public final static String getFullRequestUrl(final HttpServletRequest req) {
        return req.getRequestURL().toString() + (StringUtils.hasText(req.getQueryString()) ? "?"+req.getQueryString() : "");
    }
    
    /**
     * Method to transform the resource URL before it is processed by the AuthorizationService.
     * @param url
     * @return
     */
    public final static String transformUrl(String url) {
        
        // remove everything after '?'
        int c = url.indexOf('?');
        if (c > -1) {
            url = url.substring(0, c);
        }
        
        // temporary work around to enable authorization on opendap URLs
        url = url.replaceAll("dodsC", "fileServer").replaceAll("\\.ascii", "").replaceAll("\\.dods", "").replaceAll("\\.das", "").replaceAll("\\.dds", "");
        
        return url;
    }

}
