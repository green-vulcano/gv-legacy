package it.greenvulcano.gvesb.core.forward.security;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.greenvulcano.gvesb.iam.exception.GVSecurityException;
import it.greenvulcano.gvesb.iam.modules.Identity;
import it.greenvulcano.gvesb.iam.modules.SecurityModule;

public class JMSMessageIdentityResolver {
	
	private final static Logger LOG = LoggerFactory.getLogger(JMSMessageIdentityResolver.class);	
	
	private static Set<ServiceReference<SecurityModule>> securityModuleRefs = new LinkedHashSet<>();
	
	
	public static void init(BundleContext context) {
		try {
			securityModuleRefs.addAll(context.getServiceReferences(SecurityModule.class, null));
		} catch (InvalidSyntaxException e) {
			LOG.error("Unable to retrieve service references", e);
		}
	}
	
	public static Optional<JMSMessageIdentityInfo> resolveIdentity(Message message) {
		String messageId = "";
		
		try {
			
			messageId = message.getJMSMessageID();
		
			if (message.propertyExists(JMSMessageIdentityInfo.IDENTITY_KEY)) {
			
				String authorization = message.getStringProperty(JMSMessageIdentityInfo.IDENTITY_KEY);
				
				for (ServiceReference<SecurityModule> securityModuleRef : securityModuleRefs) {
					
					SecurityModule securityModule = securityModuleRef.getBundle().getBundleContext().getService(securityModuleRef);
					
					Optional<Identity> identity = securityModule.resolve(authorization);
					
					if (identity.isPresent()) {						
						return Optional.of(new JMSMessageIdentityInfo(identity.get()));
					}
					
				}
				
			}		
		} catch (JMSException e) {
			LOG.error("JMSMessageIdentityResolver - Unable to retrieve authorization metadata", e);
		} catch (GVSecurityException e) {
			LOG.warn("JMSMessageIdentityResolver - Autentication fail for message "+messageId, e);
		}
		
		return Optional.empty();
		
	}
	
	
	public static void destroy() {
		securityModuleRefs.clear();
	}
	
	
}
