package org.stoevesand.brain.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



/**
 * Servlet filter which disables URL-encoded session identifiers.
 * 
 * <pre>
 * Copyright (c) 2006, Craig Condit. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &quot;AS IS&quot;
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
@SuppressWarnings("deprecation")
public class DisableUrlSessionFilter implements Filter {

	private static Logger log = LogManager.getLogger(DisableUrlSessionFilter.class);

	private boolean isSEOBrowser = false;
	private String regex = "";

	/**
	 * Filters requests to disable URL-based session identifiers.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// skip non-http requests
		if (!(request instanceof HttpServletRequest)) {
			chain.doFilter(request, response);
			return;
		}

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		String userAgent = httpRequest.getHeader("User-Agent").toLowerCase();
		isSEOBrowser = userAgent.matches(regex);

		// clear session if session id in URL
		if (httpRequest.isRequestedSessionIdFromURL()) {
			HttpSession session = httpRequest.getSession();
			if (session != null)
				session.invalidate();
		}

		if (isSEOBrowser) {

			// log.debug("SEO Browser: " + userAgent);
			// wrap response to remove URL encoding
			HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(httpResponse) {
				@Override
				public String encodeRedirectUrl(String url) {
					return url;
				}

				@Override
				public String encodeRedirectURL(String url) {
					return url;
				}

				@Override
				public String encodeUrl(String url) {
					return url;
				}

				@Override
				public String encodeURL(String url) {
					return url;
				}
			};

			chain.doFilter(request, wrappedResponse);

		} else {
			chain.doFilter(request, httpResponse);
		}
	}

	/**
	 * Unused.
	 */
	public void init(FilterConfig config) throws ServletException {
		regex = ".*" + config.getInitParameter("SEOBrowser") + ".*";
		System.out.println("SEOBrowser Pattern: " + regex);
	}

	/**
	 * Unused.
	 */
	public void destroy() {
	}
}
