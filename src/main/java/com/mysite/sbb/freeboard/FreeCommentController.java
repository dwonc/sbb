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
        FreeComment freeComment = this.freeCommentService.getFreeComment(id);
        if (!freeComment.getAuthor().getUsername().equals(principal.getName())) {
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
        FreeComment freeComment = this.freeCommentService.getFreeComment(id);
        if (!freeComment.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        this.freeCommentService.modify(freeComment, freeCommentForm.getContent());
        return String.format("redirect:/free/detail/%s#freecomment_%s", freeComment.getFreeBoard().getId(), freeComment.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String freeCommentDelete(Principal principal, @PathVariable("id") Integer id) {
        FreeComment freeComment = this.freeCommentService.getFreeComment(id);
        if (!freeComment.getAuthor().getUsername().equals(principal.getName())) {
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