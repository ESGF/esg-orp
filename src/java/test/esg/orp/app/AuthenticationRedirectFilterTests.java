package esg.orp.app;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationRedirectFilterTests
{

    private static final String AUTHENTICATE_URL_PARAM = "authenticateUrl";
    private static final String AUTHENTICATE_URL =
            "https://localhost/account/signin/";
    
    private static final String RETURN_QUERY_NAME_PARAM = "returnQueryName";
    private static final String RETURN_QUERY_NAME = "r";

    private static final String REQUEST_ATTRIBUTE_PARAM = "requestAttribute";
    private static final String REQUEST_ATTRIBUTE = "id";
    
    private static final String SECRET_KEY_PARAM = "secretKey";
    private static final String COOKIE_NAME_PARAM = "sessionCookieName";
    private static final String COOKIE_NAME = "session-cookie";
    
    private static String secretKey;
    private static String cookieValue;
    private static String userID;
    
    @Mock
    private HttpServletRequest mockRequest;
    
    @Mock
    private HttpServletResponse mockResponse;
    
    @Mock
    private FilterChain mockFilterChain;
    
    @Mock
    private FilterConfig mockFilterConfig;
    
    @Captor
    private ArgumentCaptor<String> stringCaptor;
    
    @Captor
    private ArgumentCaptor<Integer> intCaptor;
    
    private AuthenticationRedirectFilter filter;
    private String expectedPrefix;
    
    @BeforeClass
    public static void setUpBeforeClass()
    {
        ClassLoader loader = Test.class.getClassLoader();
        URL cookieInfoUrl = loader.getResource(
                "esg/orp/app/cookie/sample_cookies/user-details-cookie-info");
        
        try
        {
            Path cookieInfoPath = Paths.get(cookieInfoUrl.toURI());
            
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
        }
        catch (URISyntaxException | IOException e)
        {
            ;
        }
    }
    
    @Before
    public void setUp() throws Exception
    {
        expectedPrefix = String.format("%s?%s=", AUTHENTICATE_URL, RETURN_QUERY_NAME);
        
        when(mockFilterConfig.getInitParameter(AUTHENTICATE_URL_PARAM)).thenReturn(
                AUTHENTICATE_URL);
        when(mockFilterConfig.getInitParameter(RETURN_QUERY_NAME_PARAM)).thenReturn(
                RETURN_QUERY_NAME);
        when(mockFilterConfig.getInitParameter(REQUEST_ATTRIBUTE_PARAM)).thenReturn(
                REQUEST_ATTRIBUTE);
        
        filter = new AuthenticationRedirectFilter();
        filter.init(mockFilterConfig);
    }
    
    @After
    public void tearDown() throws Exception
    {
        filter.destroy();
    }
    
    @Test
    public void testDoFilter() throws IOException, ServletException
    {
        // mock the getRequestURI() response
        StringBuffer requestUrl = new StringBuffer("http://localhost:8080/");
        when(mockRequest.getRequestURL()).thenReturn(requestUrl);
        
        filter.doFilter(mockRequest, mockResponse, mockFilterChain);
        
        // capture the redirect URL
        verify(mockResponse).sendRedirect(stringCaptor.capture());
        
        String result = stringCaptor.getValue();
        assertEquals(result, expectedPrefix + "http%3A%2F%2Flocalhost%3A8080%2F");
    }
    
    @Test
    public void testDoFilter_authenticated() throws URISyntaxException, IOException,
            ServletException
    {
        assertNotNull(secretKey);
        assertNotNull(cookieValue);
        
        when(mockFilterConfig.getInitParameter(SECRET_KEY_PARAM)).thenReturn(secretKey);
        when(mockFilterConfig.getInitParameter(COOKIE_NAME_PARAM)).thenReturn(COOKIE_NAME);
        
        filter = new AuthenticationRedirectFilter();
        filter.init(mockFilterConfig);
        
        Cookie[] cookies = new Cookie[1];
        cookies[0] = new Cookie(COOKIE_NAME, cookieValue);
        
        // insert the cookie into the request
        when(mockRequest.getCookies()).thenReturn(cookies);
        
        // mock the getRequestURI() response
        StringBuffer requestUrl = new StringBuffer("http://localhost:8080/");
        when(mockRequest.getRequestURL()).thenReturn(requestUrl);
        
        filter.doFilter(mockRequest, mockResponse, mockFilterChain);
        
        verify(mockResponse, never()).sendRedirect(anyString());
        
        // capture the request attribute, if assigned
        verify(mockRequest).setAttribute(eq(REQUEST_ATTRIBUTE), stringCaptor.capture());
        assertEquals(userID, stringCaptor.getValue());
    }

    @Test
    public void testDoFilter_badCookie() throws URISyntaxException, IOException, ServletException
    {
        assertNotNull(secretKey);
        String cookieValue = "";
        
        when(mockFilterConfig.getInitParameter(SECRET_KEY_PARAM)).thenReturn(secretKey);
        when(mockFilterConfig.getInitParameter(COOKIE_NAME_PARAM)).thenReturn(COOKIE_NAME);
        
        filter = new AuthenticationRedirectFilter();
        filter.init(mockFilterConfig);
        
        Cookie[] cookies = new Cookie[1];
        cookies[0] = new Cookie(COOKIE_NAME, cookieValue);
        
        // insert the cookie into the request
        when(mockRequest.getCookies()).thenReturn(cookies);
        
        // mock the getRequestURI() response
        StringBuffer requestUrl = new StringBuffer("http://localhost:8080/");
        when(mockRequest.getRequestURL()).thenReturn(requestUrl);
        
        filter.doFilter(mockRequest, mockResponse, mockFilterChain);
        
        verify(mockResponse).sendError(intCaptor.capture(), anyString());;
        
        int result = intCaptor.getValue();
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, result);
    }
    
    @Test
    public void testGetRedirectUrl_simpleAuthUrl() throws ServletException, MalformedURLException,
            UnsupportedEncodingException
    {
        String url, expectedUrl, redirectUrl;
        
        // Without query string
        url = "http://localhost:8080/";
        expectedUrl = expectedPrefix + "http%3A%2F%2Flocalhost%3A8080%2F";
        redirectUrl = filter.getRedirectUrl(url);
        
        assertEquals(redirectUrl, expectedUrl);
        
        // With query string
        url = "http://localhost:8080/?key=value";
        expectedUrl = expectedPrefix + "http%3A%2F%2Flocalhost%3A8080%2F%3Fkey%3Dvalue";
        redirectUrl = filter.getRedirectUrl(url);
        
        assertEquals(redirectUrl, expectedUrl);
    }

    @Test
    public void testGetRedirectUrl_complexAuthUrl() throws ServletException, MalformedURLException,
            UnsupportedEncodingException
    {
        String authenticateUrl = AUTHENTICATE_URL + "?key=value";
        String expectedPrefix = String.format("%s&%s=", authenticateUrl, RETURN_QUERY_NAME);
        
        when(mockFilterConfig.getInitParameter(AUTHENTICATE_URL_PARAM)).thenReturn(
                authenticateUrl);
        
        filter = new AuthenticationRedirectFilter();
        filter.init(mockFilterConfig);
        
        String url, expectedUrl, redirectUrl;
        
        // Without query string
        url = "http://localhost:8080/";
        expectedUrl = expectedPrefix + "http%3A%2F%2Flocalhost%3A8080%2F";
        redirectUrl = filter.getRedirectUrl(url);
        
        assertEquals(redirectUrl, expectedUrl);
        
        // With query string
        url = "http://localhost:8080/?key=value";
        expectedUrl = expectedPrefix + "http%3A%2F%2Flocalhost%3A8080%2F%3Fkey%3Dvalue";
        redirectUrl = filter.getRedirectUrl(url);
        
        assertEquals(redirectUrl, expectedUrl);
    }

}
