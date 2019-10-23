/*******************************************************************************
 * Copyright (c) 2009, 2016 GreenVulcano ESB Open Source Project.
 * All rights reserved.
 *
 * This file is part of GreenVulcano ESB.
 *
 * GreenVulcano ESB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GreenVulcano ESB is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GreenVulcano ESB. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package it.greenvulcano.gvesb.adapter.http.security;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

public class GVSecurityGuard {

    private final static Logger LOG = LoggerFactory.getLogger(GVSecurityGuard.class);

    private final static Set<ServiceReference<SecurityModule>> securityModulesReferences = new HashSet<>();

    static {

        try {
            securityModulesReferences.addAll(FrameworkUtil.getBundle(GVSecurityGuard.class).getBundleContext().getServiceReferences(SecurityModule.class, null));
        } catch (InvalidSyntaxException e) {
            LOG.error("Unable to retrieve service references", e);
        }

    }

    public static void authenticate(HttpServletRequest request, HttpServletResponse response) throws SecurityException, IOException {

        String authorization = Optional.ofNullable(request.getHeader("Authorization")).orElse("");
        if (securityModulesReferences != null) {
            LOG.debug("SecurityManager found, handling authentication");

            String wwwAuthenticate = "unknown";
            try {
                Optional<String> xRequestedWith = Optional.ofNullable(request.getHeader("X-Requested-With"));
                for (ServiceReference<SecurityModule> securityModuleRef : securityModulesReferences) {

                    SecurityModule securityModule = securityModuleRef.getBundle().getBundleContext().getService(securityModuleRef);
                    wwwAuthenticate = securityModule.getSchema() + " realm=" + securityModule.getRealm();
                    if (xRequestedWith.isPresent()) {
                        wwwAuthenticate = xRequestedWith.get() + "+" + wwwAuthenticate;
                    }
                    Optional<Identity> identity = securityModule.resolve(authorization);

                    if (identity.isPresent()) {
                        request.setAttribute(Identity.class.getName(), identity.get());
                        LOG.debug("User authenticated: " + identity.get().getName());
                        break;
                    }
                    
                }

            } catch (UserExpiredException | CredentialsExpiredException userExpiredException) {
                response.setHeader("WWW-Authenticate", wwwAuthenticate);
                response.setHeader("X-Auth-Status", "Expired");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                
                throw new SecurityException(userExpiredException);
                
            } catch (PasswordMissmatchException | UserNotFoundException | InvalidCredentialsException unauthorizedException) {                
                response.setHeader("WWW-Authenticate", wwwAuthenticate);
                response.setHeader("X-Auth-Status", "Denied");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                
                throw new SecurityException(unauthorizedException);
            } catch (Exception e) {               
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                
                throw new SecurityException(e);
            }            
            
        }
        

    }

}
