package com.mysite.sbb.freeboard;

import java.io.IOException;
import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionForm;
import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/free")
@RequiredArgsConstructor
@Controller

public class FreeBoardController {

	private final FreeBoardService freeBoardService;
	private final UserService userService;

	@GetMapping("/list")
	public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "kw", defaultValue = "") String kw) {
		Page<FreeBoard> paging = this.freeBoardService.getList(page, kw);
		model.addAttribute("kw", kw);
		model.addAttribute("paging", paging);
		return "freeboard_list";
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/create")
	public String freeBoardCreate(Model model) {
		model.addAttribute("freeboardForm", new FreeBoardForm());
		return "freeboard_form";
	}

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
	@PostMapping("/create")
    public String freeBoardCreate(@Valid FreeBoardForm freeboardForm, BindingResult bindingResult,@RequestParam(value = "imageFile", required = false) MultipartFile imageFile, Principal principal) {
		if (bindingResult.hasErrors()) {
            return "freeboard_form";
        }
		String identifier = getUserIdentifier(principal);
		SiteUser siteUser = this.userService.getUser(identifier);
		try {
			this.freeBoardService.create(freeboardForm.getSubject(), freeboardForm.getContent(), siteUser, imageFile);
		} catch (IOException e) {
			bindingResult.reject("iamgeUploadError", "이미지 업로드에 실패했습니다.");
			return "freeboard_from";
		}
        return "redirect:/free/list"; // 질문 저장후 질문목록으로 이동
    }

	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Integer id) {
	    FreeBoard freeboard = this.freeBoardService.getFreeBoard(id);
		this.freeBoardService.increaseViewCount(freeboard);
	    model.addAttribute("freeboard", freeboard);
	    model.addAttribute("freeCommentForm", new FreeCommentForm()); // 추가 필요
	    return "freeboard_detail";
	}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/modify/{id}")
	public String freeBoardModify(Model model, @PathVariable("id") Integer id, Principal principal) {
		String identifier = getUserIdentifier(principal);
	    SiteUser currentUser = this.userService.getUser(identifier); // identifier(ID 또는 Email)로 사용자 조회

		FreeBoard freeBoard = this.freeBoardService.getFreeBoard(id);
		
		if(!freeBoard.getAuthor().equals(currentUser)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다."); }
		
		FreeBoardForm freeboardForm = new FreeBoardForm();
		freeboardForm.setId(freeBoard.getId());
		freeboardForm.setSubject(freeBoard.getSubject());
		freeboardForm.setContent(freeBoard.getContent());
		freeboardForm.setImageUrl(freeBoard.getImageUrl());
		model.addAttribute("freeboardForm", freeboardForm);
		return "freeboard_form";
		}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public String freeBoardModify(@Valid FreeBoardForm freeboardForm,
			BindingResult bindingResult,
			Principal principal,@PathVariable("id") Integer id,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
		if(bindingResult.hasErrors()) {
			return "freeboard_form";
		}
		String identifier = getUserIdentifier(principal);
	    SiteUser currentUser = this.userService.getUser(identifier); // identifier(ID 또는 Email)로 사용자 조회

		FreeBoard freeboard = this.freeBoardService.getFreeBoard(id);
		
		if(!freeboard.getAuthor().equals(currentUser)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다."); }
		try {
			this.freeBoardService.modify(freeboard, freeboardForm.getSubject(), freeboardForm.getContent(), imageFile);
		} catch (IOException e) {
			bindingResult.reject("imageUploadError", "이미지 업로드에 실패했습니다.");
			return "freeboard_form";
		}
		return String.format("redirect:/free/detail/%s", id); 
		}
	
	@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public String freeBoardDelete(Principal principal, @PathVariable("id") Integer id) {
		String identifier = getUserIdentifier(principal);
	    SiteUser currentUser = this.userService.getUser(identifier); // identifier(ID 또는 Email)로 사용자 조회
	    
		FreeBoard freeboard = this.freeBoardService.getFreeBoard(id);
		if(!freeboard.getAuthor().equals(currentUser)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제 권한이 없습니다."); }
		this.freeBoardService.delete(freeboard);
		return "redirect:/";
		}
}
