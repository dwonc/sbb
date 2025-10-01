package com.mysite.sbb.user;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import com.mysite.sbb.DataNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public SiteUser create(String username, String email, String password) {
		SiteUser user = new SiteUser();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		this.userRepository.save(user);
		return user;
	}
	
	public SiteUser getUser(String identifier) {
		System.out.println("==========================================");
		System.out.println("getUser 호출됨 - identifier: [" + identifier + "]");
		
		// 먼저 username으로 찾기
		Optional<SiteUser> siteUser = this.userRepository.findByUsername(identifier);
		System.out.println("username으로 찾기 결과: " + siteUser.isPresent());
		
		// 못 찾으면 email로 찾기 (OAuth2 로그인 대응)
		if (siteUser.isEmpty()) {
			System.out.println("email로 찾기 시도...");
			siteUser = this.userRepository.findByEmail(identifier);
			System.out.println("email로 찾기 결과: " + siteUser.isPresent());
		}
		
		if (siteUser.isPresent()) {
			SiteUser user = siteUser.get();
			System.out.println("찾은 사용자 - username: " + user.getUsername() + ", email: " + user.getEmail());
			System.out.println("==========================================");
			return user;
		} else {
			System.out.println("사용자를 찾을 수 없음!");
			System.out.println("==========================================");
			throw new DataNotFoundException("siteuser not found");
		}
	}
	
	public void modifyPassword(SiteUser user, String newPassword) {
		user.setPassword(passwordEncoder.encode(newPassword));
		this.userRepository.save(user);
	}
	
	public void modifyEmail(SiteUser user, String newEmail) {
		user.setEmail(newEmail);
		this.userRepository.save(user);
	}
}