package com.mysite.sbb.notice;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.user.UserService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/notice")
@RequiredArgsConstructor
@Controller

public class NoticeController {
	
	private final NoticeService noticeService;
	private final UserService userService;
	
	@GetMapping("/list")
	public String list(Model model, @RequestParam(value="page", defaultValue="0") int page) {
		Page<Notice> paging = this.noticeService.getList(page);
		model.addAttribute("paging", paging);
		return "notice_list";
	}
	
	// 작성 폼 보기 (GET)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/create")
    public String noticeCreate() {
        return "notice_form";
    }
    
	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/create")
	public String noticeCreate(@RequestParam(value = "subject") String subject, @RequestParam(value = "content") String content,Principal principal) {
		SiteUser siteUser = this.userService.getUser(principal.getName());
		this.noticeService.create(subject, content, siteUser);
		return "redirect:/notice/list";
	}

	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") Integer id) {
		Notice notice = this.noticeService.getNotice(id);
		this.noticeService.increaseViewCount(notice);
		model.addAttribute("notice", notice);
		return "notice_detail";
	}
}
