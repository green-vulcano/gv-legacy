package it.greenvulcano.gvesb.adapter.http.security;

import java.io.IOException;
import java.util.HashSet;
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

import it.greenvulcano.gvesb.iam.exception.CredentialsExpiredException;
import it.greenvulcano.gvesb.iam.exception.InvalidCredentialsException;
import it.greenvulcano.gvesb.iam.exception.PasswordMissmatchException;
import it.greenvulcano.gvesb.iam.exception.UserExpiredException;
import it.greenvulcano.gvesb.iam.exception.UserNotFoundException;
import it.greenvulcano.gvesb.iam.modules.Identity;
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
		
		String authorization = Optional.ofNullable(servletRequest.getHeader("Authorization")).orElse("");
		if (securityModulesReferences!=null) {				
			LOG.debug("SecurityManager found, handling authentication");
			
			try {
				
				for (ServiceReference<SecurityModule> securityModuleRef :  securityModulesReferences) {
					
					SecurityModule securityModule = securityModuleRef.getBundle().getBundleContext().getService(securityModuleRef);
					Optional<Identity> identity = securityModule.resolve(authorization);
					
					if (identity.isPresent()) {
						
						servletRequest.setAttribute(Identity.class.getName(), identity.get());
								        		
		        		LOG.debug("User authenticated: "+ identity.get().getName());
		        		
		        		break;
					}				
				}
				
				chain.doFilter(request, response);
				
			} catch (UserExpiredException|CredentialsExpiredException userExpiredException) {	        		
        		servletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
			} catch (PasswordMissmatchException|UserNotFoundException|InvalidCredentialsException unauthorizedException){
				LOG.warn("Failed to authenticate user", unauthorizedException);
				servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        	} catch (Exception e) {
        		LOG.warn("Authentication process failed", e);
        		servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			}
		}

	}

	@Override
	public void destroy() {
	}

}