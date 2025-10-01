package com.mysite.sbb.freeboard;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.user.SiteUser;


public interface FreeBoardRepository extends JpaRepository<FreeBoard, Integer>{
	FreeBoard findBySubject(String subject);
	Page<FreeBoard> findAll(Pageable pageable);
	FreeBoard findBySubjectAndContent(String subject, String content);
	List<FreeBoard> findBySubjectLike(String subject);
	Page<FreeBoard> findAll(Specification<FreeBoard> spec, Pageable pageable);
	List<FreeBoard> findByAuthorOrderByCreateDateDesc(SiteUser author);
}
