package com.mysite.sbb.user;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
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
import org.springframework.web.bind.annotation.RequestParam;


@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

	private final UserService userService;
	private final QuestionService questionService;
	private final AnswerService answerService;
	private final FreeBoardService freeboardService;
	private final FreeCommentService freecommentService;
	
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
	
	@GetMapping("/profile/{username}")
	public String profile(@PathVariable("username") String username, Model model) {
	    SiteUser user = userService.getUser(username);
	    
	    model.addAttribute("user", user);
	    
	    List<Question> questions = questionService.getQuestionsByUser(user);
	    model.addAttribute("questions", questions);
	    
	    List<Answer> answers = answerService.getAnswerByUser(user);
	    model.addAttribute("answers", answers);
	    
	    List<FreeBoard> freeBoards = freeboardService.getFreeboardByUser(user);
	    model.addAttribute("freeBoards", freeBoards);  // 이 부분이 정확히 이렇게 되어있는지 확인
	    
	    List<FreeComment> freeComments = freecommentService.getFreecommentByUser(user);
	    model.addAttribute("freeComments", freeComments);
	    
	    return "user_profile";
	}
	
	@GetMapping("/profile/{username}/modify")
	public String modify(UserModifyForm userModifyForm,Model model,SiteUser user) 
	{
		model.addAttribute("user", user);
		return "modify_form";
	}
	
	
	@PostMapping("/profile/{username}/modify")
	public String modify(@Valid UserModifyForm userModifyForm, Model model, @PathVariable("username") String username,BindingResult bindingResult) {
		SiteUser user = userService.getUser(username);
		
		if (bindingResult.hasErrors()) {
			return "modify_form";
		}
		
		String password1 = userModifyForm.getPassword1();
		String password2 = userModifyForm.getPassword2();
		String email = userModifyForm.getEmail();


		if(!password1.isEmpty()) {
			if(!password1.equals(password2)) {
				bindingResult.rejectValue("password2", "passwordIncorrect", "2개의 비밀번호가 일치하지 않습니다.");
				return "modify_form";
			}
		}
		
		userService.modifyPassword(user, password1);
	
	if(email != null && !email.equals(user.getEmail())) {
		userService.modifyEmail(user, email);	
	}
	
	return "redirect:/user/profile/" + username;
	}
}
