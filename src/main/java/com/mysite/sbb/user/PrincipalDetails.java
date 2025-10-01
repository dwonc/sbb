package com.mysite.sbb.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class PrincipalDetails implements UserDetails, OAuth2User{
	
	private SiteUser user;
	private Map<String, Object> attributes;
	private String attributeKey;
	
	//일반 로그인용 생성자
	public PrincipalDetails(SiteUser user) {
		this.user = user;
	}

	//OAuth2 로그인용 생성자
	public PrincipalDetails(SiteUser user, Map<String, Object> attributes, String attributeKey) {
		this.user = user;
		this.attributes = attributes;
		this.attributeKey = attributeKey;
	}
	
	//UserDetails 메서드들
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
	}
	
	@Override
	public String getPassword() {
		return user.getPassword();
	}
	
	@Override
	public String getUsername() {
		return user.getUsername();
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	
	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	@Override
	public String getName() {
		return attributeKey != null ? attributes.get(attributeKey).toString() : null;
	}
	
	public SiteUser getUser() {
		return user;
	}
}
