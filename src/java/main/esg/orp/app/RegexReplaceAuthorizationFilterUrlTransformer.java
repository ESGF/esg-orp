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

import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;

import esg.orp.Parameters;

/**
 * Implementation of {@link AuthorizationFilterUrlTransformer} that applies a set of regular
 * expression replacements. The initialisation parameter defining the replacements has the following
 * form:
 * regex1:replacement1,regex2:replacement2,...
 * The regex and replacement values may be quoted with double quotes. Whitespace outside of quotes
 * is ignored. Replacement values may be empty to delete strings matched by the corresponding regex.
 * The replacements are executed in the order in which they are defined by the parameter value.
 */
public class RegexReplaceAuthorizationFilterUrlTransformer implements AuthorizationFilterUrlTransformer {
	/**
	 * Holds details of one of the replacements to be made.
	 */
	private class Replacement {
		private Pattern pattern;
		private String replacementString;

		public Replacement(String[] mapStrings) {
			pattern = Pattern.compile(mapStrings[0]);
			replacementString = mapStrings[1];
		}

		public String replace(String inStr) {
			return pattern.matcher(inStr).replaceAll(replacementString);
		}
	}

	private ArrayList<Replacement> replacements;

	/** Initialises the replacements strings from the values set for the AuthorizationFiler's
	 * urlTransformerReplacements initialisation parameter.
	 * @see esg.orp.app.AuthorizationFilterUrlTransformer#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) {
		String replacementsStr = filterConfig.getInitParameter(Parameters.AUTHORIZATION_URL_TRANSFORMER_REPLACEMENTS);
		replacements = parseReplacementString(replacementsStr);
	}

	/** Transforms a URL by applying a set of regular expression replacements in the order in which
	 * they are defined.
	 * @see esg.orp.app.AuthorizationFilterUrlTransformer#transformUrl(java.lang.String)
	 */
	@Override
	public String transformUrl(String url) {
		String result = url;
		for (Replacement repl : replacements) {
			result = repl.replace(result);
		}
		return result;
	}

	/**
	 * Parses the initialisation parameter to obtain the set of replacements. Regexs and replacement
	 * values are divided at ',' and ':' characters.Tokens may be quoted with double quotes, empty
	 * strings are allowed and whitespace characters outside of quotes are discarded.
	 * @param inStr string to parsed
	 * @return regular expressions and replacement values
	 */
	private ArrayList<Replacement> parseReplacementString(String inStr) {
		ArrayList<Replacement> result = new ArrayList<Replacement>();

		StrTokenizer tokenizer = new StrTokenizer(inStr, StrMatcher.charSetMatcher(",:"), StrMatcher.charSetMatcher("\""));
		tokenizer.setIgnoreEmptyTokens(false);
		tokenizer.setIgnoredMatcher(StrMatcher.charSetMatcher(" \t\n\r"));

		int idx = 0;
		String[] mapStrs = new String[] {"", ""};
		while (tokenizer.hasNext()) {
			String tok = (String) tokenizer.next();
			mapStrs[idx] = tok;
			if (idx == 1) {
				result.add(new Replacement(mapStrs));
				mapStrs = new String[] {"", ""};
			}
			idx = (idx + 1) % 2;
		}
		if (idx == 1) {
			result.add(new Replacement(mapStrs));
		}

		return result;
	}
}
