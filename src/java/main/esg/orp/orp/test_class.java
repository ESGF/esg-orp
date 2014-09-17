package esg.orp.orp;

import java.io.IOException;

import org.openid4java.message.ParameterList;
import org.springframework.security.openid.OpenIDAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;

//import esg.idp.server.web.OpenidPars;

public class test_class extends OpenIDAuthenticationFilter
{
  public Authentication attemptAuthentication(javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response)   throws AuthenticationException,IOException
  {
	// retrieve HTTP parameters from request or session
    // final ParameterList parameterList = getParameterList(request);
    // final String mode = parameterList.getParameterValue(OpenidPars.OPENID_MODE);
    Authentication local_aut = null;
    
    /*
    Map map = request.getParameterMap();
    
    for(Object key : map.keySet())
    {
      String keyStr = (String)key;
      Object value = map.get(keyStr);     
      value = value;
      //System.out.println("Key " + (String)key + "     :    " + value);
    }
    */
    //setClaimedIdentityFieldName("https%3A%2F%2Flocalhost%3A8443%2Fesgf-idp%2Fopenid%2F");
    
    //try
    //{
	  local_aut = super.attemptAuthentication(request, response);
    //}
   // catch(IOException e)
   // {
   // 	e= e;
   // }
    //catch(AuthenticationException e)
    //{
   // 	e=e;
   // }
	
	return local_aut;	  
  }  
  
}
