package it.greenvulcano.gvesb.core.forward.security;

import java.util.Objects;
import java.util.stream.Collectors;

import it.greenvulcano.gvesb.iam.modules.Identity;
import it.greenvulcano.gvesb.identity.impl.BaseIdentityInfo;

public class JMSMessageIdentityInfo extends BaseIdentityInfo {
	
	public static final String IDENTITY_KEY = "GVIAM_AUTH_TOKEN";
	
	private final Identity identity;
		
	JMSMessageIdentityInfo(Identity identity) {
		
		this.identity = Objects.requireNonNull(identity);
		
		getAttributes().put("id", identity.getId().toString());
		getAttributes().put("roles", identity.getRoles().stream().collect(Collectors.joining(",")));
	}

	@Override
	public String getName() {
		
		return identity.getName();
	}

	@Override
	protected boolean subIsInRole(String role) {
		
		return identity.getRoles().contains(role);
	}

	@Override
	protected boolean subMatchAddress(String address) {		
		return false;
	}

	@Override
	protected boolean subMatchAddressMask(String addressMask) {		
		return false;
	}



}
