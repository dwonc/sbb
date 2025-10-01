package com.mysite.sbb.answer;

import java.security.Principal;

import jakarta.validation.Valid;


import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;

@RequestMapping("/answer")
@RequiredArgsConstructor
@Controller
public class AnswerController {

    private final AnswerService answerService;
	private final QuestionService questionService;
	private final UserService userService;
	
	private String getUserIdentifier(Principal principal) {
		if (principal instanceof Authentication) {
			Authentication auth = (Authentication) principal;
			if(auth.getPrincipal() instanceof OAuth2User) {
				OAuth2User oauth2User = (OAuth2User) auth.getPrincipal();
				// OAuth2 로그인 : email로 찾기
				return (String) oauth2User.getAttributes().get("email");
			}
		}
		return principal.getName();
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public String createAnswer(Model model, @PathVariable("id") Integer id,@Valid AnswerForm answerForm, BindingResult bindingResult, Principal principal) {
		Question question = this.questionService.getQuestion(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());
		
		if(bindingResult.hasErrors()) {
			model.addAttribute("question", question);
			return "question_detail";
		}
		
		Answer answer = this.answerService.create(question, answerForm.getContent(), siteUser);
		
		return String.format("redirect:/question/detail/%s#answer_%s", id, answer.getId());
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String answerModify(AnswerForm answerForm, @PathVariable ("id") Integer id, Principal principal) {
	
		String identifier = getUserIdentifier(principal);
	    SiteUser currentUser = this.userService.getUser(identifier); // identifier(ID 또는 Email)로 사용자 조회

		Answer answer = this.answerService.getAnswer(id);
		if (!answer.getAuthor().equals(currentUser)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다."); }
		answerForm.setContent(answer.getContent());
		return "answer_form";
		}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String answerModify(@Valid AnswerForm answerForm, BindingResult bindingResult, @PathVariable("id") Integer id, Principal principal) {
		if (bindingResult.hasErrors()) {
			return "answer_form";
		}
		String identifier = getUserIdentifier(principal);
	    SiteUser currentUser = this.userService.getUser(identifier); // identifier(ID 또는 Email)로 사용자 조회

		Answer answer = this.answerService.getAnswer(id);
		if (!answer.getAuthor().equals(currentUser)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"수정권한이 없습니다."); }
		
		this.answerService.modify(answer, answerForm.getContent());
		return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
		}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String answerDelete(Principal principal, @PathVariable("id") Integer id) {
		String identifier = getUserIdentifier(principal);
	    SiteUser currentUser = this.userService.getUser(identifier); // identifier(ID 또는 Email)로 사용자 조회

		Answer answer = this.answerService.getAnswer(id);
		if (!answer.getAuthor().equals(currentUser)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다."); }
		
		this.answerService.delete(answer);
		return String.format("redirect:/question/detail/%s", answer.getQuestion().getId());
		}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/vote/{id}")
	public String answerVote(Principal principal, @PathVariable("id") Integer id) {
		Answer answer = this.answerService.getAnswer(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());
		this.answerService.vote(answer, siteUser);
		return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
	}
	}
	
