package esg.orp.app;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

public class DummyPolicyServiceFilterCollaborator implements
		PolicyServiceFilterCollaborator {

	@Override
	public void init(FilterConfig filterConfig) {
		System.out.println("init");
	}

	@Override
	public boolean isSecure(HttpServletRequest request) {
		return true;
	}

	@Override
	public void destroy() {
		System.out.println("destroyed");
	}

}
