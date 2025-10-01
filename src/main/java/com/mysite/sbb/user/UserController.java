package com.mysite.sbb.user;

import java.security.Principal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.answer.AnswerService;
import com.mysite.sbb.freeboard.FreeBoard;
import com.mysite.sbb.freeboard.FreeBoardService;
import com.mysite.sbb.freeboard.FreeComment;
import com.mysite.sbb.freeboard.FreeCommentService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

	private final UserService userService;
	private final QuestionService questionService;
	private final AnswerService answerService;
	private final FreeBoardService freeboardService;
	private final FreeCommentService freecommentService;
	
	// 헬퍼 메서드 추가
	private String getUserIdentifier(Principal principal) {
		if (principal instanceof Authentication) {
			Authentication auth = (Authentication) principal;
			if (auth.getPrincipal() instanceof OAuth2User) {
				OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
				return (String) oauth2User.getAttributes().get("email");
			}
		}
		return principal.getName();
	}
	
	@GetMapping("/signup")
	public String signup(UserCreateForm userCreateForm) {
		return "signup_form";
	}
	
	@PostMapping("/signup")
	public String signuo(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			return "signup_form";
		}
		
		if(!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
			bindingResult.rejectValue("password2", "passwordIncorrect", "2개의 패스워드가 일치하지 않습니다.");
			return "signup_form";
		}
		try {
			userService.create(userCreateForm.getUsername(), userCreateForm.getEmail(), userCreateForm.getPassword1());	
		} catch(DataIntegrityViolationException e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
			return "signup_form";
		} catch(Exception e) {
			e.printStackTrace();
			bindingResult.reject("signupFailed", e.getMessage());
			return "signup_form";
		}
		return "redirect:/";
	}
	
	@GetMapping("/login")
	public String login() {
		return "login_form";
	}
	
	@GetMapping("/profile/{identifier}")
	public String profile(@PathVariable("identifier") String identifier, Model model) {
	    // identifier는 username 또는 email일 수 있음
	    SiteUser user = userService.getUser(identifier);
	    
	    model.addAttribute("user", user);
	    
	    List<Question> questions = questionService.getQuestionsByUser(user);
	    model.addAttribute("questions", questions);
	    
	    List<Answer> answers = answerService.getAnswerByUser(user);
	    model.addAttribute("answers", answers);
	    
	    List<FreeBoard> freeBoards = freeboardService.getFreeboardByUser(user);
	    model.addAttribute("freeBoards", freeBoards);
	    
	    List<FreeComment> freeComments = freecommentService.getFreecommentByUser(user);
	    model.addAttribute("freeComments", freeComments);
	    
	    return "user_profile";
	}
	
	@GetMapping("/profile/{identifier}/modify")
	public String modify(UserModifyForm userModifyForm, Model model, @PathVariable("identifier") String identifier) {
		SiteUser user = userService.getUser(identifier);
		model.addAttribute("user", user);
		return "modify_form";
	}
	
	@PostMapping("/profile/{identifier}/modify")
	public String modify(@Valid UserModifyForm userModifyForm, Model model, 
	                     @PathVariable("identifier") String identifier, BindingResult bindingResult) {
		SiteUser user = userService.getUser(identifier);
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("user", user);
			return "modify_form";
		}
		
		String password1 = userModifyForm.getPassword1();
		String password2 = userModifyForm.getPassword2();
		String email = userModifyForm.getEmail();

		if(!password1.isEmpty()) {
			if(!password1.equals(password2)) {
				bindingResult.rejectValue("password2", "passwordIncorrect", "2개의 비밀번호가 일치하지 않습니다.");
				model.addAttribute("user", user);
				return "modify_form";
			}
			userService.modifyPassword(user, password1);
		}
		
		if(email != null && !email.equals(user.getEmail())) {
			userService.modifyEmail(user, email);	
		}
		
		// 수정 후 프로필로 리다이렉트 (username 사용)
		return "redirect:/user/profile/" + user.getUsername();
	}
}