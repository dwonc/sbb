package com.mysite.sbb.freeboard;

import java.security.Principal;

import jakarta.validation.Valid;

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
import org.springframework.validation.BindingResult;

@RequestMapping("/freecomment")
@RequiredArgsConstructor
@Controller
public class FreeCommentController {

    private final FreeCommentService freeCommentService;
    private final FreeBoardService freeBoardService;
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
    public String createComment(Model model, @PathVariable("id") Integer id, @Valid FreeCommentForm freeCommentForm, BindingResult bindingResult, Principal principal) {
        FreeBoard freeBoard = this.freeBoardService.getFreeBoard(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());

        if(bindingResult.hasErrors()) {
            model.addAttribute("freeBoard", freeBoard);
            return "freeboard_detail";
        }

        FreeComment freeComment = this.freeCommentService.create(freeBoard, freeCommentForm.getContent(), siteUser);

        return String.format("redirect:/free/detail/%s#freecomment_%s", id, freeComment.getId());
    }
    
  
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String freeCommentModify(Model model, FreeCommentForm freeCommentForm, @PathVariable("id") Integer id, Principal principal) {
    	String identifier = getUserIdentifier(principal);
	    SiteUser currentUser = this.userService.getUser(identifier); // identifier(ID 또는 Email)로 사용자 조회

    	FreeComment freeComment = this.freeCommentService.getFreeComment(id);
        if (!freeComment.getAuthor().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        freeCommentForm.setContent(freeComment.getContent());
        model.addAttribute("freeBoard", freeComment.getFreeBoard());
        return "freecomment_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String freeCommentModify(@Valid FreeCommentForm freeCommentForm, BindingResult bindingResult, @PathVariable("id") Integer id, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "freecomment_form";
        }
        String identifier = getUserIdentifier(principal);
	    SiteUser currentUser = this.userService.getUser(identifier); // identifier(ID 또는 Email)로 사용자 조회

        FreeComment freeComment = this.freeCommentService.getFreeComment(id);
        if (!freeComment.getAuthor().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        this.freeCommentService.modify(freeComment, freeCommentForm.getContent());
        return String.format("redirect:/free/detail/%s#freecomment_%s", freeComment.getFreeBoard().getId(), freeComment.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String freeCommentDelete(Principal principal, @PathVariable("id") Integer id) {
    	String identifier = getUserIdentifier(principal);
	    SiteUser currentUser = this.userService.getUser(identifier); // identifier(ID 또는 Email)로 사용자 조회

        FreeComment freeComment = this.freeCommentService.getFreeComment(id);
        if (!freeComment.getAuthor().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }

        this.freeCommentService.delete(freeComment);
        return String.format("redirect:/free/detail/%s", freeComment.getFreeBoard().getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String freeCommentVote(Principal principal, @PathVariable("id") Integer id) {
        FreeComment freeComment = this.freeCommentService.getFreeComment(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.freeCommentService.vote(freeComment, siteUser);
        return String.format("redirect:/free/detail/%s#freecomment_%s", freeComment.getFreeBoard().getId(), freeComment.getId());
    }
}