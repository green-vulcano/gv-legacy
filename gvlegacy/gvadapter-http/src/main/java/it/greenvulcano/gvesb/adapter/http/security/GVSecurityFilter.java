package it.greenvulcano.gvesb.adapter.http.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.iam.modules.SecurityModule;

public class GVSecurityFilter implements Filter {

	private final static Logger LOG = LoggerFactory.getLogger(GVSecurityFilter.class);

	private final Set<ServiceReference<SecurityModule>> securityModulesReferences = new HashSet<>();
		
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
			
		try {
			securityModulesReferences.addAll(FrameworkUtil.getBundle(getClass()).getBundleContext().getServiceReferences(SecurityModule.class, null));
		} catch (InvalidSyntaxException e) {
			LOG.error("Unable to retrieve service references", e);
		}		
		
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)	throws IOException, ServletException {
				
		HttpServletRequest servletRequest = HttpServletRequest.class.cast(request);
		HttpServletResponse servletResponse =  HttpServletResponse.class.cast(response);
		
		if (servletRequest.getMethod().equalsIgnoreCase("OPTIONS") && 
			Objects.nonNull(servletRequest.getHeader("Access-Control-Request-Method"))){
			
			servletResponse.addHeader("Access-Control-Allow-Origin", Optional.ofNullable(servletRequest.getHeader("Origin")).orElse("*"));
			servletResponse.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
			servletResponse.addHeader("Access-Control-Allow-Headers", Optional.ofNullable(servletRequest.getHeader("Access-Control-Request-Headers")).orElse("Content-Type"));
			servletResponse.addHeader("Access-Control-Max-Age", "86400");
			
			servletResponse.addHeader("Content-Length", "0");
			servletResponse.addHeader("Connection", "keep-alive");
			servletResponse.setStatus(HttpServletResponse.SC_OK);
			return;
			
		} else {
			servletResponse.addHeader("Access-Control-Allow-Origin","*");
			servletResponse.addHeader("Access-Control-Allow-Credentials", "true");
			servletResponse.addHeader("Access-Control-Expose-Headers", "Content-Type, Content-Range, X-Auth-Status");
		}	
		
		chain.doFilter(servletRequest, servletResponse);

	}

	@Override
	public void destroy() {
	}

}
