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
import javax.servlet.http.HttpSession;

import org.stoevesand.brain.auth.Authorization;

public class AuthenticationFilter implements Filter {
	private FilterConfig config;

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		
		HttpSession session = ((HttpServletRequest) req).getSession(false);
		Authorization auth = (session != null) ? (Authorization) session.getAttribute("auth") : null;
		

		if (auth != null && auth.getIsLoggedIn() ) {
		    // Logged in.
			chain.doFilter(req, resp);
		} else {
			((HttpServletResponse) resp).sendRedirect("/login.jsf");
		}
	
	}

	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

	public void destroy() {
		config = null;
	}
}