package com.mysite.sbb.question;

import com.mysite.sbb.answer.AnswerForm;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;


import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;

import lombok.RequiredArgsConstructor;

@RequestMapping("/question")
@RequiredArgsConstructor
@Controller
public class QuestionController {

	private final QuestionService questionService;
	private final UserService userService;
	
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value="page", defaultValue="0") int page, @RequestParam(value = "kw", defaultValue = "") String kw) {
		Page<Question> paging = this.questionService.getList(page, kw);
		model.addAttribute("paging", paging);
		model.addAttribute("kw", kw);
		return "question_list";
	}
	
	@GetMapping(value = "/detail/{id}")
	public String detail(Model model, @PathVariable("id") Integer id, AnswerForm answerForm) {
		
		Question question = this.questionService.getQuestion(id);
		this.questionService.increaseViewCount(question);
		model.addAttribute("question", question);
		return "question_detail";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create")
    public String questionCreate(QuestionForm questionForm) {
        return "question_form";
    }
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult,@RequestParam(value = "imageFile", required = false) MultipartFile imageFile, Principal principal) {
		if (bindingResult.hasErrors()) {
            return "question_form";
        }
		SiteUser siteUser = this.userService.getUser(principal.getName());
		try {
			this.questionService.create(questionForm.getSubject(), questionForm.getContent(), siteUser, imageFile);
		} catch (IOException e) {
			bindingResult.reject("iamgeUploadError", "이미지 업로드에 실패했습니다.");
			return "question_from";
		}
        return "redirect:/question/list"; // 질문 저장후 질문목록으로 이동
    }
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String questionModify(Model model, QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal) {
		Question question = this.questionService.getQuestion(id);
		if(!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다."); }
		questionForm.setSubject(question.getSubject());
		questionForm.setContent(question.getContent());
		questionForm.setImageUrl(question.getImageUrl());
		model.addAttribute("question", question);
		return "question_form";
		}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String questionModify(@Valid QuestionForm questionForm,BindingResult bindingResult,Principal principal,@PathVariable("id") Integer id,@RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
		if(bindingResult.hasErrors()) {
			return "question_form";
		}
		Question question = this.questionService.getQuestion(id);
		if(!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다."); }
		try {
			this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent(), imageFile);
		} catch (IOException e) {
			bindingResult.reject("imageUploadError", "이미지 업로드에 실패했습니다.");
			return "question_form";
		}
		return String.format("redirect:/question/detail/%s", id); 
		}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String questionDelete(Principal principal, @PathVariable("id") Integer id) {
		Question question = this.questionService.getQuestion(id);
		if(!question.getAuthor().getUsername().equals(principal.getName())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다."); }
		this.questionService.delete(question);
		return "redirect:/";
		}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/vote/{id}")
	public String questionVote(Principal principal, @PathVariable("id") Integer id) {
		Question question = this.questionService.getQuestion(id);
		SiteUser siteUser = this.userService.getUser(principal.getName());
		this.questionService.vote(question, siteUser);
		return String.format("redirect:/question/detail/%s", id);
	}
	}
	


