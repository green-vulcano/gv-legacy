package it.greenvulcano.gvesb.adapter.http.security;

import javax.servlet.http.HttpServletRequest;

import it.greenvulcano.gvesb.iam.modules.Identity;
import it.greenvulcano.gvesb.identity.impl.HTTPIdentityInfo;

public class GVSecurityIdentityInfo extends HTTPIdentityInfo {

	private final Identity identity;

	public GVSecurityIdentityInfo(HttpServletRequest request, Identity identity) {
		super(request);
		this.identity = identity;
	}

	@Override
	public String getName() {
	 return identity.getName();
	}

	@Override
	protected boolean subIsInRole(String role) {
		return identity.getRoles().contains(role);
	}

}
