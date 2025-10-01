package com.mysite.sbb.user;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;

@Setter
@Getter
public class UserModifyForm {

	private String password1;
	
	private String password2;
	
	@Email
	private String email;
}
