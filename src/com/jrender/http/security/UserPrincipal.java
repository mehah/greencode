package com.jrender.http.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;

public abstract class UserPrincipal implements Principal, Serializable {
	private static final long serialVersionUID = 1L;
	
	private final HashSet<String> rules = new HashSet<String>();
	
	protected final void addRule(String rule) { this.rules.add(rule); }
	protected final void addRules(String... rules) { this.rules.addAll(Arrays.asList(rules)); }
	public final boolean hasRule(String rule) { return rules.contains(rule); }
}
