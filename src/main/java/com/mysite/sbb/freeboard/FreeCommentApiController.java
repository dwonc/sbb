package com.mysite.sbb.freeboard;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/freecomment")
@RequiredArgsConstructor
public class FreeCommentApiController {
	
	private final FreeCommentService freeCommentService;
	private final FreeBoardService freeBoardService;
	private final UserService userService;
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public ResponseEntity<?> createComment(
			@PathVariable("id") Integer id,
			@Valid @RequestBody FreeCommentForm freeCommentForm,
			BindingResult bindingResult,
			Principal principal) {
		
		 FreeBoard freeBoard = this.freeBoardService.getFreeBoard(id);
	        SiteUser siteUser = this.userService.getUser(principal.getName());

	        if(bindingResult.hasErrors()) {
	            return ResponseEntity.badRequest()
	                    .body(Map.of("error", bindingResult.getAllErrors()));
	        }

	        FreeComment freeComment = this.freeCommentService.create(freeBoard, freeCommentForm.getContent(), siteUser);

	        Map<String, Object> response = new HashMap<>();
	        response.put("id", freeComment.getId());
	        response.put("content", freeComment.getContent());
	        response.put("author", freeComment.getAuthor().getUsername());
	        response.put("createDate", freeComment.getCreateDate());
	        
	        return ResponseEntity.ok(response);
	}

}
