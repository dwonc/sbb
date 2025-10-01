package com.mysite.sbb.answer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sbb.user.SiteUser;

public interface AnswerRepository extends JpaRepository<Answer, Integer>{
	List<Answer> findByAuthorOrderByCreateDateDesc(SiteUser author);
}
