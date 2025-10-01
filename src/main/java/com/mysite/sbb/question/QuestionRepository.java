package com.mysite.sbb.question;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sbb.user.SiteUser;

public interface QuestionRepository  extends JpaRepository<Question, Integer>{

	Question findBySubject(String subject);
	Question findBySubjectAndContent(String subject, String content);
	List<Question> findBySubjectLike(String subject);
	Page<Question> findAll(Pageable pegeable);
	Page<Question> findAll(Specification<Question> spec, Pageable pageable);
	List<Question> findByAuthorOrderByCreateDateDesc(SiteUser author);
	List<Question> findTop5ByOrderByViewCountDesc();

}
