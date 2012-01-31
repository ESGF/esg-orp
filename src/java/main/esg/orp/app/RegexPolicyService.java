/*******************************************************************************
 * Copyright (c) 2011 Earth System Grid Federation
 * ALL RIGHTS RESERVED. 
 * U.S. Government sponsorship acknowledged.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package esg.orp.app;

import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import esg.orp.Parameters;
import esg.orp.Utils;

/**
 * PolicyService that controls whether authentication and authorization is required depending on
 * whether the request URL matches any of a set of configured regular expressions.
 */
public class RegexPolicyService implements PolicyServiceFilterCollaborator {

	private boolean matchResult;
	private ArrayList<Pattern> patterns;
	private final Log LOG = LogFactory.getLog(this.getClass());

	public void destroy() {}

	/* (non-Javadoc)
	 * @see esg.orp.app.PolicyServiceFilterCollaborator#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) {
		String authRequiredPatternStr = filterConfig.getInitParameter(Parameters.AUTHENTICATION_REQUIRED_PATTERNS);
		String authNotRequiredPatternStr = filterConfig.getInitParameter(Parameters.AUTHENTICATION_NOT_REQUIRED_PATTERNS);
		if ((authRequiredPatternStr != null) && (authNotRequiredPatternStr != null)) {
			LOG.error(String.format("Only one of the initialisation parameters %s and %s should be specified.",
					Parameters.AUTHENTICATION_REQUIRED_PATTERNS, Parameters.AUTHENTICATION_NOT_REQUIRED_PATTERNS));
		}
		String patternStr = null;
		// Find the pattern parameter - if both are specified, only act on the access denied patterns.
		if (authNotRequiredPatternStr != null) {
			patternStr = authNotRequiredPatternStr;
			matchResult = false;
		} else if (authRequiredPatternStr != null) {
			patternStr = authRequiredPatternStr;
			matchResult = true;
		}
		if (patternStr == null) {
			LOG.error(String.format("One of the initialisation parameters %s and %s should be specified.",
					Parameters.AUTHENTICATION_REQUIRED_PATTERNS, Parameters.AUTHENTICATION_NOT_REQUIRED_PATTERNS));
			// Default to requiring authorization for all URLs.
			patternStr = "";
			matchResult = false;
		}
		patterns = parsePatternString(patternStr);
	}

	/**
	 * Checks whether the URL matches any of the configured patterns.
	 * @see esg.orp.app.PolicyServiceFilterCollaborator#isSecure(javax.servlet.http.HttpServletRequest)
	 * @param request
	 * @return true if the URL requires authentication, otherwise false
	 */
	public boolean isSecure(HttpServletRequest request) {
		String url = Utils.getFullRequestUrl(request);
		for (Pattern pat : patterns) {
			if (pat.matcher(url).matches()) {
				return matchResult;
			}
		}
		return !matchResult;
	}

	/**
	 * Parses the pattern string, which should be a comma separated list of regular expressions,
	 * each of which may be surrounded with double quotes.
	 * @param inStr pattern string to parse
	 * @return list of pattern regular expressions
	 */
	private ArrayList<Pattern> parsePatternString(String inStr) {
		ArrayList<Pattern> result = new ArrayList<Pattern>();

		StrTokenizer tokenizer = new StrTokenizer(inStr, ',', '"');
		tokenizer.setIgnoreEmptyTokens(true);
		tokenizer.setIgnoredMatcher(StrMatcher.charSetMatcher(" \t\n\r"));

		while (tokenizer.hasNext()) {
			String tok = (String) tokenizer.next();
			Pattern pat = Pattern.compile(tok);
			result.add(pat);
		}

		return result;
	}
}
