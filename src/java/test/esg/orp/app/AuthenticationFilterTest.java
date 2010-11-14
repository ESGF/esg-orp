package esg.orp.app;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class AuthenticationFilterTest {

	private AuthenticationFilter af;
	@Before
	public void setup() throws Exception {
		af = new AuthenticationFilter();
		//setup instance
		setField(af, "openidRelyingPartyUrl", "https://ipcc-ar5.dkrz.de/");
		setField(af, "policyService", new DummyPolicyServiceFilterCollaborator());
	}
	
	private static void setField(Object instance, String field, Object value ) throws Exception {
		Field f = instance.getClass().getDeclaredField(field);
		f.setAccessible(true);
		f.set(instance, value);
	}
	private static Object invoke(Object instance, String method, Object ... args ) throws Exception {
		Class<?>[] c = new Class[args.length];
		for (int i = 0; i < c.length; i++) {
			c[i] = args.getClass();
		}
		Method m = instance.getClass().getDeclaredMethod(method, c);
		m.setAccessible(true);
		return m.invoke(instance, args);
	}
	
	@Test
	public void testAttemptValidation() throws Exception {
		System.out.println(invoke(af, "retrieveORPCert"));
		
	}

}
