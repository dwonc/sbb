package com.mysite.sbb.freeboard;

import com.mysite.sbb.user.SiteUser;
import com.mysite.sbb.DataNotFoundException;
import com.mysite.sbb.answer.Answer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FreeCommentService {

	private final FreeCommentRepository freeCommentRepository;
	
	
	public FreeComment create(FreeBoard freeBoard, String content, SiteUser author) {
		FreeComment freeComment = new FreeComment();
		freeComment.setContent(content);
		freeComment.setCreateDate(LocalDateTime.now());
		freeComment.setFreeBoard(freeBoard);
		freeComment.setAuthor(author);
		this.freeCommentRepository.save(freeComment);
		return freeComment;
	}
	
	public FreeComment getFreeComment(Integer id) {
		Optional<FreeComment> freeComment = this.freeCommentRepository.findById(id);
		if(freeComment.isPresent()) {
			return freeComment.get();
		} else {
			throw new DataNotFoundException("freeComment not found");
		}
	}
	
	public void modify(FreeComment freeComment, String content) {
		freeComment.setContent(content);
		freeComment.setModifyDate(LocalDateTime.now());
		this.freeCommentRepository.save(freeComment);
	}
	
	public void delete(FreeComment freeComment) {
		this.freeCommentRepository.delete(freeComment);
	}
	
	public void vote(FreeComment freeComment, SiteUser siteUser) {
		freeComment.getVoter().add(siteUser);
		this.freeCommentRepository.save(freeComment);
	}
	
	public List<FreeComment> getFreecommentByUser(SiteUser user) {
	    return freeCommentRepository.findByAuthorOrderByCreateDateDesc(user);
	}
}

