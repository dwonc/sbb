package com.mysite.sbb.main;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mysite.sbb.answer.AnswerRepository;
import com.mysite.sbb.freeboard.FreeBoard;
import com.mysite.sbb.freeboard.FreeBoardService;
import com.mysite.sbb.notice.Notice;
import com.mysite.sbb.notice.NoticeService;
import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;
import com.mysite.sbb.question.QuestionService;
import com.mysite.sbb.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class MainContorller {

	private final QuestionService questionService;
	private final FreeBoardService freeBoardService;
	private final NoticeService noticeService;
	private final UserRepository userRepository;
	private final AnswerRepository answerRepository;
	private final QuestionRepository questionRepository;
	
	@GetMapping("/")
	public String main(Model model) {
	    // 각 게시판 최신글 5개씩
	    List<Question> popularQuestions = questionService.getPopularQuestions(5);
	    List<FreeBoard> recentFreeBoards = freeBoardService.getRecentFreeBoards(5);
	    List<Notice> recentNotices = noticeService.getRecentNotices(3);
	    
	    model.addAttribute("popularQuestions", popularQuestions);
	    model.addAttribute("recentFreeBoards", recentFreeBoards);
	    model.addAttribute("recentNotices", recentNotices);
	    
	    // 통계
	    
	    long totalQuestions = questionRepository.count();
	    long totalAnswers = answerRepository.count();
	    long totalUsers = userRepository.count();
	    
	    model.addAttribute("totalQuestions", totalQuestions);
	    model.addAttribute("totalAnswers", totalAnswers);
	    model.addAttribute("totalUsers", totalUsers);
	    
	    return "main";
	}
}

