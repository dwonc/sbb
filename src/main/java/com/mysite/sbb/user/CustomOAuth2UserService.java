package com.mysite.sbb.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 회원가입 또는 업데이트
        SiteUser user = saveOrUpdate(registrationId, attributes);

        // 수정된 attributes 생성 (email을 nameAttributeKey로 사용)
        Map<String, Object> modifiedAttributes = new HashMap<>(attributes);
        modifiedAttributes.put("email", user.getEmail());

        // email을 key로 사용하여 DefaultOAuth2User 생성
        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            modifiedAttributes,
            "email"  // 여기가 핵심! sub 대신 email 사용
        );
    }

    private SiteUser saveOrUpdate(String registrationId, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");

        Optional<SiteUser> userOptional = userRepository.findByEmail(email);

        SiteUser user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setProvider(registrationId);
            user.setProviderId(providerId);
        } else {
            user = SiteUser.builder()
                    .username(email.split("@")[0])
                    .email(email)
                    .password("OAUTH_USER")
                    .provider(registrationId)
                    .providerId(providerId)
                    .build();
        }

        return userRepository.save(user);
    }
}