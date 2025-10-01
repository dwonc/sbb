package com.mysite.sbb.freeboard;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sbb.answer.Answer;
import com.mysite.sbb.user.SiteUser;

public interface FreeCommentRepository extends JpaRepository<FreeComment, Integer>{
	List<FreeComment> findByAuthorOrderByCreateDateDesc(SiteUser author);
}